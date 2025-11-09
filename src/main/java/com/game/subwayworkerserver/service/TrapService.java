package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.GpsAddTrapRequest;
import com.game.subwayworkerserver.controller.response.CoworkerListResponse;
import com.game.subwayworkerserver.controller.response.UserTrapListResponse;
import com.game.subwayworkerserver.domain.reward.BaseReward;
import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import com.game.subwayworkerserver.repository.UserItemMapRepository;
import com.game.subwayworkerserver.repository.UserTrapMapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrapService {

    private final UserRepository userRepository;
    private final UserItemMapRepository userItemMapRepository;
    private final UserTrapMapRepository userTrapMapRepository;
    private final CoworkerRepository coworkerRepository;
    private final UserLevelProcessor userLevelProcessor;

    private final RedisTemplate<String, String> redisTemplate;

    private static final String COWORKER_COWORKER_DATA_PREFIX = "trapped:coworker:data:";
    private static final long TRAP_VALID_HOURS = 12;

    /**
     * 덫 설치 API 요청을 처리하고, 아이템을 소모하여 트랩 정보를 DB에 저장합니다.
     *
     * @param uuid    현재 유저의 UUID
     * @param request 설치할 트랩 정보 (아이템 이름, 위도, 경도)
     */
    @Transactional
    public void setTrap(String uuid, GpsAddTrapRequest request) {

        // 1. 유저 정보 조회
        UserEntity user = getUserByUuid(uuid);
        long userId = user.getId();

        // 2. 트랩 아이템 정보 조회 (Enum)
        ItemName trapItem;
        try {
            trapItem = ItemName.fromConstantName(request.trapName());
        } catch (IllegalArgumentException e) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "유효하지 않은 트랩 아이템 이름입니다.");
        }

        // 트랩 카테고리인지 확인하는 로직 필요 (ItemCategory.TRAP 인지 확인)
        if (trapItem.getCategory() != ItemCategory.TRAP) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "선택된 아이템은 트랩 설치 도구가 아닙니다.");
        }
        // 3. 인벤토리에서 트랩 아이템 소모
        UserItemMapEntity itemMap = userItemMapRepository.findByUserIdAndItemName(userId, trapItem)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "인벤토리에 해당 트랩 아이템이 없습니다."));

        // 수량 확인 및 감소 (decreaseQuantity는 0 미만 방지 로직을 포함)
        try {
            itemMap.decreaseQuantity();
        } catch (IllegalStateException e) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "트랩 아이템 수량이 부족합니다.");
        }

        // 4. 트랩 설치 엔티티 생성 및 DB 저장 (12시간 후 만료)
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(TRAP_VALID_HOURS);
        UserTrapMapEntity setTrap = UserTrapMapEntity.of(
                userId,
                trapItem,
                request.latitude(),
                request.longitude(),
                expiresAt
        );
        userTrapMapRepository.save(setTrap);

        log.info("User {} set trap '{}' at ({}, {}). Expires at: {}",
                userId, trapItem.name(), request.latitude(), request.longitude(), expiresAt);
    }

    public UserTrapListResponse findTrapByUser(final String uuid) {
        UserEntity user = getUserByUuid(uuid);
        List<UserTrapMapEntity> userTrapMapList =
                userTrapMapRepository.findByUserIdAndExpiresAtAfterAndIsTrappedFalse(user.getId(), LocalDateTime.now());
        return UserTrapListResponse.of(userTrapMapList);
    }

    private UserEntity getUserByUuid(final String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(ErrorCode.USER_NOT_FOUND)
        );
    }

    @Transactional
    public CoworkerListResponse getTrapSuccessReward(final String uuid) {
        UserEntity user = getUserByUuid(uuid);
        List<UserTrapMapEntity> userTrapMapList = userTrapMapRepository.findByUserIdAndIsTrappedTrue(user.getId());
        List<CoworkerEntity> coworkerEntityList = saveUserTrapIfSuccess(userTrapMapList);
        user.updateTrapDone();

        if (coworkerEntityList.isEmpty()) {
            return CoworkerListResponse.empty();
        }
        int size = coworkerEntityList.size();
        BaseReward trapReward = BaseReward.getTrapReward();

        int totalCoin = trapReward.coin() * size;
        int totalExp = trapReward.exp() * size;
        user.increaseCoin(totalCoin);

        userLevelProcessor.processExpGainAndLevelUp(user, totalExp);

        return CoworkerListResponse.of(totalCoin, totalExp, coworkerEntityList);


    }

    private List<CoworkerEntity> saveUserTrapIfSuccess(final List<UserTrapMapEntity> userTrapMapList) {
        return userTrapMapList.stream().map(trapMap -> {
                    ItemName trapItemName = trapMap.getTrapItemName();

                    if (!trapItemName.canTrapSucceed()) {
                        trapMap.setIsNotSuccessful();
                        trapMap.setExpiresAt();
                        return null;
                    }

                    String capturedCoworkerId = trapMap.getCapturedCoworkerId();
                    String trappedKey = COWORKER_COWORKER_DATA_PREFIX + capturedCoworkerId;
                    Map<Object, Object> hashData = redisTemplate.opsForHash().entries(trappedKey);

                    if (hashData.isEmpty()) {
                        log.info("Null info for instance {}.", trappedKey);
                        trapMap.setIsNotSuccessful();
                        trapMap.setExpiresAt();
                        return null;
                    }

                    String coworkerName = (String) hashData.get("coworkerName");
                    String levelString = (String) hashData.get("coworkerLevel");

                    if (coworkerName != null && levelString != null) {
                        try {
                            int coworkerLevel = Integer.parseInt(levelString);

                            // 3. DB에 CoworkerEntity 저장
                            CoworkerEntity coworkerEntity = CoworkerEntity.of(trapMap.getUserId(), coworkerName, coworkerLevel);
                            CoworkerEntity savedCoworker = coworkerRepository.save(coworkerEntity);

                            // 4. ⭐ 트랩 상태 업데이트: 성공 처리 및 즉시 만료 ⭐
                            trapMap.setIsSuccessful();
                            trapMap.setExpiresAt();
                            redisTemplate.delete(trappedKey);

                            // 5. 성공한 친구 엔티티를 수집하기 위해 반환
                            return savedCoworker;
                        } catch (NumberFormatException e) {
                            log.error("Invalid coworkerLevel format: {}", levelString);
                            return null;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}

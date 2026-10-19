package com.game.subwayworkerserver.service.user;

import com.game.subwayworkerserver.service.common.UserFinder;
import com.game.subwayworkerserver.service.coworker.CoworkerListFilter;
import com.game.subwayworkerserver.domain.item.ItemName;
import com.game.subwayworkerserver.domain.item.ItemCategory;
import com.game.subwayworkerserver.domain.user.Gender;
import com.game.subwayworkerserver.controller.request.UserCreateRequest;
import com.game.subwayworkerserver.controller.response.coworker.*;
import com.game.subwayworkerserver.controller.response.item.*;
import com.game.subwayworkerserver.controller.response.user.*;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import com.game.subwayworkerserver.domain.baseinfo.SubscriptionItem;
import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.*;
import com.game.subwayworkerserver.config.CacheConfig;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserFinder userFinder;
    private final UserRepository userRepository;
    private final CoworkerRepository coworkerRepository;
    private final UserItemMapRepository userItemMapRepository;
    private final CoworkerListFilter coworkerListFilter;
    private final UserGachaRepository gachaRepository;
    private final SkillCardRepository skillCardRepository;
    private final AttendanceChecker attendanceChecker;
    private final UserSubscriptionRepository subscriptionRepository;

    public Boolean verifyUserName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }

        return userRepository.existsByName(name);
    }

    @Transactional
    public String createUser(final UserCreateRequest request) {
        CoworkerType coworkerType = CoworkerType.valueOf(request.coworkerName());

        if (userRepository.existsByName(request.username())) {
            throw new BaseException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        Gender userGender = Gender.fromString(request.gender());
        UserEntity userEntity = UserEntity.of(
                request.username(), UUID.randomUUID().toString(), userGender, request.email(), request.realName(),
                request.tossHash(), request.tossUserKey()
        );
        UserEntity savedUser = userRepository.save(userEntity);

        CoworkerEntity coworkerEntity = CoworkerEntity.of(savedUser.getId(), coworkerType);
        coworkerRepository.save(coworkerEntity);

        List<UserGachaEntity> gachaEntities = UserGachaEntity.from(userEntity.getId());
        gachaRepository.saveAll(gachaEntities);
        saveUserItemIntoMap(savedUser.getId());
        return savedUser.getUuid();
    }

    private void saveUserItemIntoMap(final long userId) {
        Map<String, Integer> defaultItems = ItemName.getDefaultItems();

        List<UserItemMapEntity> entitiesToSave = defaultItems.entrySet().stream()
                .map(entry -> {
                    ItemName itemName = ItemName.fromConstantName(entry.getKey());
                    return UserItemMapEntity.of(userId, itemName, entry.getValue());
                })
                .toList();

        if (!entitiesToSave.isEmpty()) {
            userItemMapRepository.saveAll(entitiesToSave);
        }
    }

    @Cacheable(value = CacheConfig.USER_INFO_CACHE, key = "#uuid")
    @Transactional
    public UserResponse getUserInfos(final String uuid) {

        UserEntity user = userFinder.getByUuid(uuid);
        attendanceChecker.checkDailyAttendance(user);

        List<CoworkerEntity> coworkerInUse = coworkerRepository.findByUserIdAndInUseTrue(user.getId());
        List<UserSubscriptionEntity> subscriptionList =
                subscriptionRepository.findAllByUserIdAndExpirationDateGreaterThanEqual(user.getId(), LocalDate.now());
        List<SubscriptionItem> items = subscriptionList.stream().map(UserSubscriptionEntity::getType).toList();

        return new UserResponse(
                UserBaseInfo.of(user, items),
                UserLevelInfo.of(user),
                CoworkerInUseInfo.of(coworkerInUse)
        );
    }

    public CaptureItemListResponse getCaptureItemList(final String uuid) {
        List<CaptureItemInfo> list = getItemsByCategory(uuid, ItemCategory.CAPTURE).stream()
                .map(item -> CaptureItemInfo.of(item.getItemName().getName(), item.getQuantity()))
                .collect(Collectors.toList());
        return CaptureItemListResponse.of(list);
    }

    public TrapItemListResponse getTrapItemList(final String uuid) {
        List<TrapItemInfo> list = getItemsByCategory(uuid, ItemCategory.TRAP).stream()
                .map(item -> TrapItemInfo.of(item.getItemName().getName(), item.getQuantity()))
                .collect(Collectors.toList());
        return TrapItemListResponse.of(list);
    }

    private List<UserItemMapEntity> getItemsByCategory(String uuid, ItemCategory category) {
        long userId = userFinder.getByUuid(uuid).getId();
        return userItemMapRepository.findByUserId(userId).stream()
                .filter(item -> item.getItemName().getCategory().equals(category))
                .collect(Collectors.toList());
    }

    @CacheEvict(value = CacheConfig.USER_INFO_CACHE, key = "#uuid")
    @Transactional
    public void deleteUser(final String uuid) {
        UserEntity user = userFinder.getByUuid(uuid);
        userRepository.delete(user);
    }

    public CoworkerListResponse getCoworkerListByUser(final String uuid) {
        UserEntity user = userFinder.getByUuid(uuid);
        List<CoworkerEntity> coworkerEntityList = coworkerRepository.findByUserIdAndOnSaleFalseAndAvailableTrue(user.getId());
        List<CoworkerEntity> sortedCoworkerList = coworkerListFilter.sort(coworkerEntityList);
        List<UserGachaEntity> treasureList = gachaRepository.findAllByUserId(user.getId());
        List<SkillCardEntity> skillCardList = skillCardRepository.findAllByUserIdAndInUseTrueAndAvailableTrue(user.getId());
        return CoworkerListResponse.of(sortedCoworkerList, treasureList, skillCardList);
    }

    public List<String> getAllUserUuids() {
        return userRepository.getAllUserUuid();
    }

    public UuidResponse getUuidByHash(final String tossHash) {
        Optional<String> uuidByTossHash = userRepository.findUuidByTossHash(tossHash);
        return uuidByTossHash.map(UuidResponse::new).orElseGet(UuidResponse::empty);
    }
}

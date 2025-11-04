package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.CaptureItemListResponse;
import com.game.subwayworkerserver.controller.request.UserCreateRequest;
import com.game.subwayworkerserver.controller.response.*;
import com.game.subwayworkerserver.domain.baseinfo.FriendType;
import com.game.subwayworkerserver.entity.*;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.FriendRepository;
import com.game.subwayworkerserver.repository.UserGachaRepository;
import com.game.subwayworkerserver.repository.UserItemMapRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final UserItemMapRepository userItemMapRepository;
    private final FriendListFilter friendListFilter;
    private final UserGachaRepository gachaRepository;


    public Boolean verifyUserName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }

        return userRepository.existsByName(name);
    }

    @Transactional
    public String createUser(final UserCreateRequest request) {
        FriendType friendType = FriendType.valueOf(request.friendName());

        if (userRepository.existsByName(request.username())) {
            throw new BaseException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        Gender userGender = Gender.fromString(request.gender());
        UserEntity userEntity = UserEntity.of(request.username(), UUID.randomUUID().toString(), userGender);
        UserEntity savedUser = userRepository.save(userEntity);

        FriendEntity friendEntity = FriendEntity.of(savedUser.getId(), friendType);
        friendRepository.save(friendEntity);

        List<UserGachaEntity> gachaEntities = UserGachaEntity.from(userEntity.getId());
        gachaRepository.saveAll(gachaEntities);
        return savedUser.getUuid();
    }

    public UserResponse getUserInfos(final String uuid) {

        UserEntity user = findUserBy(uuid);
        List<FriendEntity> friendInUse = friendRepository.findByUserIdAndInUseTrue(user.getId());

        return new UserResponse(
                UserBaseInfo.of(user),
                UserLevelInfo.of(user),
                FriendInUseInfo.of(friendInUse)
        );
    }

    public CaptureItemListResponse getCaptureItemList(final String uuid) {

        UserEntity user = findUserBy(uuid);

        List<UserItemMapEntity> userItems = userItemMapRepository.findByUserId(user.getId());

        List<CaptureItemInfo> list = userItems.stream()
                .filter(item -> item.getItemName().getCategory().equals(ItemCategory.CAPTURE)) // 포획 카테고리 필터링
                .map(item ->
                        CaptureItemInfo.of(
                                item.getItemName().getName(),
                                item.getQuantity()
                        )).toList();

        return new CaptureItemListResponse(list);
    }

    private UserEntity findUserBy(String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(() ->
                new BaseException(ErrorCode.USER_NOT_FOUND, "User not found with uuid: " + uuid)
        );
    }

    public TrapItemListResponse getTrapItemList(final String uuid) {

        UserEntity user = findUserBy(uuid);
        List<UserItemMapEntity> userItems = userItemMapRepository.findByUserId(user.getId());

        List<TrapItemInfo> list = userItems.stream()
                .filter(item -> item.getItemName().getCategory().equals(ItemCategory.TRAP)) // 포획 카테고리 필터링
                .map(item ->
                        TrapItemInfo.of(
                                item.getItemName().getName(),
                                item.getQuantity()
                        )).toList();

        return new TrapItemListResponse(list);
    }

    @Transactional
    public void deleteUser(final String uuid) {
        UserEntity user = findUserBy(uuid);
        userRepository.delete(user);
    }

    public FriendListResponse getFriendListByUser(final String uuid) {
        UserEntity user = findUserBy(uuid);
        List<FriendEntity> friendEntityList = friendRepository.findByUserIdAndOnSaleFalseAndAvailableTrue(user.getId());
        List<FriendEntity> sortedFriendList = friendListFilter.sort(friendEntityList);
        return FriendListResponse.of(sortedFriendList);
    }

    public List<String> getAllUserUuids() {
        return userRepository.getAllUserUuid();
    }
}

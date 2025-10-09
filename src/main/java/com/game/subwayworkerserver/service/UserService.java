package com.game.subwayworkerserver.service;

import com.amazonaws.services.kms.model.NotFoundException;
import com.game.subwayworkerserver.controller.request.UserCreateRequest;
import com.game.subwayworkerserver.controller.response.FriendInUseInfo;
import com.game.subwayworkerserver.controller.response.UserBaseInfo;
import com.game.subwayworkerserver.controller.response.UserLevelInfo;
import com.game.subwayworkerserver.controller.response.UserResponse;
import com.game.subwayworkerserver.entity.FriendEntity;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.repository.FriendRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.game.subwayworkerserver.domain.baseinfo.FriendAttribute.fromKoreanName;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    public Boolean verifyUserName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }

        return userRepository.existsByName(name);
    }

    @Transactional
    public String createUser(final UserCreateRequest request) {
        fromKoreanName(request.friendAttribute());

        UserEntity userEntity = UserEntity.of(request.username(), UUID.randomUUID().toString());
        UserEntity savedUser = userRepository.save(userEntity);

        FriendEntity friendEntity = FriendEntity.of(savedUser.getId(), request.friendAttribute());
        friendRepository.save(friendEntity);
        return savedUser.getUuid();
    }

    public UserResponse getUserInfos(final String uuid) {

        UserEntity user = userRepository.findByUuid(uuid).orElseThrow(() ->
                new NotFoundException("User not found with uuid: " + uuid)
        );
        Long userId = user.getId();
        List<FriendEntity> friendInUse = friendRepository.findByUserIdAndInUseTrue(userId);

        return new UserResponse(
                UserBaseInfo.of(user),
                UserLevelInfo.of(user),
                FriendInUseInfo.of(friendInUse)
        );
    }
}

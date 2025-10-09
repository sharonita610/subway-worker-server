package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.UserCreateRequest;
import com.game.subwayworkerserver.entity.FriendEntity;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.repository.FriendRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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


}

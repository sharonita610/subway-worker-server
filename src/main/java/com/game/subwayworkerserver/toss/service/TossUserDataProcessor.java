package com.game.subwayworkerserver.toss.service;

import com.game.subwayworkerserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TossUserDataProcessor {

    private final UserRepository userRepository;

    public ApiResponse<Map<String, Object>> process(ApiResponse<Map<String, Object>> userInfo) {

        Map<String, Object> userDataMap = userInfo.data();
        String username = null;
        String email = null;

        if (userDataMap != null) {
            username = Optional.ofNullable(userDataMap.get("name")).map(Object::toString).orElse(null);
            email = Optional.ofNullable(userDataMap.get("email")).map(Object::toString).orElse(null);
            System.out.println("email = " + email);
            System.out.println("username = " + username);
            if (username != null && email != null) {
                Optional<String> uuid = userRepository.findUuidByRealNameAndEmail(username, email);
                if (uuid.isPresent()) {
                    System.out.println("uuid = " + uuid);
                    userDataMap.put("uuid", uuid.get());
                } else {
                    userDataMap.put("uuid", "");
                }
            } else {
                userDataMap.put("uuid", "");
            }
        }
        return userInfo;
    }
}

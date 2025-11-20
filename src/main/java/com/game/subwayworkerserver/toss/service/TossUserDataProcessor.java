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
        Map<String, Object> fullInfoMap = null;
        String username = null;
        String email = null;

        System.out.println("userDataMap = " + userDataMap);
        if (userDataMap != null && userDataMap.containsKey("data")) {
            fullInfoMap = (Map<String, Object>) userDataMap.get("fullInfo");
        }

        username = Optional.ofNullable(fullInfoMap.get("name")).map(Object::toString).orElse(null);
        email = Optional.ofNullable(fullInfoMap.get("email")).map(Object::toString).orElse(null);
        System.out.println("email = " + email);
        System.out.println("username = " + username);
        if (username != null && email != null) {
            Optional<String> uuid = userRepository.findUuidByRealNameAndEmail(username, email);
            if (uuid.isPresent()) {
                System.out.println("uuid = " + uuid);
                fullInfoMap.put("uuid", uuid.get());
            }
        } else {
            fullInfoMap.put("uuid", "");
        }
        return userInfo;
    }
}

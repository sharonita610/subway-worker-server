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

        Map<String, Object> topLevelMap = userInfo.data();
        Map<String, Object> successMap = null;

        System.out.println("userDataMap = " + topLevelMap);
        if (topLevelMap != null && topLevelMap.containsKey("success")) {
            Object successObject = topLevelMap.get("success");
            if (successObject instanceof Map) {
                successMap = (Map<String, Object>) successObject;
            }
        }
        String username = null;
        String email = null;

        if (successMap != null) {
            username = Optional.ofNullable(successMap.get("name")).map(Object::toString).orElse(null);
            email = Optional.ofNullable(successMap.get("email")).map(Object::toString).orElse(null);

            if (username != null && email != null) {
                Optional<String> uuid = userRepository.findUuidByRealNameAndEmail(username, email);

                if (uuid.isPresent()) {
                    System.out.println("uuid = " + uuid);
                    successMap.put("uuid", uuid.get());
                } else {
                    successMap.put("uuid", "");
                }
            }
        }

        System.out.println(" userInfo = " +  userInfo);
        return userInfo;
    }
}

package com.game.subwayworkerserver.toss.service;

import com.game.subwayworkerserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

        if (topLevelMap != null && topLevelMap.containsKey("success")) {
            Object successObject = topLevelMap.get("success");
            if (successObject instanceof Map) {
                successMap = (Map<String, Object>) successObject;
            }
        }
        String userKey = null;

        if (successMap != null) {
            userKey = Optional.ofNullable(successMap.get("userKey")).map(Object::toString).orElse(null);

            if (userKey != null) {
                Optional<String> uuid = userRepository.findUuidByTossUserKey(userKey);

                if (uuid.isPresent()) {
                    successMap.put("uuid", uuid.get());
                } else {
                    successMap.put("uuid", "");
                }
            }
        }

        return userInfo;
    }
}

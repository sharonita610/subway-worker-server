package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.response.FriendAttributesResponse;
import com.game.subwayworkerserver.domain.baseinfo.FriendAttribute;
import com.game.subwayworkerserver.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendService {


    private final FriendRepository friendRepository;

    public FriendAttributesResponse getFriendAttributes() {
        return FriendAttributesResponse.from(FriendAttribute.getAllAttributesAsList());
    }
}

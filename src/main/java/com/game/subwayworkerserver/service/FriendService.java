package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.response.FriendTypesResponse;
import com.game.subwayworkerserver.domain.baseinfo.FriendType;
import com.game.subwayworkerserver.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendService {


    private final FriendRepository friendRepository;

    public FriendTypesResponse getFriendAttributes() {
        return FriendTypesResponse.from(FriendType.getAll());
    }
}

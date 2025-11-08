package com.game.subwayworkerserver.controller.response;

import com.game.subwayworkerserver.domain.user.MailReward;

import java.util.List;

public record UserMailBoxResponse (
        List<MailReward> mailList

){
    public static UserMailBoxResponse of(List<MailReward> domainList) {
        return new UserMailBoxResponse(domainList);
    }
}

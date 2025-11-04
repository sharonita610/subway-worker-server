package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.SkillCardFilterRequest;
import com.game.subwayworkerserver.domain.market.SkillCardOnSale;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SkillCardListFilter {
    public List<SkillCardOnSale> filter(List<SkillCardOnSale> friendOnSalesDomainList, SkillCardFilterRequest request) {
        return null;
    }
}

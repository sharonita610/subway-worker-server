package com.game.subwayworkerserver.controller.request;

import java.util.List;

public record CoworkerFilterRequest(
        List<String> elements, // "성과", "협력" 등
        List<String> skills, // "보조배터리", "피로회복제" 등
        String sortType // "가격높은순", "공격력강한순" 등
) {}

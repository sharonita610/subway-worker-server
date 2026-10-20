package com.game.subwayworkerserver.dto.request.market;

import java.util.List;

public record CoworkerFilterRequest(
        List<String> elements,
        List<String> skills,
        String sortType
) {}

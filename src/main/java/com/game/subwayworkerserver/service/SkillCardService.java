package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.SkillByUserRequest;
import com.game.subwayworkerserver.controller.response.SkillByUserResponse;
import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.entity.SkillCardEntity;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import com.game.subwayworkerserver.repository.SkillCardRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillCardService {

    private final UserRepository userRepository;
    private final CoworkerRepository coworkerId;
    private final SkillCardRepository skillCardRepository;

    public SkillByUserResponse getSkillListByUser(String uuid, SkillByUserRequest request) {
        UserEntity user = userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(ErrorCode.USER_NOT_FOUND)
        );

        CoworkerEntity coworkerEntity = coworkerId.findById(request.coworkerId()).orElseThrow(
                () -> new BaseException(ErrorCode.NOT_FOUND, "Coworker not found.")
        );

        SkillCardType skillCardType = SkillCardType.valueOf(request.cardType().toUpperCase());

        List<Integer> requestedSkill = new ArrayList<>();

        if (skillCardType.equals(SkillCardType.ACTIVE)) {
            requestedSkill.add(coworkerEntity.getActiveSkill1());
            requestedSkill.add(coworkerEntity.getActiveSkill2());
        } else{
            requestedSkill.add(coworkerEntity.getPassiveSkill1());
            requestedSkill.add(coworkerEntity.getPassiveSkill2());
        }

        List<SkillCardEntity> skillCardEntityList =
                skillCardRepository.findAllByCoworkerIdAndAvailableTrueAndSkillCardEnumIdIn(user.getId(), requestedSkill);

        return SkillByUserResponse.of(skillCardType, requestedSkill, skillCardEntityList);
    }
}

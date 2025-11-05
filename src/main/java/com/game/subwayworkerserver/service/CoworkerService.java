package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.PartnerConfigureRequest;
import com.game.subwayworkerserver.controller.response.CoworkerTypesResponse;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoworkerService {

    private final CoworkerRepository coworkerRepository;
    private final UserRepository userRepository;

    public CoworkerTypesResponse getCoworkerElements() {
        return CoworkerTypesResponse.from(CoworkerType.getAll());
    }

    @Transactional
    public void configurePartners(final String uuid, final PartnerConfigureRequest request) {

        long userId = userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(ErrorCode.USER_NOT_FOUND)
        ).getId();

        List<Long> requestCoworkerIds = request.coworkerIds();
        List<CoworkerEntity> currentPartners = coworkerRepository.findByUserIdAndInUseTrue(userId);

        currentPartners.stream()
                .filter(p -> !requestCoworkerIds.contains(p.getId()))
                .forEach(CoworkerEntity::setInUseFalse);

        List<CoworkerEntity> requestedCoworkers = coworkerRepository.findAllById(requestCoworkerIds);
        requestedCoworkers.forEach(coworker -> {
            if (coworker.getUserId() == userId) {
                coworker.setInUseTrue();
            } else {
                throw new BaseException(ErrorCode.BAD_REQUEST, "자신의 동료만 파트너로 지정할 수 있습니다.");
            }
        });
    }
}

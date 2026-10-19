package com.game.subwayworkerserver.service.gps;

import com.game.subwayworkerserver.controller.request.GpsRequest;
import com.game.subwayworkerserver.controller.response.gps.RandomGpsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GpsService {

    private final GpsItemsCreator gpsItemsCreator;
    private final GpsItemsFinder gpsItemsFinder;

    public RandomGpsResponse createAndGetItems(final GpsRequest request, final String uuid) {
        gpsItemsCreator.createItemsInArea(request.latitude(), request.longitude());
        return gpsItemsFinder.findItemsInArea(request.latitude(), request.longitude(), uuid);
    }
}

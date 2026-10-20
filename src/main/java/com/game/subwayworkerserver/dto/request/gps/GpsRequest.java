package com.game.subwayworkerserver.dto.request.gps;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GpsRequest(
        @NotNull(message = "위도는 필수입니다.")
        @Min(value = -90, message = "위도 값은 -90보다 커야 합니다.")
        @Max(value = 90, message = "위도 값은 90보다 작아야 합니다.")
        Double latitude,

        @NotNull(message = "경도는 필수입니다.")
        @Min(value = -180, message = "경도 값은 -180보다 커야 합니다.")
        @Max(value = 180, message = "경도 값은 180보다 작아야 합니다.")
        Double longitude,

        @NotNull(message = "반경(km)은 필수입니다.")
        @DecimalMin(value = "0.5", inclusive = true, message = "반경은 0.5km 이상이어야 합니다.")
        Double rangeKm
) {}


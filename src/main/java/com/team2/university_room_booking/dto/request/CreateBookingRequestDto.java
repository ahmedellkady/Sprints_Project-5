package com.team2.university_room_booking.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class CreateBookingRequestDto {

    @NotNull
    private Long roomId;

    @NotNull
    @FutureOrPresent
    private LocalDateTime startTime;

    @NotNull
    @FutureOrPresent
    private LocalDateTime endTime;

    // Optional: IDs of features the room must have
    private Set<Long> requiredFeatureIds;

    @NotBlank
    private String purpose;
}

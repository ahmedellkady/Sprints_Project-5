package com.team2.university_room_booking.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class RecurringBookingRequestDto {

    @NotNull
    private Long roomId;

    @NotNull
    @FutureOrPresent
    private LocalDateTime startTime;

    @NotNull
    @FutureOrPresent
    private LocalDateTime endTime;

    @NotBlank
    private String purpose;

    // Optional: IDs of features the room must have
    private Set<Long> requiredFeatureIds;

    // Weekly recurrence specification
    @NotEmpty
    private Set<DayOfWeek> daysOfWeek;

    // Specify series end using either 'until' (preferred) or 'occurrences' (count)
    @FutureOrPresent
    private LocalDateTime until; // inclusive boundary for last occurrence start

    private Integer occurrences; // total number of occurrences to create (must be > 0 if provided)
}

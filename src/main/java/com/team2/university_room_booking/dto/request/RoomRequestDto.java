package com.team2.university_room_booking.dto.request;

import com.team2.university_room_booking.enums.RoomType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequestDto {

    @NotBlank(message = "Room name is required")
    @Size(min = 2, max = 50, message = "Room name must be between 2 and 50 characters")
    private String name;

    @NotNull(message = "Room type is required")
    private RoomType type;

    @Min(value = 1, message = "Capacity must be at least 1")
    private int capacity;

    @NotNull(message = "Building ID is required")
    private Long buildingId;

    private Set<Long> featureIds = new HashSet<>();

    private boolean available = true; // default to true, can be overridden
}
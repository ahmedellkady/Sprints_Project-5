package com.team2.university_room_booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRoomFeatureDto {
    @NotBlank(message = "Feature name is required")
    private String name;
}

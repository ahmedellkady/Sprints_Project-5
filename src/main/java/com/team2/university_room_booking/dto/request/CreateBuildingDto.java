package com.team2.university_room_booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBuildingDto {
    @NotBlank(message = "Building name is required")
    private String name;

    @NotNull(message = "Department ID is required")
    private Long departmentId;
}

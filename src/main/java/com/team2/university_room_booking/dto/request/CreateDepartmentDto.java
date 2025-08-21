package com.team2.university_room_booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDepartmentDto {
    @NotBlank(message = "Department name is required")
    private String name;
}
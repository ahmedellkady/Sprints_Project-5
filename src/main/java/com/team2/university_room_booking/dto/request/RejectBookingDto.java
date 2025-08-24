package com.team2.university_room_booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectBookingDto {
    @NotBlank(message = "Reason is required for rejection")
    private String reason;
}

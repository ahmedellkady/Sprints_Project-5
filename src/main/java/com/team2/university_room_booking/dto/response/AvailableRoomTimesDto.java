package com.team2.university_room_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AvailableRoomTimesDto {
    private LocalDateTime start;
    private LocalDateTime end;
}

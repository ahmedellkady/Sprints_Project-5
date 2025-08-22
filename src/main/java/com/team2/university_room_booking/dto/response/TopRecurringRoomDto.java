package com.team2.university_room_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopRecurringRoomDto {
    private Long roomId;
    private long bookingCount;
}

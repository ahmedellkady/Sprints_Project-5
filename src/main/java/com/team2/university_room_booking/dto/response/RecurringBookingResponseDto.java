package com.team2.university_room_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurringBookingResponseDto {
    private List<Long> bookingIds;
    private int count;
}

package com.team2.university_room_booking.dto.request;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HolidayRequestDto {
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
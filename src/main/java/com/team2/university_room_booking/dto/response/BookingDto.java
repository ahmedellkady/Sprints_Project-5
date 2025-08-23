package com.team2.university_room_booking.dto.response;

import com.team2.university_room_booking.enums.BookingStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    private Long Id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
    private BookingStatus status;
    private String roomName;
    private String userUsername;
}

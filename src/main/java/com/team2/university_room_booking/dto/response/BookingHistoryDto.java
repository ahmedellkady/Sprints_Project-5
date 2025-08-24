package com.team2.university_room_booking.dto.response;

import com.team2.university_room_booking.enums.BookingStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingHistoryDto {

    private Long id;
    private LocalDateTime timestamp;

    private Long bookingId;

    // User who owns the booking
    private Long userId;
    private String userName;


    private Long actorId;
    private String actorName;
    private String actorRole; // ADMIN, STUDENT, FACULTY MEMBER

    private BookingStatus status;
    private String reason;
}

package com.team2.university_room_booking.model;
import java.time.LocalDateTime;

import com.team2.university_room_booking.enums.BookingStatus;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
    
}

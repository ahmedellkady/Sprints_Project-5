package com.team2.university_room_booking.repository;

import com.team2.university_room_booking.model.Booking;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByRoomId(Long roomId);

    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.room.name = :roomName
          AND (b.status IS NULL OR b.status <> 'CANCELLED')
          AND b.startTime < :end
          AND b.endTime   > :start
        """)
    Long countOverlaps(@Param("roomName") String roomName,
                       @Param("start") LocalDateTime start,
                       @Param("end") LocalDateTime end);
}

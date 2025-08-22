package com.team2.university_room_booking.repository;

import com.team2.university_room_booking.enums.BookingStatus;
import com.team2.university_room_booking.model.Booking;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByRoomId(Long roomId);

    boolean existsByRoomIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            Long roomId,
            Collection<BookingStatus> statuses,
            LocalDateTime newEnd,
            LocalDateTime newStart
    );

    // Projection for room usage count
    interface RoomBookingCount {
        Long getRoomId();
        long getCount();
    }

    @Query("""
            select b.room.id as roomId, count(b.id) as count
            from Booking b
            where b.user.id = :userId
            group by b.room.id
            order by count(b.id) desc
            """)
    List<RoomBookingCount> findTopRoomsByUser(@Param("userId") Long userId, Pageable pageable);

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

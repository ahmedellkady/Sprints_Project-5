package com.team2.university_room_booking.controller;

import com.team2.university_room_booking.dto.request.CreateBookingRequestDto;
import com.team2.university_room_booking.dto.response.TopRecurringRoomDto;
import com.team2.university_room_booking.model.Booking;
import com.team2.university_room_booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Void> createBooking(@Valid @RequestBody CreateBookingRequestDto request) {
        Booking created = bookingService.createBooking(request);
        URI location = URI.create("/bookings/" + created.getId());
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/users/{userId}/recurring-bookings")
    public ResponseEntity<List<TopRecurringRoomDto>> getTopRecurringRooms(
            @PathVariable Long userId,
            @RequestParam(name = "limit", defaultValue = "3") int limit
    ) {
        var result = bookingService.getTopRecurringRoomsForUser(userId, limit);
        return ResponseEntity.ok(result);
    }
}

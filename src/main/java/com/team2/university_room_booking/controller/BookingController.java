package com.team2.university_room_booking.controller;

import com.team2.university_room_booking.dto.request.CreateBookingRequestDto;
import com.team2.university_room_booking.dto.request.RejectBookingDto;
import com.team2.university_room_booking.dto.response.BookingDto;
import com.team2.university_room_booking.dto.response.TopRecurringRoomDto;
import com.team2.university_room_booking.enums.BookingStatus;
import com.team2.university_room_booking.model.Booking;
import com.team2.university_room_booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Book;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY_MEMBER')")
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

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingDto>> getAllBookingsByStatus(@PathVariable BookingStatus status){
        return ResponseEntity.ok(bookingService.getAllBookingsByStatus(status));

    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingDto> approveBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.approveBooking(id));

    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingDto> rejectBooking(@PathVariable Long id, @Valid @RequestBody RejectBookingDto rejectBookingDto) {
        return ResponseEntity.ok(bookingService.rejectBooking(id, rejectBookingDto));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY_MEMBER')")
    public ResponseEntity<BookingDto> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }
}

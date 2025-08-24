package com.team2.university_room_booking.controller;

import com.team2.university_room_booking.dto.response.BookingHistoryDto;
import com.team2.university_room_booking.enums.BookingStatus;
import com.team2.university_room_booking.service.BookingHistoryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN')")
public class BookingHistoryController{

    private final BookingHistoryService bookingHistoryService;

    public BookingHistoryController(BookingHistoryService bookingHistoryService) {
        this.bookingHistoryService = bookingHistoryService;
    }

    @GetMapping("/booking-history")
    public ResponseEntity<List<BookingHistoryDto>> getAuditTrail(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {

        List<BookingHistoryDto> result = bookingHistoryService.getAuditTrail(
                userId, bookingId, status, dateFrom, dateTo);
        return ResponseEntity.ok(result);
    }
}

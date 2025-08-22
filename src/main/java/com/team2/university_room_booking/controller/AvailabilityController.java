package com.team2.university_room_booking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team2.university_room_booking.dto.request.AvailabilityRequestDto;
import com.team2.university_room_booking.service.AvailabilityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    @PostMapping("/room/{roomName}")
    public ResponseEntity<Boolean> isRoomAvailable(@PathVariable String roomName, @RequestBody AvailabilityRequestDto dto) {
        boolean isAvailable = availabilityService.isRoomAvailable(roomName, dto);
        return ResponseEntity.ok(isAvailable);
    }
}

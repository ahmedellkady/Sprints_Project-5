package com.team2.university_room_booking.controller;

import com.team2.university_room_booking.dto.request.AvailabilityRequestDto;
import com.team2.university_room_booking.dto.request.RoomRequestDto;
import com.team2.university_room_booking.dto.response.AvailableRoomTimesDto;
import com.team2.university_room_booking.dto.response.RoomDto;
import com.team2.university_room_booking.service.BookingService;
import com.team2.university_room_booking.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // Create room
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomDto> createRoom(@Valid @RequestBody RoomRequestDto requestDto) {
        RoomDto createdRoom = roomService.createRoom(requestDto);
        return ResponseEntity.ok(createdRoom);
    }

    // Get all rooms
    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    // Get room by ID
    @GetMapping("/{id}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    // Update room
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomDto> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequestDto requestDto
    ) {
        RoomDto updatedRoom = roomService.updateRoom(id, requestDto);
        return ResponseEntity.ok(updatedRoom);
    }

    // Delete room
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roomId}/availability")
    public ResponseEntity<List<AvailableRoomTimesDto>> getRoomAvailability(@PathVariable Long roomId, @Valid @RequestBody AvailabilityRequestDto dto) {
        List<AvailableRoomTimesDto> freeSlots = roomService.getRoomAvailability(roomId, dto);
        return ResponseEntity.ok(freeSlots);
    }
}

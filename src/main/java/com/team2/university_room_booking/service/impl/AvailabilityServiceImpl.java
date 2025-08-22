package com.team2.university_room_booking.service.impl;


import org.springframework.stereotype.Service;

import com.team2.university_room_booking.dto.request.AvailabilityRequestDto;
import com.team2.university_room_booking.exceptions.BadRequestException;
import com.team2.university_room_booking.exceptions.ResourceNotFoundException;
import com.team2.university_room_booking.repository.BookingRepository;
import com.team2.university_room_booking.repository.RoomRepository;
import com.team2.university_room_booking.service.AvailabilityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    @Override
    public boolean isRoomAvailable(String roomName, AvailabilityRequestDto dto) {
        if (!dto.getEnd().isAfter(dto.getStart()))
        throw new BadRequestException("End date must be after start date.");
        
        roomRepository.findByName(roomName).orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        
        long overlaps = bookingRepository.countOverlaps(roomName, dto.getStart(), dto.getEnd());
        
        return overlaps == 0;
    }
}

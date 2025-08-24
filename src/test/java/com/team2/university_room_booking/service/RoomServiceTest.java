package com.team2.university_room_booking.service;

import com.team2.university_room_booking.dto.request.AvailabilityRequestDto;
import com.team2.university_room_booking.dto.request.RoomRequestDto;
import com.team2.university_room_booking.dto.response.RoomDto;
import com.team2.university_room_booking.exceptions.BadRequestException;
import com.team2.university_room_booking.exceptions.ResourceConflictException;
import com.team2.university_room_booking.exceptions.ResourceNotFoundException;
import com.team2.university_room_booking.mapper.DtoMapper;
import com.team2.university_room_booking.model.*;
import com.team2.university_room_booking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock private RoomRepository roomRepository;
    @Mock private RoomFeatureRepository roomFeatureRepository;
    @Mock private BuildingRepository buildingRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private DtoMapper dtoMapper;

    @InjectMocks private RoomService roomService;
    private Room room;
    private RoomDto roomDto;
    private RoomRequestDto roomRequestDto;
    private Building building;

    @BeforeEach
    void setUp() {
        building = new Building();
        building.setId(1L);
        building.setName("Main Building");

        room = new Room();
        room.setId(1L);
        room.setName("A101");
        room.setBuilding(building);

        roomDto = new RoomDto();
        roomDto.setId(1L);
        roomDto.setName("A101");

        roomRequestDto = new RoomRequestDto();
        roomRequestDto.setName("A101");
        roomRequestDto.setBuildingId(1L);
        roomRequestDto.setCapacity(50);
        roomRequestDto.setAvailable(true);
        roomRequestDto.setFeatureIds(Set.of());
    }

    @Test
    void createRoom_Success() {
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(roomRepository.existsByNameAndBuildingId("A101", 1L)).thenReturn(false);
        when(dtoMapper.toRoomEntity(roomRequestDto)).thenReturn(room);
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        when(dtoMapper.toRoomDto(room)).thenReturn(roomDto);

        RoomDto result = roomService.createRoom(roomRequestDto);

        assertNotNull(result);
        assertEquals("A101", result.getName());
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void createRoom_Fail_BuildingNotFound() {
        when(buildingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roomService.createRoom(roomRequestDto));
    }

    @Test
    void deleteRoom_Fail_WithBookings() {
        when(roomRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.existsByRoomId(1L)).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> roomService.deleteRoom(1L));
    }

    @Test
    void getRoomAvailability_Success() {
        AvailabilityRequestDto req = new AvailabilityRequestDto();
        req.setStart(LocalDateTime.now().plusHours(1));
        req.setEnd(LocalDateTime.now().plusHours(3));

        when(bookingRepository.findAllOverlappingBookings(anyLong(), anyList(), any(), any()))
                .thenReturn(new ArrayList<>(List.of()));

        var result = roomService.getRoomAvailability(1L, req);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getRoomAvailability_Fail_InvalidTime() {
        AvailabilityRequestDto req = new AvailabilityRequestDto();
        req.setStart(LocalDateTime.now().plusHours(2));
        req.setEnd(LocalDateTime.now().plusHours(1));

        assertThrows(BadRequestException.class, () -> roomService.getRoomAvailability(1L, req));
    }
}


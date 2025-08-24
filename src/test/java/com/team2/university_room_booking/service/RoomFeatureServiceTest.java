package com.team2.university_room_booking.service;

import com.team2.university_room_booking.dto.request.CreateRoomFeatureDto;
import com.team2.university_room_booking.dto.response.RoomFeatureDto;
import com.team2.university_room_booking.exceptions.BadRequestException;
import com.team2.university_room_booking.exceptions.ResourceNotFoundException;
import com.team2.university_room_booking.mapper.DtoMapper;
import com.team2.university_room_booking.model.RoomFeature;
import com.team2.university_room_booking.repository.RoomFeatureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomFeatureServiceTest {

    @Mock
    private RoomFeatureRepository repository;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private RoomFeatureService service;

    private RoomFeature feature;
    private RoomFeatureDto featureDto;
    private CreateRoomFeatureDto createDto;

    @BeforeEach
    void setUp() {
        feature = new RoomFeature();
        feature.setId(1L);
        feature.setName("Projector");

        featureDto = new RoomFeatureDto();
        featureDto.setId(1L);
        featureDto.setName("Projector");

        createDto = new CreateRoomFeatureDto();
        createDto.setName("Projector");
    }

    @Test
    void testCreate_Success() {
        when(dtoMapper.toRoomFeatureEntity(createDto)).thenReturn(feature);
        when(repository.save(feature)).thenReturn(feature);
        when(dtoMapper.toRoomFeatureDto(feature)).thenReturn(featureDto);

        RoomFeatureDto result = service.create(createDto);

        assertNotNull(result);
        assertEquals("Projector", result.getName());
        verify(repository).save(feature);
    }

    @Test
    void testCreate_Fail_NullName() {
        CreateRoomFeatureDto dto = new CreateRoomFeatureDto();
        dto.setName("");

        assertThrows(BadRequestException.class, () -> service.create(dto));
        verify(repository, never()).save(any());
    }

    @Test
    void testGetAll() {
        when(repository.findAll()).thenReturn(Arrays.asList(feature));
        when(dtoMapper.toRoomFeatureDto(feature)).thenReturn(featureDto);

        List<RoomFeatureDto> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals("Projector", result.get(0).getName());
    }

    @Test
    void testGetById_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(feature));
        when(dtoMapper.toRoomFeatureDto(feature)).thenReturn(featureDto);

        RoomFeatureDto result = service.getById(1L);

        assertEquals("Projector", result.getName());
    }

    @Test
    void testGetById_NotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getById(1L));
    }

    @Test
    void testUpdate_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(feature));
        when(repository.save(feature)).thenReturn(feature);
        when(dtoMapper.toRoomFeatureDto(feature)).thenReturn(featureDto);

        RoomFeatureDto result = service.update(1L, featureDto);

        assertEquals("Projector", result.getName());
        verify(repository).save(feature);
    }

    @Test
    void testUpdate_NotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, featureDto));
    }

    @Test
    void testDelete_Success() {
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.countRoomsWithFeature(1L)).thenReturn(0);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void testDelete_NotFound() {
        when(repository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));
    }

    @Test
    void testDelete_Fail_FeatureInUse() {
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.countRoomsWithFeature(1L)).thenReturn(2);

        assertThrows(BadRequestException.class, () -> service.delete(1L));
        verify(repository, never()).deleteById(1L);
    }
}

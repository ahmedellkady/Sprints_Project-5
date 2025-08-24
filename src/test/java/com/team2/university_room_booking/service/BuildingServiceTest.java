package com.team2.university_room_booking.service;

import com.team2.university_room_booking.dto.request.CreateBuildingDto;
import com.team2.university_room_booking.dto.response.BuildingDto;
import com.team2.university_room_booking.exceptions.BadRequestException;
import com.team2.university_room_booking.exceptions.ResourceNotFoundException;
import com.team2.university_room_booking.mapper.DtoMapper;
import com.team2.university_room_booking.model.Building;
import com.team2.university_room_booking.model.Department;
import com.team2.university_room_booking.model.Room;
import com.team2.university_room_booking.repository.BuildingRepository;
import com.team2.university_room_booking.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildingServiceTest {

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private BuildingService buildingService;

    private CreateBuildingDto createBuildingDto;
    private Building building;
    private Department department;
    private BuildingDto buildingDto;

    @BeforeEach
    void setUp() {
        createBuildingDto = new CreateBuildingDto();
        createBuildingDto.setName("Engineering Building");
        createBuildingDto.setDepartmentId(1L);

        department = new Department();
        department.setId(1L);
        department.setName("Engineering");

        building = new Building();
        building.setId(1L);
        building.setName("Engineering Building");
        building.setDepartment(department);
        building.setRooms(Collections.emptySet());

        buildingDto = new BuildingDto();
        buildingDto.setId(1L);
        buildingDto.setName("Engineering Building");
        buildingDto.setDepartmentName("Engineering");
    }


    @Test
    void testCreateBuilding_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(buildingRepository.save(any(Building.class))).thenReturn(building);
        when(dtoMapper.toBuildingEntity(createBuildingDto)).thenReturn(building);
        when(dtoMapper.toBuildingDto(building)).thenReturn(buildingDto);

        BuildingDto result = buildingService.createBuilding(createBuildingDto);

        assertNotNull(result);
        assertEquals("Engineering Building", result.getName());
        verify(buildingRepository, times(1)).save(any(Building.class));
    }

    @Test
    void testCreateBuilding_DepartmentNotFound() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());
        when(dtoMapper.toBuildingEntity(createBuildingDto)).thenReturn(building);

        assertThrows(ResourceNotFoundException.class,
                () -> buildingService.createBuilding(createBuildingDto));
    }

    @Test
    void testGetBuildingById_Success() {
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(dtoMapper.toBuildingDto(building)).thenReturn(buildingDto);

        BuildingDto result = buildingService.getBuildingById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetBuildingById_NotFound() {
        when(buildingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> buildingService.getBuildingById(1L));
    }

    @Test
    void testUpdateBuilding_Success() {
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(buildingRepository.save(any(Building.class))).thenReturn(building);
        when(dtoMapper.toBuildingDto(building)).thenReturn(buildingDto);

        BuildingDto result = buildingService.updateBuilding(1L, createBuildingDto);

        assertNotNull(result);
        assertEquals("Engineering Building", result.getName());
    }

    @Test
    void testDeleteBuilding_Success() {
        when(buildingRepository.existsById(1L)).thenReturn(true);
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));

        buildingService.deleteBuilding(1L);

        verify(buildingRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteBuilding_NotFound() {
        when(buildingRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> buildingService.deleteBuilding(1L));
    }

    @Test
    void testDeleteBuilding_WithRooms() {
        building.setRooms(Collections.singleton(new Room()));
        when(buildingRepository.existsById(1L)).thenReturn(true);
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));

        assertThrows(BadRequestException.class,
                () -> buildingService.deleteBuilding(1L));
    }
}

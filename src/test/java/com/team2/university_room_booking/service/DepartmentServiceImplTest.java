package com.team2.university_room_booking.service;

import com.team2.university_room_booking.dto.request.CreateDepartmentDto;
import com.team2.university_room_booking.dto.response.DepartmentDto;
import com.team2.university_room_booking.exceptions.BadRequestException;
import com.team2.university_room_booking.exceptions.ResourceNotFoundException;
import com.team2.university_room_booking.mapper.DtoMapper;
import com.team2.university_room_booking.model.Department;
import com.team2.university_room_booking.repository.DepartmentRepository;
import com.team2.university_room_booking.service.impl.DepartmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private DepartmentServiceImpl departmentService;
    private Department department;
    private CreateDepartmentDto createDto;
    private DepartmentDto departmentDto;

    @BeforeEach
    void setUp() {
        createDto = new CreateDepartmentDto();
        createDto.setName("Engineering");

        department = new Department();
        department.setId(1L);
        department.setName("Engineering");

        departmentDto = new DepartmentDto();
        departmentDto.setId(1L);
        departmentDto.setName("Engineering");
    }

    @Test
    void testCreateDepartment_Success() {
        when(dtoMapper.toDepartmentEntity(createDto)).thenReturn(department);
        when(departmentRepository.existsByName("Engineering")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);
        when(dtoMapper.toDepartmentDto(department)).thenReturn(departmentDto);

        DepartmentDto result = departmentService.createDepartment(createDto);

        assertNotNull(result);
        assertEquals("Engineering", result.getName());
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void testCreateDepartment_NameEmpty() {
        Department emptyDepartment = new Department();
        emptyDepartment.setName(""); // force empty

        when(dtoMapper.toDepartmentEntity(createDto)).thenReturn(emptyDepartment);

        assertThrows(BadRequestException.class,
                () -> departmentService.createDepartment(createDto));
    }


    @Test
    void testCreateDepartment_DuplicateName() {
        when(dtoMapper.toDepartmentEntity(createDto)).thenReturn(department);
        when(departmentRepository.existsByName("Engineering")).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> departmentService.createDepartment(createDto));
    }

    @Test
    void testGetAllDepartments() {
        when(departmentRepository.findAll()).thenReturn(List.of(department));
        when(dtoMapper.toDepartmentDto(department)).thenReturn(departmentDto);

        List<DepartmentDto> result = departmentService.getAllDepartments();

        assertEquals(1, result.size());
        assertEquals("Engineering", result.get(0).getName());
    }

    @Test
    void testGetDepartmentById_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(dtoMapper.toDepartmentDto(department)).thenReturn(departmentDto);

        DepartmentDto result = departmentService.getDepartmentById(1L);

        assertNotNull(result);
        assertEquals("Engineering", result.getName());
    }

    @Test
    void testGetDepartmentById_NotFound() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.getDepartmentById(1L));
    }

    @Test
    void testUpdateDepartment_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByName("Engineering")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);
        when(dtoMapper.toDepartmentDto(department)).thenReturn(departmentDto);

        DepartmentDto result = departmentService.updateDepartment(1L, createDto);

        assertEquals("Engineering", result.getName());
    }

    @Test
    void testUpdateDepartment_NotFound() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.updateDepartment(1L, createDto));
    }

    @Test
    void testUpdateDepartment_DuplicateName() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByName("Engineering")).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> departmentService.updateDepartment(1L, createDto));
    }

    @Test
    void testDeleteDepartment_Success() {
        when(departmentRepository.existsById(1L)).thenReturn(true);

        departmentService.deleteDepartment(1L);

        verify(departmentRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteDepartment_NotFound() {
        when(departmentRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.deleteDepartment(1L));
    }
}

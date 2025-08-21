package com.team2.university_room_booking.service.impl;

import com.team2.university_room_booking.dto.request.CreateDepartmentDto;
import com.team2.university_room_booking.dto.response.DepartmentDto;
import com.team2.university_room_booking.exceptions.BadRequestException;
import com.team2.university_room_booking.exceptions.ResourceNotFoundException;
import com.team2.university_room_booking.mapper.DtoMapper;
import com.team2.university_room_booking.model.Department;
import com.team2.university_room_booking.repository.DepartmentRepository;
import com.team2.university_room_booking.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DtoMapper dtoMapper;

    @Transactional
    @Override
    public DepartmentDto createDepartment(CreateDepartmentDto dto) {
        Department department = dtoMapper.toDepartmentEntity(dto);
        if (department.getName() == null || department.getName().isEmpty()) {
            throw new BadRequestException("Department name cannot be empty");
        }
        if (departmentRepository.existsByName(department.getName())) {
            throw new BadRequestException("Department with this name already exists");
        }
        return dtoMapper.toDepartmentDto(departmentRepository.save(department));
    }

    @Override
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(dtoMapper::toDepartmentDto)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentDto getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .map(dtoMapper::toDepartmentDto)
                .orElseThrow(() -> new RuntimeException("Department not found"));
    }

    // Update
    @Override
    @Transactional
    public DepartmentDto updateDepartment(Long id, CreateDepartmentDto dto) {
        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        if (dto.getName() == null || dto.getName().isEmpty()) {
            throw new BadRequestException("Department name cannot be empty");
        }
        if (departmentRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Department with this name already exists");
        }
        existing.setName(dto.getName());
        return dtoMapper.toDepartmentDto(departmentRepository.save(existing));
    }

    // Delete
    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }
        // Check if department has any bookings{
        //
        // }
        departmentRepository.deleteById(id);
    }
}

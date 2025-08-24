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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DtoMapper dtoMapper;

    @Transactional
    @Override
    public DepartmentDto createDepartment(CreateDepartmentDto dto) {
        log.info("Creating department with name: {}", dto.getName());

        Department department = dtoMapper.toDepartmentEntity(dto);

        if (department.getName() == null || department.getName().isEmpty()) {
            log.error("Department name is empty");
            throw new BadRequestException("Department name cannot be empty");
        }

        if (departmentRepository.existsByName(department.getName())) {
            log.error("Department with name {} already exists", department.getName());
            throw new BadRequestException("Department with this name already exists");
        }

        Department saved = departmentRepository.save(department);
        log.info("Department created successfully with id {}", saved.getId());

        return dtoMapper.toDepartmentDto(saved);
    }

    @Override
    public List<DepartmentDto> getAllDepartments() {
        log.info("Fetching all departments");
        return departmentRepository.findAll()
                .stream()
                .map(dtoMapper::toDepartmentDto)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentDto getDepartmentById(Long id) {
        log.info("Fetching department with id {}", id);
        return departmentRepository.findById(id)
                .map(dtoMapper::toDepartmentDto)
                .orElseThrow(() -> {
                    log.error("Department not found with id {}", id);
                    return new ResourceNotFoundException("Department not found with id: " + id);
                });
    }

    @Override
    @Transactional
    public DepartmentDto updateDepartment(Long id, CreateDepartmentDto dto) {
        log.info("Updating department with id {}", id);

        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Department not found with id {}", id);
                    return new ResourceNotFoundException("Department not found with id: " + id);
                });

        if (dto.getName() == null || dto.getName().isEmpty()) {
            log.error("Department name is empty during update");
            throw new BadRequestException("Department name cannot be empty");
        }

        if (departmentRepository.existsByName(dto.getName())) {
            log.error("Department with name {} already exists", dto.getName());
            throw new BadRequestException("Department with this name already exists");
        }

        existing.setName(dto.getName());
        Department updated = departmentRepository.save(existing);

        log.info("Department updated successfully with id {}", updated.getId());
        return dtoMapper.toDepartmentDto(updated);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        log.warn("Deleting department with id {}", id);

        if (!departmentRepository.existsById(id)) {
            log.error("Department not found with id {}", id);
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }

        departmentRepository.deleteById(id);
        log.info("Department deleted successfully with id {}", id);
    }
}

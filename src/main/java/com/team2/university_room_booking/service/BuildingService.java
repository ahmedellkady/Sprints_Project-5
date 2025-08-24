package com.team2.university_room_booking.service;

import com.team2.university_room_booking.dto.request.CreateBuildingDto;
import com.team2.university_room_booking.dto.response.BuildingDto;
import com.team2.university_room_booking.exceptions.BadRequestException;
import com.team2.university_room_booking.exceptions.ResourceNotFoundException;
import com.team2.university_room_booking.mapper.DtoMapper;
import com.team2.university_room_booking.model.Building;
import com.team2.university_room_booking.model.Department;
import com.team2.university_room_booking.repository.BuildingRepository;
import com.team2.university_room_booking.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final DepartmentRepository departmentRepository;
    private final DtoMapper dtoMapper;

    @Transactional
    public BuildingDto createBuilding(CreateBuildingDto dto) {
        log.info("Creating new building with name: {}", dto.getName());
        Building building = dtoMapper.toBuildingEntity(dto);
        building.setId(null); // Ensure the ID is null for new entities

        if (dto.getDepartmentId() != null) {
            log.debug("Fetching department with id {}", dto.getDepartmentId());
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> {
                        log.error("Department not found with id {}", dto.getDepartmentId());
                        return new ResourceNotFoundException("Department not found with id " + dto.getDepartmentId());
                    });
            building.setDepartment(department);
        }

        Building saved = buildingRepository.save(building);
        log.info("Building created successfully with id {}", saved.getId());
        return dtoMapper.toBuildingDto(saved);
    }

    public List<BuildingDto> getAllBuildings() {
        log.info("Fetching all buildings");
        return buildingRepository.findAll().stream()
                .map(dtoMapper::toBuildingDto)
                .toList();
    }

    public BuildingDto getBuildingById(Long id) {
        log.info("Fetching building with id {}", id);
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Building not found with id {}", id);
                    return new ResourceNotFoundException("Building not found with id " + id);
                });
        return dtoMapper.toBuildingDto(building);
    }

    @Transactional
    public BuildingDto updateBuilding(Long id, CreateBuildingDto dto) {
        log.info("Updating building with id {}", id);
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Building not found with id {}", id);
                    return new ResourceNotFoundException("Building not found with id " + id);
                });

        building.setName(dto.getName());

        if (dto.getDepartmentId() != null) {
            log.debug("Updating department for building {} to department id {}", id, dto.getDepartmentId());
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> {
                        log.error("Department not found with id {}", dto.getDepartmentId());
                        return new ResourceNotFoundException("Department not found with id " + dto.getDepartmentId());
                    });
            building.setDepartment(department);
        }

        Building updated = buildingRepository.save(building);
        log.info("Building updated successfully with id {}", updated.getId());
        return dtoMapper.toBuildingDto(updated);
    }

    @Transactional
    public void deleteBuilding(Long id) {
        log.warn("Deleting building with id {}", id);
        if (!buildingRepository.existsById(id)) {
            log.error("Building not found with id {}", id);
            throw new ResourceNotFoundException("Building not found with id " + id);
        }
        if (!buildingRepository.findById(id).orElseThrow().getRooms().isEmpty()) {
            log.error("Cannot delete building with id {} because it has associated rooms", id);
            throw new BadRequestException("Cannot delete building with associated rooms");
        }
        buildingRepository.deleteById(id);
        log.info("Building deleted successfully with id {}", id);
    }
}

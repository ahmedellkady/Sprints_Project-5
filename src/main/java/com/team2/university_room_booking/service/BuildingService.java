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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final DepartmentRepository departmentRepository;
    private final DtoMapper dtoMapper;

    @Transactional
    public BuildingDto createBuilding(CreateBuildingDto dto) {
        Building building = dtoMapper.toBuildingEntity(dto);
        building.setId(null); // Ensure the ID is null for new entities
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id " + dto.getDepartmentId()));
            building.setDepartment(department);
        }

        return dtoMapper.toBuildingDto(buildingRepository.save(building));
    }


    public List<BuildingDto> getAllBuildings() {
        return buildingRepository.findAll().stream()
                .map(dtoMapper::toBuildingDto)
                .toList();
    }


    public BuildingDto getBuildingById(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id " + id));
        return dtoMapper.toBuildingDto(building);
    }

    @Transactional
    public BuildingDto updateBuilding(Long id, CreateBuildingDto dto) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id " + id));

        building.setName(dto.getName());

        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id " + dto.getDepartmentId()));
            building.setDepartment(department);
        }

        return dtoMapper.toBuildingDto(buildingRepository.save(building));
    }

    @Transactional
    public void deleteBuilding(Long id) {
        if (!buildingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Building not found with id " + id);
        }
        // Check if the building has any associated rooms before deleting
        if (!buildingRepository.findById(id).orElseThrow().getRooms().isEmpty()) {
            throw new BadRequestException("Cannot delete building with associated rooms");
        }
        buildingRepository.deleteById(id);
    }
}

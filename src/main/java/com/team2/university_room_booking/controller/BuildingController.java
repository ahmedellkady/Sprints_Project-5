package com.team2.university_room_booking.controller;

import com.team2.university_room_booking.dto.request.CreateBuildingDto;
import com.team2.university_room_booking.dto.response.BuildingDto;
import com.team2.university_room_booking.service.BuildingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BuildingDto> createBuilding(@Valid @RequestBody CreateBuildingDto dto) {
        return ResponseEntity.ok(buildingService.createBuilding(dto));
    }

    @GetMapping
    public ResponseEntity<List<BuildingDto>> getAllBuildings() {
        return ResponseEntity.ok(buildingService.getAllBuildings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BuildingDto> getBuildingById(@PathVariable Long id) {
        return ResponseEntity.ok(buildingService.getBuildingById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BuildingDto> updateBuilding(@PathVariable Long id, @Valid @RequestBody CreateBuildingDto dto) {
        return ResponseEntity.ok(buildingService.updateBuilding(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBuilding(@PathVariable Long id) {
        buildingService.deleteBuilding(id);
        return ResponseEntity.noContent().build();
    }
}

package com.team2.university_room_booking.service;

import com.team2.university_room_booking.dto.request.CreateRoomFeatureDto;
import com.team2.university_room_booking.dto.response.RoomFeatureDto;
import com.team2.university_room_booking.exceptions.BadRequestException;
import com.team2.university_room_booking.exceptions.ResourceNotFoundException;
import com.team2.university_room_booking.mapper.DtoMapper;
import com.team2.university_room_booking.model.RoomFeature;
import com.team2.university_room_booking.repository.RoomFeatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomFeatureService {

    private final RoomFeatureRepository repository;
    private final DtoMapper dtoMapper;

    public RoomFeatureDto create(CreateRoomFeatureDto dto) {
        if (dto.getName() == null || dto.getName().isEmpty()) {
            throw new BadRequestException("Feature name cannot be null or empty");
        }
        RoomFeature saved = repository.save(dtoMapper.toRoomFeatureEntity(dto));
        return dtoMapper.toRoomFeatureDto(saved);
    }

    public List<RoomFeatureDto> getAll() {
        return repository.findAll().stream()
                .map(dtoMapper::toRoomFeatureDto)
                .collect(Collectors.toList());
    }

    public RoomFeatureDto getById(Long id) {
        RoomFeature feature = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feature not found"));
        return dtoMapper.toRoomFeatureDto(feature);
    }

    public RoomFeatureDto update(Long id, RoomFeatureDto dto) {
        RoomFeature feature = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found"));
        feature.setName(dto.getName());
        RoomFeature updated = repository.save(feature);
        return dtoMapper.toRoomFeatureDto(updated);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Feature not found with id: " + id);
        }
        // Check if the feature is used in any room
        if (repository.countRoomsWithFeature(id) > 0) {
            throw new BadRequestException("Cannot delete feature that is in use by rooms");
        }
        repository.deleteById(id);
    }
}

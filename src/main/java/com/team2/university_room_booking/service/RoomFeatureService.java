package com.team2.university_room_booking.service;

import com.team2.university_room_booking.dto.request.CreateRoomFeatureDto;
import com.team2.university_room_booking.dto.response.RoomFeatureDto;
import com.team2.university_room_booking.exceptions.BadRequestException;
import com.team2.university_room_booking.exceptions.ResourceNotFoundException;
import com.team2.university_room_booking.mapper.DtoMapper;
import com.team2.university_room_booking.model.RoomFeature;
import com.team2.university_room_booking.repository.RoomFeatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomFeatureService {

    private final RoomFeatureRepository repository;
    private final DtoMapper dtoMapper;

    public RoomFeatureDto create(CreateRoomFeatureDto dto) {
        log.info("Creating RoomFeature with name={}", dto.getName());

        if (dto.getName() == null || dto.getName().isEmpty()) {
            log.error("Failed to create RoomFeature: name is null or empty");
            throw new BadRequestException("Feature name cannot be null or empty");
        }

        RoomFeature saved = repository.save(dtoMapper.toRoomFeatureEntity(dto));
        RoomFeatureDto response = dtoMapper.toRoomFeatureDto(saved);

        log.info("RoomFeature created successfully with id={}", response.getId());
        return response;
    }

    public List<RoomFeatureDto> getAll() {
        log.info("Fetching all RoomFeatures");
        List<RoomFeatureDto> features = repository.findAll().stream()
                .map(dtoMapper::toRoomFeatureDto)
                .collect(Collectors.toList());
        log.info("Found {} RoomFeatures", features.size());
        return features;
    }

    public RoomFeatureDto getById(Long id) {
        log.info("Fetching RoomFeature by id={}", id);

        RoomFeature feature = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("RoomFeature with id={} not found", id);
                    return new RuntimeException("Feature not found");
                });

        log.info("RoomFeature found: id={}, name={}", feature.getId(), feature.getName());
        return dtoMapper.toRoomFeatureDto(feature);
    }

    public RoomFeatureDto update(Long id, RoomFeatureDto dto) {
        log.info("Updating RoomFeature with id={}", id);

        RoomFeature feature = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("RoomFeature with id={} not found for update", id);
                    return new ResourceNotFoundException("Feature not found");
                });

        feature.setName(dto.getName());
        RoomFeature updated = repository.save(feature);
        RoomFeatureDto response = dtoMapper.toRoomFeatureDto(updated);

        log.info("RoomFeature updated successfully: id={}, newName={}", response.getId(), response.getName());
        return response;
    }

    public void delete(Long id) {
        log.info("Deleting RoomFeature with id={}", id);

        if (!repository.existsById(id)) {
            log.error("Cannot delete RoomFeature: id={} does not exist", id);
            throw new ResourceNotFoundException("Feature not found with id: " + id);
        }

        if (repository.countRoomsWithFeature(id) > 0) {
            log.error("Cannot delete RoomFeature id={} because it is used by rooms", id);
            throw new BadRequestException("Cannot delete feature that is in use by rooms");
        }

        repository.deleteById(id);
        log.info("RoomFeature deleted successfully with id={}", id);
    }
}

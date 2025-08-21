package com.team2.university_room_booking.dto.response;

import com.team2.university_room_booking.enums.RoomType;
import com.team2.university_room_booking.model.RoomFeature;
import lombok.*;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDto {
    private Long id;
    private String name;
    private RoomType type;
    private int capacity;
    private boolean available;
    private String buildingName;
    private Set<String> features; // return names of features instead of IDs

    public void setFeaturesFromEntities(Set<RoomFeature> roomFeatures) {
        this.features = roomFeatures.stream()
                .map(RoomFeature::getName)
                .collect(Collectors.toSet());
    }
}

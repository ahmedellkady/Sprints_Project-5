package com.team2.university_room_booking.service.policy;

import java.time.Duration;

public class BookingPolicy {

    public static final Duration MAX_HORIZON = Duration.ofDays(90);
    public static final Duration MIN_DURATION = Duration.ofHours(1);
    public static final Duration MAX_DURATION = Duration.ofHours(4);
}

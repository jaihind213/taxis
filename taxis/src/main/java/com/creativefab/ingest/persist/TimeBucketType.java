package com.creativefab.ingest.persist;

import lombok.Getter;

public enum TimeBucketType {
    DAILY(24*60), HOURLY(60), HALF_HOURLY(30), FIFTEEN_MIN(15);

    @Getter
    private final int durationMin;

    TimeBucketType(int durationMin) {
        this.durationMin = durationMin;
    }

    public static TimeBucketType from(String cadence){
        return TimeBucketType.valueOf(cadence.toUpperCase());
    }
}

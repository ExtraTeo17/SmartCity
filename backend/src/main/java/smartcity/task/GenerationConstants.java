package smartcity.task;

import smartcity.TimeProvider;

import java.util.concurrent.TimeUnit;

class GenerationConstants {
    static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
    static final int SIMULATION_CONTROL_INTERVAL = TimeProvider.MS_PER_TICK;

    static final int CREATE_CAR_INTERVAL = 120;
    static final int CREATE_CAR_INTERVAL_FOR_BATCHES = 520;
    static final int FIXED_CAR_SEED = 175;

    static final int CREATE_BIKE_INTERVAL = 500;
    static final int FIXED_BIKE_SEED = 11;

    static final int CREATE_PED_INTERVAL = 120;
    static final int FIXED_PED_SEED = 29;

    static final int BUS_CONTROL_INTERVAL = 2000;

    static final int ZONE_ADJUSTMENT = 20;
}

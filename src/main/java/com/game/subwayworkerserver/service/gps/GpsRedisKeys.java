package com.game.subwayworkerserver.service.gps;

public class GpsRedisKeys {

    private GpsRedisKeys() {}

    public static final String COWORKER_LOCATIONS_KEY = "coworker:locations";
    public static final String RANDOM_GIFT_BOX_KEY = "giftbox:locations";
    public static final String COWORKER_DATA_PREFIX = "coworker:data:";
    public static final String USER_ACQUIRED_PREFIX = "user:acquired:";
    public static final String TRAPPED_COWORKER_DATA_PREFIX = "trapped:coworker:data:";
    public static final String VISITED_GRID_KEY = "map:visited:grids";
    public static final String GRID_LOCK_PREFIX = "lock:grid:";
}

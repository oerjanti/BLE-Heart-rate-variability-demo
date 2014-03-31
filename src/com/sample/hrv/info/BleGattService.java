package com.sample.hrv.info;

import java.util.HashMap;

/**
 * Created by steven on 10/7/13.
 * Modified by olli on 3/28/2014.
 */
public class BleGattService extends BleInfoService {

    private static final String UUID_SERVICE = "00001801-0000-1000-8000-00805f9b34fb";

    private static final String UUID_DEVICE_NAME = "00002a05-0000-1000-8000-00805f9b34fb";

    private static final HashMap<String, String> CHARACTERISTIC_MAP = new HashMap<String, String>();

    static {
        CHARACTERISTIC_MAP.put(UUID_DEVICE_NAME, "Service Changed");
    }

    @Override
    public String getUUID() {
        return UUID_SERVICE;
    }

    @Override
    public String getName() {
        return "GATT Service";
    }

    @Override
    public String getCharacteristicName(String uuid) {
        if (!CHARACTERISTIC_MAP.containsKey(uuid))
            return "Unknown";
        return CHARACTERISTIC_MAP.get(uuid);
    }
}

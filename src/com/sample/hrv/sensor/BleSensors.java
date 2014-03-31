package com.sample.hrv.sensor;

import java.util.HashMap;

/**
 * Created by steven on 9/4/13.
 * Modified by olli on 3/28/2014.
 */
public class BleSensors {

    private static HashMap<String, BleSensor<?>> SENSORS = new HashMap<String, BleSensor<?>>();

    static {
        final BleTestSensor testSensor = new BleTestSensor();
        final BleHeartRateSensor heartRateSensor = new BleHeartRateSensor();

        SENSORS.put(testSensor.getServiceUUID(), testSensor);
        SENSORS.put(heartRateSensor.getServiceUUID(), heartRateSensor);
    }

    public static BleSensor<?> getSensor(String uuid) {
        return SENSORS.get(uuid);
    }
}

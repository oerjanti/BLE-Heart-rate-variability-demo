package com.sample.hrv.info;

import java.util.HashMap;

/**
 * Created by steven on 10/7/13.
 * Modified by olli on 3/28/2014.
 */
public class BleInfoServices {

    private static HashMap<String, BleInfoService> SERVICES = new HashMap<String, BleInfoService>();

    static {
        final BleGattService gapSerivce = new BleGattService();
        final BleGapService gattSerivce = new BleGapService();
        final BleDeviceInfoService deviceInfoSerivce = new BleDeviceInfoService();

        SERVICES.put(gapSerivce.getUUID(), gapSerivce);
        SERVICES.put(gattSerivce.getUUID(), gattSerivce);
        SERVICES.put(deviceInfoSerivce.getUUID(), deviceInfoSerivce);
    }

    public static BleInfoService getService(String uuid) {
        return SERVICES.get(uuid);
    }
}

package com.sample.hrv.info;

/**
 * Created by steven on 10/7/13.
 */
public abstract class BleInfoService {
    private final static String TAG = BleInfoService.class.getSimpleName();

    protected BleInfoService() {
    }

    public abstract String getUUID();

    public abstract String getName();

    public abstract String getCharacteristicName(String uuid);
}

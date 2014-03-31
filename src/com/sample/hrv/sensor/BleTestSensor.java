package com.sample.hrv.sensor;

import com.sample.hrv.BluetoothGattExecutor;

import android.bluetooth.BluetoothGattCharacteristic;


/**
 * Created by steven on 9/3/13. 
 * Modified by olli on 3/28/2014.
 */
public class BleTestSensor extends BleSensor<Void> {

    BleTestSensor() {
        super();
    }

    @Override
    public String getName() {
        return "Test";
    }

    @Override
    public String getServiceUUID() {
        return "f000aa60-0451-4000-b000-000000000000";
    }

    @Override
    public String getDataUUID() {
        return "f000aa61-0451-4000-b000-000000000000";
    }

    @Override
    public String getConfigUUID() {
        return "f000aa62-0451-4000-b000-000000000000";
    }

    @Override
    public String getDataString() {
        return "";
    }

    @Override
    public BluetoothGattExecutor.ServiceAction[] enable(boolean enable) {
        return new BluetoothGattExecutor.ServiceAction[0];
    }

    @Override
    public BluetoothGattExecutor.ServiceAction notify(boolean start) {
        return BluetoothGattExecutor.ServiceAction.NULL;
    }

    @Override
    public Void parse(BluetoothGattCharacteristic c) {
        //TODO: implement method
        return null;
    }
}

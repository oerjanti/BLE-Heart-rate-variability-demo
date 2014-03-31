/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sample.hrv;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.List;


import com.sample.hrv.R;
import com.sample.hrv.adapters.BleServicesAdapter;
import com.sample.hrv.adapters.BleServicesAdapter.OnServiceItemClickListener;
import com.sample.hrv.demo.DemoHeartRateSensorActivity;
import com.sample.hrv.demo.DemoSensorActivity;
import com.sample.hrv.sensor.BleHeartRateSensor;
import com.sample.hrv.sensor.BleSensor;
import com.sample.hrv.sensor.BleSensors;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BleService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceServicesActivity extends Activity {
    private final static String TAG = DeviceServicesActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView connectionState;
    private TextView dataField;
    private TextView heartRateField;
    private TextView intervalField;
    private Button demoButton;
    
    private ExpandableListView gattServicesList;
    private BleServicesAdapter gattServiceAdapter;

    private String deviceName;
    private String deviceAddress;
    private BleService bleService;
    private boolean isConnected = false;

    private BleSensor<?> activeSensor;
    private BleSensor<?> heartRateSensor;
    
	private OnServiceItemClickListener serviceListener;

    // Code to manage Service lifecycle.
    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bleService = ((BleService.LocalBinder) service).getService();
            if (!bleService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            bleService.connect(deviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bleService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BleService.ACTION_GATT_CONNECTED.equals(action)) {
                isConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(bleService.getSupportedGattServices());
				enableHeartRateSensor();
            } else if (BleService.ACTION_DATA_AVAILABLE.equals(action)) {
				displayData(intent.getStringExtra(BleService.EXTRA_SERVICE_UUID), intent.getStringExtra(BleService.EXTRA_TEXT));

            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (gattServiceAdapter == null)
                        return false;

                    final BluetoothGattCharacteristic characteristic = gattServiceAdapter.getChild(groupPosition, childPosition);
                    final BleSensor<?> sensor = BleSensors.getSensor(characteristic.getService().getUuid().toString());

                    if (activeSensor != null)
                        bleService.enableSensor(activeSensor, false);

                    if (sensor == null) {
                        bleService.readCharacteristic(characteristic);
                        return true;
                    }

                    if (sensor == activeSensor)
                        return true;

                    activeSensor = sensor;
                    bleService.enableSensor(sensor, true);
                    return true;
                }
            };

    private final BleServicesAdapter.OnServiceItemClickListener demoClickListener = new BleServicesAdapter.OnServiceItemClickListener() {
        @Override
        public void onDemoClick(BluetoothGattService service) {
        	Log.d(TAG, "onDemoClick: service" +service.getUuid().toString());
            final BleSensor<?> sensor = BleSensors.getSensor(service.getUuid().toString());
            if (sensor == null)
                return;

            final Class<? extends DemoSensorActivity> demoClass;
            if (sensor instanceof BleHeartRateSensor)
                demoClass = DemoHeartRateSensorActivity.class;
            else
                return;

            final Intent demoIntent = new Intent();
            demoIntent.setClass(DeviceServicesActivity.this, demoClass);
            demoIntent.putExtra(DemoSensorActivity.EXTRAS_DEVICE_ADDRESS, deviceAddress);
            demoIntent.putExtra(DemoSensorActivity.EXTRAS_SENSOR_UUID, service.getUuid().toString());
            startActivity(demoIntent);
        }

        @Override
        public void onServiceEnabled(BluetoothGattService service, boolean enabled) {
            if (gattServiceAdapter == null)
                return;

            final BleSensor<?> sensor = BleSensors.getSensor(service.getUuid().toString());
            if (sensor == null)
                return;

            if (sensor == activeSensor)
                return;

            if (activeSensor != null)
                bleService.enableSensor(activeSensor, false);
            activeSensor = sensor;
            bleService.enableSensor(sensor, true);
        }

        @Override
        public void onServiceUpdated(BluetoothGattService service) {
            final BleSensor<?> sensor = BleSensors.getSensor(service.getUuid().toString());
            if (sensor == null)
                return;

            bleService.updateSensor(sensor);
        }
    };

    private void clearUI() {
        gattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        dataField.setText(R.string.no_data);
		heartRateField.setText(R.string.no_data);
		intervalField.setText(R.string.no_data);
    }

	public void setServiceListener(OnServiceItemClickListener listener) {
		this.serviceListener = listener;
	}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(deviceAddress);
        gattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        gattServicesList.setOnChildClickListener(servicesListClickListner);
        connectionState = (TextView) findViewById(R.id.connection_state);
        dataField = (TextView) findViewById(R.id.data_value);
		heartRateField = (TextView) findViewById(R.id.heartrate_value);

		demoButton = (Button) findViewById(R.id.demo);

		demoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick serviceListener: "+serviceListener);
				if (serviceListener == null)
					return;
				final BluetoothGattService service = gattServiceAdapter.getHeartRateService();
				serviceListener.onDemoClick(service);
				Log.d(TAG, "set service listener");
			}
		});

		demoButton.setVisibility(View.VISIBLE);
        
        getActionBar().setTitle(deviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        final Intent gattServiceIntent = new Intent(this, BleService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bleService != null) {
            final boolean result = bleService.connect(deviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        bleService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (isConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                bleService.connect(deviceAddress);
                return true;
            case R.id.menu_disconnect:
                bleService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String uuid, String data) {
		if (data != null) {
			if (uuid.equals(BleHeartRateSensor.getServiceUUIDString())) {
				heartRateField.setText(data);
			} else {
				dataField.setText(data);
			}
		}
    }

	private boolean enableHeartRateSensor() {
		if (gattServiceAdapter == null)
			return false;

		final BluetoothGattCharacteristic characteristic = gattServiceAdapter
				.getHeartRateCharacteristic();
		Log.d(TAG,"characteristic: " + characteristic);
		final BleSensor<?> sensor = BleSensors.getSensor(characteristic
				.getService()
				.getUuid()
				.toString());

		if (heartRateSensor != null)
			bleService.enableSensor(heartRateSensor, false);

		if (sensor == null) {
			bleService.readCharacteristic(characteristic);
			return true;
		}

		if (sensor == heartRateSensor)
			return true;

		heartRateSensor = sensor;
		bleService.enableSensor(sensor, true);
		
        this.setServiceListener(demoClickListener);

		return true;
	}
	
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;

        gattServiceAdapter = new BleServicesAdapter(this, gattServices);
        gattServiceAdapter.setServiceListener(demoClickListener);
        gattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}

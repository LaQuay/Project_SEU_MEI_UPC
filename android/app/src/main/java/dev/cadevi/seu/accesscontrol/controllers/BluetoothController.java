package dev.cadevi.seu.accesscontrol.controllers;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BluetoothController {
    public static final int REQUEST_ENABLE_BT = 288;
    private static final String TAG = BluetoothController.class.getSimpleName();
    private static BluetoothController instance;
    private final String pepitoBLE = "00:15:83:00:8B:4D";
    private final UUID pepitoUUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private final UUID characteristicUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;

    private ReadReceived readReceivedCallback;

    // Device connect call back
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            bluetoothGatt = gatt;
            switch (newState) {
                case 0:
                    Log.e(TAG, "Device Disconnected");
                    break;
                case 2:
                    Log.e(TAG, "Device Connected: "
                            + bluetoothGatt.getDevice().getName() + ", " + bluetoothGatt.getDevice().getAddress());

                    // discover services and characteristics for this device
                    bluetoothGatt.discoverServices();

                    break;
                default:
                    Log.e(TAG, "Unknown state");

                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "onServicesDiscovered - Success: GATT_SUCCESS");
                bluetoothGattCharacteristic =
                        bluetoothGatt.getService(pepitoUUID).getCharacteristic(characteristicUUID);

                // Enable the client notification for READING
                gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);

                Log.e(TAG, "Characteristic available");
            } else {
                Log.e(TAG, "onServicesDiscovered - Fail: " + status);
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            Log.e(TAG, "onCharacteristicRead, charact: " + characteristic.getUuid() + " status: "
                    + status + ", value: " + Arrays.toString(characteristic.getValue()));
            if (status == BluetoothGatt.GATT_SUCCESS) {
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            Log.e(TAG, "onCharacteristicWrite, charact: " + characteristic.getUuid() + " status: "
                    + status + ", value: " + Arrays.toString(characteristic.getValue()));
            if (status == BluetoothGatt.GATT_SUCCESS) {
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, "onCharacteristicChanged, value: " + Arrays.toString(characteristic.getValue()));

            readData(characteristic.getValue());
        }
    };
    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;
    private Handler mHandler = new Handler();
    private boolean connectingToBluetoothDevice;
    //result.getScanRecord().getServiceUuids() to get UUID
    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.e(TAG, "onScanResult: " + result.getDevice().getName() + ", " + result.getDevice().getAddress());
            if (result.getDevice().getAddress().equals(pepitoBLE) && !connectingToBluetoothDevice) {
                Log.e(TAG, "Found, connecting " + result.getDevice().getName() + ", " + result.getDevice().getAddress());
                connectingToBluetoothDevice = true;
                result.getDevice().connectGatt(mContext, false, btleGattCallback);
            }
        }
    };

    private BluetoothController(Context context) {
        this.mContext = context;
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public static BluetoothController getInstance(Context context) {
        if (instance == null) {
            instance = new BluetoothController(context);
        }
        return instance;
    }

    public void startServices() {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) mContext).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            scanBTLEDevices();
        }
    }

    public void scanBTLEDevices() {
        Log.e(TAG, "scanBTLEDevices");
        final BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        int SCAN_PERIOD = 3000;
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner.stopScan(mLeScanCallback);
            }
        }, SCAN_PERIOD);

        connectingToBluetoothDevice = false;

        ScanFilter scanFilter = new ScanFilter.Builder()
                .setDeviceAddress(pepitoBLE)
                .build();
        List<ScanFilter> listScanFilter = new ArrayList<>();
        listScanFilter.add(scanFilter);
        ScanSettings settings = new ScanSettings.Builder().build();
        bluetoothLeScanner.startScan(listScanFilter, settings, mLeScanCallback);
    }

    public void stopScanBTLEDevices() {
        Log.e(TAG, "stopScanBTLEDevices");
        BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        connectingToBluetoothDevice = false;
        bluetoothLeScanner.stopScan(mLeScanCallback);
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    public void stopAll() {
        stopScanBTLEDevices();
    }

    public void readData(byte[] bytes) {
        try {
            String text = new String(bytes, "UTF-8");
            Log.e(TAG, "Reading data: " + text);
            readReceivedCallback.onReadReceived(text);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    // MTU max 23 Bytes
    public void sendData(String text) {
        byte[] value;
        try {
            value = text.getBytes("UTF-8");
            bluetoothGattCharacteristic.setValue(value);
            bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void setCallbackReadRequest(ReadReceived callbackReadRequest) {
        readReceivedCallback = callbackReadRequest;
    }

    public interface ReadReceived {
        void onReadReceived(String valueReceived);
    }
}

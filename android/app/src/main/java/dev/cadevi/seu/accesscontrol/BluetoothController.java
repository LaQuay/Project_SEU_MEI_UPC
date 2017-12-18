package dev.cadevi.seu.accesscontrol;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

//https://github.com/joelwass/Android-BLE-Connect-Example/blob/master/app/src/main/java/com/example/joelwasserman/androidbleconnectexample/MainActivity.java
public class BluetoothController {
    private static final String TAG = BluetoothController.class.getSimpleName();
    public static final int REQUEST_ENABLE_BT = 288;
    private final String pepitoBLE = "00:15:83:00:8B:4D";
    private static BluetoothController instance;
    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;
    private boolean mScanning;
    private Handler mHandler = new Handler();
    private boolean connectingToBluetoothDevice;
    BluetoothGatt bluetoothGatt;


    protected BluetoothController(Context context) {
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

    public void setListeners() {
    }

    public void startServices() {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        Log.e(TAG, "mBluetoothAdapter: " + mBluetoothAdapter + ", mBluetoothAdapter.isEnabled: " + mBluetoothAdapter.isEnabled());
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

        int SCAN_PERIOD = 5000;
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                bluetoothLeScanner.stopScan(mLeScanCallback);
            }
        }, SCAN_PERIOD);

        mScanning = true;
        connectingToBluetoothDevice = false;
        bluetoothLeScanner.startScan(mLeScanCallback);
    }

    public void stopScanBTLEDevices() {
        Log.e(TAG, "stopScanBTLEDevices");
        BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        mScanning = false;
        bluetoothLeScanner.stopScan(mLeScanCallback);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult()" + ", requestCode: " + requestCode + ", requestCode: " + requestCode + ", data: " + data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, data.getDataString());
            }
        }
    }

    public void stopAll() {
        stopScanBTLEDevices();
    }

    public void sendData(String text) {
    }

    private void broadcastUpdate(final BluetoothGattCharacteristic characteristic) {
        Log.e(TAG, "broadcastUpdate: " + characteristic.getUuid());
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.e(TAG, "onScanResult");
            Log.e(TAG, result.getDevice().getName() + ", " + result.getDevice().getAddress());
            if (result.getDevice().getAddress().equals(pepitoBLE)) {
                Log.e(TAG, "Found, connecting " + result.getDevice().getName() + ", " + result.getDevice().getAddress());
                connectingToBluetoothDevice = true;
                result.getDevice().connectGatt(mContext, false, btleGattCallback);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.e(TAG, "onBatchScanResults");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "onScanFailed");
        }
    };

    // Device connect call back
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            Log.e(TAG, "onCharacteristicChanged" + Arrays.toString(characteristic.getValue()));
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            bluetoothGatt = gatt;
            switch (newState) {
                case 0:
                    Log.e(TAG, "Device Disconnected");
                    break;
                case 2:
                    Log.e(TAG, "Device Connected");

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
            // this will get called after the client initiates a BluetoothGatt.discoverServices() call
            Log.e(TAG, "onServicesDiscovered" + status);
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.e(TAG, "onCharacteristicRead" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(characteristic);
            }
        }
    };
}

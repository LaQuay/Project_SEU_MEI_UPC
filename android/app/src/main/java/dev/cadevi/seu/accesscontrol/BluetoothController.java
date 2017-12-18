package dev.cadevi.seu.accesscontrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

//http://stackoverflow.com/questions/6732716/service-discovery-fail-bluetooth-chat-connection-using-spp
//http://stackoverflow.com/questions/3397071/service-discovery-failed-exception-using-bluetooth-on-android
public class BluetoothController {
    private static final String TAG = BluetoothController.class.getSimpleName();
    private static BluetoothController instance = null;
    private BluetoothSPP bt;
    private Context mContext;

    protected BluetoothController(Context context) {
        this.mContext = context;

        bt = new BluetoothSPP(context);
    }

    public static BluetoothController getInstance(Context context) {
        if (instance == null) {
            instance = new BluetoothController(context);
        }
        return instance;
    }

    public void setListeners() {
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                // Do something when data incoming
                Log.e(TAG, "Rebut: " + message);
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                // Do something when successfully connected
                Log.e(TAG, "onDeviceConnected(), name: " + name + ", address: " + address);
            }

            public void onDeviceDisconnected() {
                // Do something when connection was disconnected
                Log.e(TAG, "onDeviceDisconnected()");
            }

            public void onDeviceConnectionFailed() {
                // Do something when connection failed
                Log.e(TAG, "onDeviceConnectionFailed()");
            }
        });

        bt.setBluetoothStateListener(new BluetoothSPP.BluetoothStateListener() {
            public void onServiceStateChanged(int state) {
                if (state == BluetoothState.STATE_CONNECTED) {
                    // Do something when successfully connected
                    Log.e(TAG, "State changed to: STATE_CONNECTED");
                } else if (state == BluetoothState.STATE_CONNECTING) {
                    // Do something while connecting
                    Log.e(TAG, "State changed to: STATE_CONNECTING");
                } else if (state == BluetoothState.STATE_LISTEN) {
                    // Do something when device is waiting for connection
                    Log.e(TAG, "State changed to: STATE_LISTEN");
                } else if (state == BluetoothState.STATE_NONE) {
                    // Do something when device don't have any connection
                    Log.e(TAG, "State changed to: STATE_NONE");
                }
            }
        });
    }

    public void startServices() {
        if (!bt.isBluetoothEnabled() || !bt.isBluetoothAvailable()) {
            Toast.makeText(mContext, "Si us plau, engega el BT, BT no disponible", Toast.LENGTH_SHORT).show();
        } else {
            bt.setupService();
            bt.startService(BluetoothState.DEVICE_OTHER);

            Intent intent = new Intent(mContext.getApplicationContext(), DeviceList.class);
            ((Activity) mContext).startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult()" + ", requestCode: " + requestCode + ", resultCode: " + resultCode + ", data: " + data);
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK) {
                bt.connect(data);
            }
        }
    }

    public void stopAll() {
        instance = null;
        bt.stopService();
    }

    public void sendData(String text) {
        bt.send(text, true);
    }
}

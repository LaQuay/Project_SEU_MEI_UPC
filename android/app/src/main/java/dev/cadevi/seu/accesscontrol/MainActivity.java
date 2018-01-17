package dev.cadevi.seu.accesscontrol;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import dev.cadevi.seu.accesscontrol.controllers.BluetoothController;
import dev.cadevi.seu.accesscontrol.controllers.FirebaseUtils;
import dev.cadevi.seu.accesscontrol.controllers.VolleyController;

public class MainActivity extends AppCompatActivity implements BluetoothController.ReadReceived, BluetoothController.BluetoothStatus {
    private static final String TAG = MainActivity.class.getSimpleName();
    private BluetoothController bluetoothController;
    private TextView bluetoothStatusTextView;
    private TextView accessName;
    private TextView accessStatus;
    private Button mSendImageButton;
    private View mColorAccess;

    private BluetoothController.ReadReceived readReceivedCallback = this;
    private BluetoothController.BluetoothStatus bluetoothStatusCallback = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothStatusTextView = findViewById(R.id.tv_bluetoothStatus);
        accessName = findViewById(R.id.tv_access_name);
        accessStatus = findViewById(R.id.tv_access_status);
        mSendImageButton = findViewById(R.id.button_send);
        mColorAccess = findViewById(R.id.view_status);

        final int permissions = 4;
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                boolean allPermissionsChecked = report.getGrantedPermissionResponses().size() == permissions;

                if (allPermissionsChecked) {
                    startBluetooth();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

            }
        }).check();

        mSendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothController.getInstance(getApplicationContext()).sendData("HOLA");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopBluetooth();
    }

    private void startBluetooth() {
        bluetoothController = BluetoothController.getInstance(this);
        bluetoothController.startServices();
        bluetoothController.setCallbacks(bluetoothStatusCallback, readReceivedCallback);
    }

    private void stopBluetooth() {
        bluetoothController.stopAll();
    }

    @Override
    public void onReadReceived(String valueReceived) {
        String url = FirebaseUtils.getAccessForUser(valueReceived);
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());
                        try {
                            final String access = response.getString("acces");
                            final String userName = response.getString("name");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    accessName.setText(userName);

                                    if (access.equals("true")) {
                                        accessStatus.setText(getString(R.string.access_granted));
                                        mColorAccess.setBackgroundColor(getResources().getColor(R.color.colorAccessOK));
                                        BluetoothController.getInstance(getApplicationContext()).sendData("1");
                                    } else {
                                        accessStatus.setText(getString(R.string.access_denied));
                                        mColorAccess.setBackgroundColor(getResources().getColor(R.color.colorAccessFail));
                                        BluetoothController.getInstance(getApplicationContext()).sendData("0");
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );

        // add it to the RequestQueue
        VolleyController.getInstance(this).addToQueue(getRequest);
    }

    @Override
    public void onBluetoothChanged(int bluetoothStatus) {
        if (bluetoothStatusTextView != null) {
            String text = "...";

            switch (bluetoothStatus) {
                case BluetoothController.BLUETOOTH_CONNECTING:
                    text = getString(R.string.bluetooth_connecting);
                    break;
                case BluetoothController.BLUETOOTH_CONNECTED:
                    text = getString(R.string.bluetooth_connected);
                    break;
                case BluetoothController.BLUETOOTH_DISCONNECTED:
                    text = getString(R.string.bluetooth_disconnected);
                    break;
                case BluetoothController.BLUETOOTH_READY:
                    text = getString(R.string.bluetooth_ready);
                    break;
            }
            final String finalText = text;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bluetoothStatusTextView.setText(finalText);
                }
            });
        }
    }
}

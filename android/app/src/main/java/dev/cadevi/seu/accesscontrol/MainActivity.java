package dev.cadevi.seu.accesscontrol;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
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

import org.json.JSONObject;

import java.util.List;

import dev.cadevi.seu.accesscontrol.controllers.BluetoothController;
import dev.cadevi.seu.accesscontrol.controllers.FirebaseUtils;
import dev.cadevi.seu.accesscontrol.controllers.VolleyController;

public class MainActivity extends AppCompatActivity implements BluetoothController.ReadReceived {
    private static final String TAG = MainActivity.class.getSimpleName();
    private BluetoothController bluetoothController;
    private TextView mTextMessage;
    private Button mSendImageButton;

    private BluetoothController.ReadReceived readReceivedCallback = this;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = findViewById(R.id.message);
        mSendImageButton = findViewById(R.id.button_send);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

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
        bluetoothController.setCallbackReadRequest(readReceivedCallback);
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
                        // display response
                        Log.d("Response", response.toString());
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
}

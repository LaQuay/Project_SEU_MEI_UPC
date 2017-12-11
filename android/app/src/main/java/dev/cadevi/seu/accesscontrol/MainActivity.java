package dev.cadevi.seu.accesscontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private BluetoothController bluetoothController;
    private TextView mTextMessage;

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
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        startBluetooth();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopBluetooth();
    }

    private void startBluetooth() {
        bluetoothController = BluetoothController.getInstance(this);
        bluetoothController.startServices();
        bluetoothController.setListeners();
    }

    private void stopBluetooth() {
        bluetoothController.stopAll();
    }

    public void myOnActivityResult(int requestCode, int resultCode, Intent data) {
        bluetoothController.onActivityResult(requestCode, resultCode, data);
        //handler.postDelayed(testBT, 1000);
    }
}

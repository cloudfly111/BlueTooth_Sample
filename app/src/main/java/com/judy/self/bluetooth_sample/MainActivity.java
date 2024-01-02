package com.judy.self.bluetooth_sample;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.judy.self.bluetooth_sample.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private ActivityResultLauncher blueEnableLaucher;
    ActivityResultLauncher blueScanPermissionLaucher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("bIsEnable", true);
            getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).add(R.id.fragment_Container, MainFragment.class, bundle).commit();
        }
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if(bluetoothAdapter.isEnabled()){
            BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            scanLeDevice(bluetoothLeScanner);
            pairBLEDevice(bluetoothAdapter);
        }else{


        blueEnableLaucher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                    if (bluetoothAdapter.isEnabled()) {
                        BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                        scanLeDevice(bluetoothLeScanner);
                        pairBLEDevice(bluetoothAdapter);
                    }
                } else {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {

                        if (bluetoothAdapter.isEnabled()) {
                            BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                            scanLeDevice(bluetoothLeScanner);
                            pairBLEDevice(bluetoothAdapter);
                        }
                    }
                }
            }
        });
        }
        blueScanPermissionLaucher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean o) {
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        /*
         * 詢問是否允許開啟藍芽
         * (必須先通過 permission.BLUETOOTH_CONNECT 請求)
         * */
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.R){
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && !BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                blueEnableLaucher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            }else if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADMIN)!=PackageManager.PERMISSION_GRANTED){
                Log.i(TAG, "onRequestPermissionsResult: enable bluetooth and ACCESS_COARSE_LOCATION ");
            }
        }else{
            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                blueEnableLaucher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            }
        }

        Log.i(TAG, "onRequestPermissionsResult: " + requestCode);

    }

    public boolean scanning;
    Handler handler = new Handler();

    // Stops scanning after 10 seconds.
    final long SCAN_PERIOD = 10000;

    public ArrayList<String> deviceMACAddress = new ArrayList<String>();
    // Device scan callback.
    ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                    Log.i(TAG, "onBatchScanResults: " + results.size());
                }

                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if(!getSupportFragmentManager().getFragments().isEmpty()){
                        MainFragment mainFragment= (MainFragment) getSupportFragmentManager().getFragments().get(0);
                        mainFragment.scanStatus.onScan();
                    }
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    }
                    Log.i(TAG, "onScanResult: " + result.getDevice());
                    deviceMACAddress.add(result.getDevice().getAddress());

                }
            };

    //掃描周遭的藍芽裝置
    private void scanLeDevice(BluetoothLeScanner bluetoothLeScanner) {

        if (!scanning) {

            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                            blueScanPermissionLaucher.launch(android.Manifest.permission.BLUETOOTH_SCAN);
                            return;
                        }
                    }else{
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},300);
                            return;
                        }
                    }
                    bluetoothLeScanner.stopScan(leScanCallback);
                    Log.i(TAG, "run: ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED = "+(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED));
                    Log.i(TAG, "run: ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED = "+(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED));
                    Log.i(TAG, "run: stopScan 1");
                    MainFragment mainFragment= (MainFragment) getSupportFragmentManager().getFragments().get(0);
                    mainFragment.scanStatus.finishScan();
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);

            Log.i(TAG, "run: startScan 2");

        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
            Log.i(TAG, "run: stopScan");

        }
    }

    //加入藍芽配對功能，顯示已配對的裝置名稱清單(pairedList)
    private void pairBLEDevice(BluetoothAdapter bluetoothAdapter) {

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        }
        Set<BluetoothDevice> pairedDeviceSet = bluetoothAdapter.getBondedDevices();
        List<String> pairedList = new ArrayList<String>();
        for (BluetoothDevice d : pairedDeviceSet) {
            pairedList.add(d.getName());
        }
        Log.i(TAG, "pairBLEDevice: " + pairedList);
    }
}
package com.judy.self.bluetooth_sample;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.judy.self.bluetooth_sample.databinding.FragmentMainBinding;

import java.util.HashMap;
import java.util.Map;


public class MainFragment extends Fragment {
    private String TAG = "MainFragment";
    private FragmentMainBinding binding;
    //設定是否開啟藍芽功能
    private boolean bIsEnable = false;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(boolean bIsEnable) {
        MainFragment fragment = new MainFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("bIsEnable", bIsEnable);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle myBundle = getArguments();
        bIsEnable = myBundle.getBoolean("bIsEnable", false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        if (bIsEnable) {
            binding.textView.setText("Is Enabled");
            binding.Scanbutton.setVisibility(View.INVISIBLE);
        }
//      Android 官方手冊：  https://developer.android.com/develop/connectivity/bluetooth/setup?hl=zh-tw#java
        //1. 取得 BluetoothAdapter
        BluetoothManager bluetoothManager = getSystemService(getContext(), BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            binding.textView.setText("This device does not support bluetooth");
        }
        //2. 確認藍芽是否啟用
//        https://stackoverflow.com/questions/67722950/android-12-new-bluetooth-permissions
        if (!bluetoothAdapter.isEnabled()) {
            binding.textView.setText("This device does not enable bluetooth");

            /*
             * 詢問是否允許開啟藍芽
             * (必須先通過 permission.BLUETOOTH_CONNECT 請求)
             * */
            ActivityResultLauncher blueToothRequestLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {

                    if (o.getResultCode() == Activity.RESULT_OK) { //允許開啟藍芽
                        binding.textView.setText("User agree to enable bluetooth.");
                    } else if (o.getResultCode() == Activity.RESULT_CANCELED) { //不允許開啟藍芽
                        binding.textView.setText("User disagree to enable bluetooth.");
                    }

                }
            });

            /*
             * 詢問是否能與已配對的藍芽裝置連結
             * */
            ActivityResultLauncher blueConnectPermissionLaucher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean o) {
                    if (o) {
                        binding.textView.setText("get the permission for connecting to paired Bluetooth devices.");
                        blueToothRequestLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
                    }
                }
            });
            blueConnectPermissionLaucher.launch(Manifest.permission.BLUETOOTH_CONNECT);
        }

        //3. 掃描藍芽
        // source：https://developer.android.com/develop/connectivity/bluetooth/ble/find-ble-devices?hl=zh-tw
        //取得掃描權限後, 開始掃描周遭裝置
//      //注意：Android 11 以下 必須要取得 ACCESS_FINE_LOCATION 才能 掃描周遭裝置
        ActivityResultLauncher deviceLocationLaucher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean o) {
                binding.textView.setText("get the permission for device location");
            }
        });

        ActivityResultLauncher blueScanPermissionLaucher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean o) {
                if (o) {
                    binding.textView.setText("get the permission for scanning Bluetooth devices.");
                    deviceLocationLaucher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

                    //開始掃描周遭裝置
                    BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                    scanLeDevice(bluetoothLeScanner);

                    binding.Scanbutton.setVisibility(View.VISIBLE);
                    binding.Scanbutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            scanLeDevice(bluetoothLeScanner);
                        }
                    });
                }
            }
        });
        blueScanPermissionLaucher.launch(Manifest.permission.BLUETOOTH_SCAN);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityResultLauncher blueAdminPermissionLaucher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean o) {
                    if (o) {
                        binding.textView.setText("get the BLUETOOTH_ADMIN permission ");
                    }
                }
            });
            blueAdminPermissionLaucher.launch(Manifest.permission.BLUETOOTH_ADMIN);
        }


        checkPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION});

    }

    boolean scanning;
    Handler handler = new Handler();

    // Stops scanning after 10 seconds.
    final long SCAN_PERIOD = 20000;

    // Device scan callback.
    ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);

                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    binding.textView.setText("get the device: " + result.getDevice() + "[" + result.getDevice() + "]");

                    Log.i(TAG, "onScanResult: "+result.getDevice());
//                    leDeviceListAdapter.addDevice(result.getDevice());
//                    leDeviceListAdapter.notifyDataSetChanged();
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

                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        ActivityResultLauncher blueScanPermissionLaucher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                            @Override
                            public void onActivityResult(Boolean o) {
                                if (o) {
                                    binding.textView.setText("get the permission for scanning Bluetooth devices.");
                                }
                            }
                        });
                        blueScanPermissionLaucher.launch(Manifest.permission.BLUETOOTH_SCAN);
                        return;
                    }
                    bluetoothLeScanner.stopScan(leScanCallback);
                    Log.i(TAG, "run: stopScan 1");
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

    //確認是否取得權限
    private HashMap<String,Boolean> checkPermissions(String[] permissions){
        HashMap<String,Boolean> checkMap =new HashMap<String,Boolean>();

        for(String p : permissions){
            checkMap.put(p,ActivityCompat.checkSelfPermission(getContext(), p)== PackageManager.PERMISSION_GRANTED);
            Log.i(TAG, "checkPermissions: "+checkMap.get(p));
        }
        Log.i(TAG, "checkPermissions: "+checkMap);
        return checkMap;
    }
}
package com.judy.self.bluetooth_sample;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.judy.self.bluetooth_sample.databinding.FragmentMainBinding;


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
            /*
             * 詢問是否能與已配對的藍芽裝置連結
             * */
            //3. 掃描藍芽
            // source：https://developer.android.com/develop/connectivity/bluetooth/ble/find-ble-devices?hl=zh-tw
            //取得掃描權限後, 開始掃描周遭裝置
            //注意：Android 11 以下 必須要取得 ACCESS_FINE_LOCATION 才能 掃描周遭裝置
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADMIN}, 300);
        }

    }







}
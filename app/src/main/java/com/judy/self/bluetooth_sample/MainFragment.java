package com.judy.self.bluetooth_sample;

import static androidx.core.content.ContextCompat.checkSelfPermission;
import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Toast;

import com.judy.self.bluetooth_sample.databinding.FragmentMainBinding;

import java.security.Permission;
import java.util.ArrayList;


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
            boolean bluetoothLEAvailable = getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
            Log.i(TAG, "onViewCreated: bluetoothLEAvailable = "+bluetoothLEAvailable);
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
             * 開啟藍芽權限：
             *  1. 開啟藍芽功能前，需先通過 permission.BLUETOOTH_CONNECT 請求
             *  2. 需取得 permission.ACCESS_FINE_LOCATION 權限，才能掃描周遭藍芽裝置
             *  3. 取得掃描周遭藍芽裝置權限 permission.BLUETOOTH_SCAN
             *掃描藍芽 source：https://developer.android.com/develop/connectivity/bluetooth/ble/find-ble-devices?hl=zh-tw
             *取得掃描權限後, 開始掃描周遭裝置
             *注意：Android 11 以下 必須要取得 ACCESS_FINE_LOCATION 才能 掃描周遭裝置
             *
             */

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {

                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN}, 300);

            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN,}, 300);
            }
        }

    }
    /** 藍芽掃描確認 */
    IBlueToothScanStatus scanStatus=new IBlueToothScanStatus() {
        @Override
        public void startScan() {
            setStatus("start scan");
        }

        @Override
        public void onScan() {
            setStatus("scanning");
            setRefreshingIcon();
        }

        @Override
        public void finishScan() {
            setStatus("finish scan");
            clearRefreshingIcon();
            setDeviceNameList(((MainActivity)getActivity()).deviceMACAddress);
        }
    };

    /**
     * 依目前執行流程設定狀態文字
     */
    private void setStatus(String status){
        binding.textView.setText(status);
    }
    /**
     * 設定 refresh 動畫
     */
    private void setRefreshingIcon() {
        //設定圖片
        binding.refreshingImageview.setBackgroundResource(R.drawable.refresh_icon);
        //設定轉動動畫
        Animation refreshingAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anim_refreshing);
        refreshingAnim.setDuration(600);
        binding.refreshingImageview.startAnimation(refreshingAnim);
    }
    /**
     * 清除 refresh 動畫
     */
    private void clearRefreshingIcon() {
        binding.refreshingImageview.clearAnimation();
        binding.refreshingImageview.setVisibility(View.INVISIBLE);
    }

    private void showDialog(ArrayList<String> dataList){

    }
    private void setDeviceNameList(ArrayList<String> list){
        binding.DeviceListView.setAdapter(new DeviceListAdapter(getContext(),list));

    }
}
package com.judy.self.bluetooth_sample;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * BLE GATT 連線
 * */
public class BLEService extends Service {

    private Binder localBinder = new BLEBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    public boolean initialize(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter!=null;
    }

    /**
     * 建立 binder 取得連接相關服務
     * */
    class BLEBinder extends Binder {
        public BLEService getService(){
            return BLEService.this;
        }
    }
}

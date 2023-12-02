package com.judy.self.bluetooth_sample;

/**
 * 藍芽掃描狀態確認
 */
public interface IBlueToothScanStatus {
    /**開始藍芽掃描*/
    public void startScan();

    /**正在掃描周遭藍芽裝置*/
    public void onScan();

    /**完成藍芽掃描*/
    public void finishScan();
}

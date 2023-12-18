package com.judy.self.bluetooth_sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.judy.self.bluetooth_sample.databinding.ListItemBinding;

import java.util.ArrayList;

public class DeviceListAdapter extends BaseAdapter {

    private ArrayList<String> deviceList;
    private LayoutInflater layoutInflater;
    private ListItemBinding binding;

    public DeviceListAdapter(Context context, ArrayList<String> deviceList) {
        this.deviceList = deviceList;
        this.layoutInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return deviceList.isEmpty() ? 0 : deviceList.size();
    }

    @Override
    public Object getItem(int i) {
        return deviceList.isEmpty() ? "" : deviceList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        binding = ListItemBinding.inflate(layoutInflater,viewGroup,false);
        binding.textView.setText(deviceList.isEmpty() ? "" : deviceList.get(i));
        return binding.getRoot();
    }
}

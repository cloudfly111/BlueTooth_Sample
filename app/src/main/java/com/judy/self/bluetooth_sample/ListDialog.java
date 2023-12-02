package com.judy.self.bluetooth_sample;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.judy.self.bluetooth_sample.databinding.DialogListBinding;

/**
 * 顯示清單的 Dialog
 */
public class ListDialog extends BottomSheetDialog {
    private DialogListBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public ListDialog(@NonNull Context context) {
        super(context);
    }
}

package com.judy.self.bluetooth_sample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.judy.self.bluetooth_sample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("bIsEnable",true);
            getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).add(R.id.fragment_Container, MainFragment.class, bundle).commit();
        }

    }
}
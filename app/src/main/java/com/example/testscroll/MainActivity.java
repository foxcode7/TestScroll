package com.example.testscroll;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    public LinearLayout containerLayout;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        containerLayout = findViewById(R.id.container_layout);
        getSupportFragmentManager().beginTransaction().add(R.id.frame_layout, new MainFragment(), "").commitAllowingStateLoss();
    }
}
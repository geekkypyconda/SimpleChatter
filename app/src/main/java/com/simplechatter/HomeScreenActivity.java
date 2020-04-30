package com.simplechatter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

public class HomeScreenActivity extends AppCompatActivity {

    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_home_screen);
        toolbar = (Toolbar)findViewById(R.id.HomeScreenActivity_customToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Simple Chatter");
    }
}

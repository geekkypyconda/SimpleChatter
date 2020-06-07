package com.simplechatter.AboutInfoSection;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.simplechatter.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
    public void sendFeedback(View view)
    {
        Intent intent = new Intent(this,FeedbackActivity.class);
        startActivity(intent);
        finish();
    }
    public void reportBug(View view)
    {
        Intent intent = new Intent(this,ReportBugActivity.class);
        startActivity(intent);
        finish();
    }
}

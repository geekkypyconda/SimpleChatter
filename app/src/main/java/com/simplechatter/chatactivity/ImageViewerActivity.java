package com.simplechatter.chatactivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.simplechatter.R;
import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {

    private ImageView imageView;
    String imageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        imageView = (ImageView)findViewById(R.id.ImageViewerActivity_imageView);
        imageUrl = "";
        Intent intent = getIntent();
        imageUrl = intent.getStringExtra("image_url");
        Picasso.get().load(imageUrl).into(imageView);
    }
}

package com.sivert.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ortiz.touchview.TouchImageView;


public class Photo_read extends AppCompatActivity {

    TouchImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_read);

        final String imageUrl = getIntent().getStringExtra("imageid");

        img = findViewById(R.id.img);

        SimpleTarget target = new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition transition) {
                img.setImageBitmap(resource);
            }
        };

        Glide.with(Photo_read.this).asBitmap().load(imageUrl).into(target);
    }
}
package com.sivert.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sivert.chat.Model.User;

public class ProfileActivity extends AppCompatActivity {

    ImageView profile_image;
    ImageView image_profile;
    TextView username;
    ImageView official;
    Button btn_message;

    FirebaseUser fuser;
    DatabaseReference reference;

    boolean b_radio_topic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Setting
        SharedPreferences setting = getSharedPreferences("PREFS", MODE_PRIVATE);
        b_radio_topic = setting.getBoolean("TOPIC", true);

        if (b_radio_topic) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_profile);
        } else {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_profile_dark);
        }
        //END SETTING

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(ProfileActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });

        profile_image = findViewById(R.id.profile_image);
        image_profile = findViewById(R.id.image_profile);
        username = findViewById(R.id.username);
        official = findViewById(R.id.official);

        btn_message = findViewById(R.id.btn_message);

        final String userid = getIntent().getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();




        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    Glide.with(getApplicationContext())
                            .load(R.mipmap.ic_launcher)
                            .apply(RequestOptions.circleCropTransform())
                            .into(profile_image);
                    Glide.with(getApplicationContext())
                            .load(R.mipmap.ic_launcher)
                            .apply(RequestOptions.circleCropTransform())
                            .into(image_profile);
                } else {
                    Glide.with(getApplicationContext())
                            .load(user.getImageURL())
                            .apply(RequestOptions.circleCropTransform())
                            .into(profile_image);
                    Glide.with(getApplicationContext())
                            .load(user.getImageURL())
                            .apply(RequestOptions.circleCropTransform())
                            .into(image_profile);
                }
                if (user.getOfficial().equals("true")) {
                    official.setVisibility(View.VISIBLE);
                } else {
                    official.setVisibility(View.GONE);
                }


                btn_message.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ProfileActivity.this, MessageActivity.class);
                        intent.putExtra("userid", user.getId());
                        ProfileActivity.this.startActivity(intent);
//                Intent intent = new Intent(mContext, MessageActivity.class);
//                intent.putExtra("userid", user.getId());
//                mContext.startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });








    }
}
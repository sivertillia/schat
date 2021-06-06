package com.sivert.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sivert.chat.Fragments.ChatsFragment;
import com.sivert.chat.Fragments.FriendsFragment;
import com.sivert.chat.Fragments.ProfileFragment;
import com.sivert.chat.Fragments.UsersFragment;
import com.sivert.chat.Model.Chat;
import com.sivert.chat.Model.User;
import com.sivert.chat.Notifications.Data;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    ImageView profile_image;
    TextView username;
    ImageView official;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    //TEST
    boolean b_radio_topic;
    //END TEST



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Setting
        SharedPreferences setting = getSharedPreferences("PREFS", MODE_PRIVATE);
        b_radio_topic = setting.getBoolean("TOPIC", true);

        if (b_radio_topic) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
        } else {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main_dark);
        }
        //END SETTING


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        official = findViewById(R.id.official);





        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    Glide.with(getApplicationContext())
                            .load(R.mipmap.ic_launcher)
                            .apply(RequestOptions.circleCropTransform())
                            .into(profile_image);
                } else {
                    Glide.with(getApplicationContext())
                            .load(user.getImageURL())
                            .apply(RequestOptions.circleCropTransform())
                            .into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user.getOfficial().equals("true")) {
                    official.setVisibility(View.VISIBLE);
                } else {
                    official.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.view_pager);


        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                int unread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()) {
                        unread++;
                    }
                }


                if (unread == 0) {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Чат");

                } else {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "("+unread+")Чат");

                }


                viewPagerAdapter.addFragment(new FriendsFragment(), "Друзья");
                viewPagerAdapter.addFragment(new ProfileFragment(), "Профиль");

                viewPager.setAdapter(viewPagerAdapter);

                tabLayout.setupWithViewPager(viewPager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;

            case R.id.setting:
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                return true;

            case R.id.users:
                startActivity(new Intent(MainActivity.this, UsersActivity.class));
                return true;
        }

        return false;
    }

    public static class ViewPagerAdapter extends FragmentPagerAdapter {

        private static ArrayList<Fragment> fragments;
        private static ArrayList<String> titles;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.fragments = new  ArrayList<>();
            this.titles = new  ArrayList<>();

        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public static void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }



    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}
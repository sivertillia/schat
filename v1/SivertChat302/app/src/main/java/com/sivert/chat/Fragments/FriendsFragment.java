package com.sivert.chat.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sivert.chat.Adapter.UserAdapter;
import com.sivert.chat.Model.Chatlist;
import com.sivert.chat.Model.Friendslist;
import com.sivert.chat.Model.User;
import com.sivert.chat.R;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> mUsers;

    FirebaseUser fuser;
    DatabaseReference reference;
    private List<Friendslist> usersList;

    EditText search_users;

    boolean b_radio_topic;
    RelativeLayout relativeLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        search_users = view.findViewById(R.id.search_users);


        //Setting
        relativeLayout = view.findViewById(R.id.relativeLayout);

        SharedPreferences setting = this.getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        b_radio_topic = setting.getBoolean("TOPIC", true);

        if (b_radio_topic) {
            relativeLayout.setBackgroundColor(Color.WHITE);
        } else {
            relativeLayout.setBackgroundColor(Color.BLACK);
            search_users.setTextColor(Color.WHITE);
            search_users.setHintTextColor(Color.GRAY);
            search_users.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        }
        //END SETTING

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        usersList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Friendlist").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Friendslist friendslist = snapshot.getValue(Friendslist.class);
                    usersList.add(friendslist);
                }

                friendsList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }


    private void searchUsers(String s) {

        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
                .startAt(s)
                .endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    assert fuser != null;
                    if (!user.getId().equals(fuser.getUid())) {
                        mUsers.add(user);
                    }
                }

                userAdapter = new UserAdapter(getContext(), mUsers, "friend");
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void friendsList() {
        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (search_users.getText().toString().equals("")) {
                    mUsers.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        for (Friendslist friendslist : usersList) {
                            if (user.getId().equals(friendslist.getId())) {
                                mUsers.add(user);
                            }
                        }
                    }


                    userAdapter = new UserAdapter(getContext(), mUsers, "friend");
                    recyclerView.setAdapter(userAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
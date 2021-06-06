package com.sivert.chat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sivert.chat.MainActivity;
import com.sivert.chat.MessageActivity;
import com.sivert.chat.Model.Chat;
import com.sivert.chat.Model.Friendslist;
import com.sivert.chat.Model.User;
import com.sivert.chat.ProfileActivity;
import com.sivert.chat.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    FirebaseUser fuser;
    DatabaseReference reference;

    private Context mContext;
    private List<User> mUsers;
    private String ischat;

    String theLastMessage;




    public UserAdapter(Context mContext, List<User> mUsers, String ischat) {
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.ischat = ischat;
    }

        @NonNull
        @Override
        public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Setting
            SharedPreferences setting = this.mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
            boolean b_radio_topic = setting.getBoolean("TOPIC", true);
            //END SETTING
            if (ischat == "chat") {
                if (b_radio_topic) {
                    View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item, parent, false);
                    return new UserAdapter.ViewHolder(view);
                } else {
                    View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_dark, parent, false);
                    return new UserAdapter.ViewHolder(view);
                }
            } else if (ischat == "friend") {
                if (b_radio_topic) {
                    View view = LayoutInflater.from(mContext).inflate(R.layout.friend_item, parent, false);
                    return new UserAdapter.ViewHolder(view);
                } else {
                    View view = LayoutInflater.from(mContext).inflate(R.layout.friend_item_dark, parent, false);
                    return new UserAdapter.ViewHolder(view);
                }
            } else {
                if (b_radio_topic) {
                    View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
                    return new UserAdapter.ViewHolder(view);
                } else {
                    View view = LayoutInflater.from(mContext).inflate(R.layout.user_item_dark, parent, false);
                    return new UserAdapter.ViewHolder(view);
                }
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            fuser = FirebaseAuth.getInstance().getCurrentUser();

            final User user = mUsers.get(position);


            holder.username.setText(user.getUsername());
            if (user.getImageURL().equals("default")){
                holder.profile_image.setImageResource(R.mipmap.ic_launcher);
            } else {
                Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
            }

            if (ischat == "chat"){
                lastMessage(user.getId(), holder.last_msg);
            } else {
                holder.last_msg.setVisibility(View.GONE);
            }

            if (ischat == "chat") {
                if (user.getStatus().equals("online")) {
                    holder.img_on.setVisibility(View.VISIBLE);
                    holder.img_off.setVisibility(View.GONE);
                } else {
                    holder.img_off.setVisibility(View.VISIBLE);
                    holder.img_on.setVisibility(View.GONE);
                }
            } else {
                holder.img_off.setVisibility(View.GONE);
                holder.img_on.setVisibility(View.GONE);
            }

            if (ischat == "chat") {
                holder.official.setVisibility(View.GONE);
            } else {
                if (user.getOfficial().equals("true")) {
                    holder.official.setVisibility(View.VISIBLE);
                } else {
                    holder.official.setVisibility(View.GONE);
                }
            }
            if (ischat == "chat") {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, MessageActivity.class);
                        intent.putExtra("userid", user.getId());
                        mContext.startActivity(intent);
                    }
                });
            } else {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.putExtra("userid", user.getId());
                        mContext.startActivity(intent);
                    }
                });
            }
            holder.del_friend_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String userid = user.getId();
                    final DatabaseReference frindRef = FirebaseDatabase.getInstance().getReference("Friendlist")
                            .child(fuser.getUid())
                            .child(userid);
                    frindRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            frindRef.child("id").removeValue();
                            Toast.makeText(mContext, "Вы удалили друга: "+user.getUsername(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });
            holder.add_friend_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String userid = user.getId();

                    final DatabaseReference frindRef = FirebaseDatabase.getInstance().getReference("Friendlist")
                            .child(fuser.getUid())
                            .child(userid);
                    frindRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                frindRef.child("id").setValue(userid);
                                Toast.makeText(mContext, "Вы добавили друга: "+user.getUsername(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, "Вы уже добавили друга: "+user.getUsername(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });

        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView username;
            public ImageView profile_image;
            private ImageView img_on;
            private ImageView img_off;
            private TextView last_msg;
            private ImageView official;
            private ImageView add_friend_btn;
            private ImageView del_friend_btn;

            public ViewHolder(View itemView) {
                super(itemView);

                username = itemView.findViewById(R.id.username);
                profile_image = itemView.findViewById(R.id.profile_image);
                img_on = itemView.findViewById(R.id.img_on);
                img_off = itemView.findViewById(R.id.img_off);
                last_msg = itemView.findViewById(R.id.last_msg);
                official = itemView.findViewById(R.id.official);
                add_friend_btn = itemView.findViewById(R.id.add_friend_btn);
                del_friend_btn = itemView.findViewById(R.id.del_friend_btn);
            }
        }
        //check for last message
        private void lastMessage(final String userid, final TextView last_msg) {
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        if(firebaseUser != null)
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                        if (chat.getReceiver().equals(firebaseUser.getUid())){
                            theLastMessage = "Вам: "+chat.getMessage();
                        } else {
                            theLastMessage = "Я: "+chat.getMessage();

                        }
                    }
                }

                switch (theLastMessage) {
                    case "Вам: 6YgktrqRUk5E":
                        last_msg.setText("Вам: Фото");
                        break;
                    case "Я: 6YgktrqRUk5E":
                        last_msg.setText("Я: Фото");
                        break;
                    case "default":
                        last_msg.setText("No Message");
                        break;
                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

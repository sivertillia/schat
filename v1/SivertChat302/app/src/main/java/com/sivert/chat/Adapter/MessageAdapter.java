package com.sivert.chat.Adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.solver.widgets.Snapshot;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sivert.chat.MessageActivity;
import com.sivert.chat.Model.Chat;
import com.sivert.chat.Model.User;
import com.sivert.chat.Photo_read;
import com.sivert.chat.R;
import com.sivert.chat.StartActivity;

import java.util.HashMap;
import java.util.List;
import java.util.zip.Inflater;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChat;
    private String imageurl;

    public static String toAnswer;
    public static String ToAnswerName;


    FirebaseUser fuser;

    EditText text_send;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageurl) {
        this.mChat = mChat;
        this.mContext = mContext;
        this.imageurl = imageurl;

    }
    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Setting
        SharedPreferences setting = this.mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        boolean b_radio_topic = setting.getBoolean("TOPIC", true);
        //END SETTING
        if (viewType == MSG_TYPE_RIGHT) {
            if (b_radio_topic) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
                return new MessageAdapter.ViewHolder(view);
            } else {
                View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right_dark, parent, false);
                return new MessageAdapter.ViewHolder(view);
            }
        } else {
            if (b_radio_topic) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
                return new MessageAdapter.ViewHolder(view);
            } else {
                View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left_dark, parent, false);
                return new MessageAdapter.ViewHolder(view);
            }
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.ViewHolder holder, final int position) {
        View theVqw = LayoutInflater.from(mContext).inflate(R.layout.activity_message_dark, null);
        text_send = theVqw.findViewById(R.id.text_send);


        //Setting
        SharedPreferences setting = this.mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        boolean b_radio_topic = setting.getBoolean("TOPIC", true);
        //END SETTING

        final Chat chat = mChat.get(position);

        holder.show_message.setText(chat.getMessage());
        holder.toTextAnswer.setText(chat.getToAnswer());
        holder.name_toAnswer.setText("Ответ");
        setToAnswer("false");




        if (holder.show_message.getText().equals("6YgktrqRUk5E")){
            if (b_radio_topic) {
                holder.show_message.setText("Фото");
                holder.show_message.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_dark, 0, 0, 0);
            } else {
                holder.show_message.setText("Фото");
                holder.show_message.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play, 0, 0, 0);
            }
        }
        else {
            holder.show_message.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CharSequence text = holder.show_message.getText();
                    CharSequence username = chat.getReceiver();
                    showPopupMenu(v, text, username);
                    text_send.setText("");
                    return false;
                }
            });
        }
        holder.show_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String img = chat.getImg();
                if (!img.equals("false")) {
                    Intent intent = new Intent(mContext, Photo_read.class);
                    intent.putExtra("imageid", img);
                    mContext.startActivity(intent);
                } else {
                }
            }
        });

        if (imageurl.equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imageurl).into(holder.profile_image);
        }

        if (position == mChat.size()-1){
            if (chat.isIsseen()){
                holder.txt_seen.setText("Просмотренно");
            } else {
                holder.txt_seen.setText("Доставленно");
            }
        } else {
            holder.txt_seen.setVisibility(View.GONE);
        }

        if (b_radio_topic) {
            if (fuser.getUid().equals(chat.getSender())) {
                if (chat.getToAnswer().equals("false")) {
                    holder.show_message.setBackgroundResource(R.drawable.background_right);
                    holder.toAnswer.setVisibility(View.GONE);
                } else {
                    holder.show_message.setBackgroundResource(R.drawable.background_right_to_answer);
                    holder.toAnswer.setVisibility(View.VISIBLE);
                }
            } else {
                if (chat.getToAnswer().equals("false")) {
                    holder.show_message.setBackgroundResource(R.drawable.background_left);
                    holder.toAnswer.setVisibility(View.GONE);
                } else {
                    holder.show_message.setBackgroundResource(R.drawable.background_left_to_answer);
                    holder.toAnswer.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (fuser.getUid().equals(chat.getSender())) {
                if (chat.getToAnswer().equals("false")) {
                    holder.show_message.setBackgroundResource(R.drawable.background_right_dark);
                    holder.toAnswer.setVisibility(View.GONE);
                } else {
                    holder.show_message.setBackgroundResource(R.drawable.background_right_dark_to_answer);
                    holder.toAnswer.setVisibility(View.VISIBLE);
                }
            } else {
                if (chat.getToAnswer().equals("false")) {
                    holder.show_message.setBackgroundResource(R.drawable.background_left_dark);
                    holder.toAnswer.setVisibility(View.GONE);
                } else {
                    holder.show_message.setBackgroundResource(R.drawable.background_left_dark_to_answer);
                    holder.toAnswer.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        View theView = View.inflate(mContext, R.layout.activity_message, null);

//        View asd = new Inflater().inflate(mContext, R.layout.activity_message, null);
        public TextView show_message;
        public ImageView profile_image;
        public TextView txt_seen;
        public RelativeLayout toAnswer;
        public RelativeLayout sms;
        public TextView name_toAnswer;
        public TextView toTextAnswer;

//        public EditText text_send;


        public ViewHolder(View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            toAnswer = itemView.findViewById(R.id.toAnswer);
            sms = itemView.findViewById(R.id.sms);
            name_toAnswer = itemView.findViewById(R.id.name_toAnswer);
            toTextAnswer = itemView.findViewById(R.id.toTextAnswer);

//            text_send = theVqw.findViewById(R.id.text_send);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(fuser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    private void showPopupMenu(View v, final CharSequence text, final CharSequence username) {
        final PopupMenu popupMenu = new PopupMenu(mContext, v);
        popupMenu.inflate(R.menu.popup_menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.copyText:
                                copy(text);
                                return true;
                            case R.id.toAnswer:
                                toAnswer(text, username);
                                return true;
                            default:
                                return false;
                        }
                    }
                });

        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {

            }
        });
        popupMenu.show();
    }


    private void copy(CharSequence copyText) {
        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", copyText);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(mContext, "Скопировано в буфер обмена", Toast.LENGTH_SHORT).show();
    }

    public void toAnswer(CharSequence sms_text, CharSequence sms_username) {
        setToAnswer(sms_text.toString());
        setToAnswerName(sms_username.toString());



    }
    public static String getToAnswer() {
        return toAnswer;
    }

    public static void setToAnswer(String toAnswer) {
        MessageAdapter.toAnswer = toAnswer;
    }

    public static String getToAnswerName() {
        return ToAnswerName;
    }

    public static void setToAnswerName(String toAnswerName) {
        ToAnswerName = toAnswerName;
    }
}
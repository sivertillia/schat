package com.sivert.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.sivert.chat.Adapter.MessageAdapter;
import com.sivert.chat.Fragments.APIService;
import com.sivert.chat.Model.Chat;
import com.sivert.chat.Model.User;
import com.sivert.chat.Notifications.Client;
import com.sivert.chat.Notifications.Data;
import com.sivert.chat.Notifications.MyResponce;
import com.sivert.chat.Notifications.Sender;
import com.sivert.chat.Notifications.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private RelativeLayout activity_message;

//    CircleImageView profile_image;
    ImageView profile_image;
    TextView username;
    ImageView official;

    FirebaseUser fuser;
    DatabaseReference reference;

    ImageButton btn_send, btn_send_img;
    EditText text_send;

    MessageAdapter messageAdapter;
    List<Chat> mchat;
    RecyclerView recyclerView;

    ValueEventListener seenListener;

//    String userid;

    APIService apiService;

    boolean notify = false;

    boolean b_radio_topic;

    //send image
    private static final int IMAGE_REQUEST = 1;
    private static final int RESULT_OK = 1;
    private Uri imageUri;
    StorageReference storageReference;
    private StorageTask uploadTask;
    //close send image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Setting
        SharedPreferences setting = getSharedPreferences("PREFS", MODE_PRIVATE);
        b_radio_topic = setting.getBoolean("TOPIC", true);

        if (b_radio_topic) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_message);
        } else {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_message_dark);
        }
        //END SETTING

        //send image
        storageReference = FirebaseStorage.getInstance().getReference("bd_images");
        //close send image

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        activity_message = findViewById(R.id.activity_message);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        official = findViewById(R.id.official);

        btn_send = findViewById(R.id.btn_send);
        btn_send_img = findViewById(R.id.btn_send_img);
        text_send = findViewById(R.id.text_send);

        final String userid = getIntent().getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();


        btn_send.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            notify = true;
            String msg = text_send.getText().toString();
            if (!msg.equals("")) {
                sendMessage(fuser.getUid(), userid, msg);
            } else {
                Toast.makeText(MessageActivity.this, "Вы не можете отправить пустое сообщение", Toast.LENGTH_SHORT).show();
            }
            text_send.setText("");
            }
        });
        btn_send_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });
        official.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(activity_message, "Оффициальная галочка", Snackbar.LENGTH_LONG).show();
            }
        });



        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

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
                if (user.getOfficial().equals("true")) {
                    official.setVisibility(View.VISIBLE);
                } else {
                    official.setVisibility(View.GONE);
                }
                readMessages(fuser.getUid(), userid, user.getImageURL());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userid);
    }

    private void seenMessage(final String userid) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        String toAnswer = messageAdapter.getToAnswer();


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);
        hashMap.put("img", "false");
        hashMap.put("toAnswer", toAnswer);
        hashMap.put("toAnswerName", "false");


        reference.child("Chats").push().setValue(hashMap);
        final String userid = getIntent().getStringExtra("userid");

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                 .child(fuser.getUid())
                 .child(userid);

         chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });


         final String msg = message;

         reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
         reference.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 User user = dataSnapshot.getValue(User.class);
                 if (notify){
                    sendNotifiaction(receiver, user.getUsername(), msg);
                 }
                 notify = false;
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });
    }

    //send image
    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }
    private String getFileExtension(Uri uri) {
        //getContext()
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        //getContext()
        final ProgressDialog pd = new ProgressDialog(MessageActivity.this);
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));
            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()) {
                        final String userid = getIntent().getStringExtra("userid");

                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        sendImgMessage(fuser.getUid(), userid, mUri);

                        pd.dismiss();
                    } else {
                        //getContext()
                        Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //getContext()
                    Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }
        else {
            //getContext()
            Toast.makeText(MessageActivity.this, "Вы не вибрали изображения", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && requestCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                //getContext()
                Toast.makeText(MessageActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }
    private void sendImgMessage(String sender, final String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", "6YgktrqRUk5E");
        hashMap.put("isseen", false);
        hashMap.put("img", message);
        hashMap.put("toAnswer", "false");
        hashMap.put("toAnswerName", "false");

        reference.child("Chats").push().setValue(hashMap);
        final String userid = getIntent().getStringExtra("userid");

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                 .child(fuser.getUid())
                 .child(userid);

         chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });


         final String msg = message;

         reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
         reference.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 User user = dataSnapshot.getValue(User.class);
                 if (notify){
                    sendNotifiaction(receiver, user.getUsername(), msg);
                 }
                 notify = false;
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });
    }
    //close send image

    private void sendNotifiaction(String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String userid = getIntent().getStringExtra("userid");
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.mipmap.ic_launcher, username+": "+message, "New Message", userid);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponce>() {
                                @Override
                                public void onResponse(Call<MyResponce> call, Response<MyResponce> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(MessageActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponce> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages(final String myid, final String userid, final String imageurl){
        mchat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) || chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mchat.add(chat);

                    }
                    messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");

        final String userid = getIntent().getStringExtra("userid");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        if (seenListener != null && reference!=null) {
            reference.removeEventListener(seenListener);
        }
        super.onPause();
        status("offline");
        currentUser("none");
    }
}
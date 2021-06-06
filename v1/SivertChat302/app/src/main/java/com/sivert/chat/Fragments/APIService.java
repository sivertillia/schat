package com.sivert.chat.Fragments;

import com.sivert.chat.Notifications.MyResponce;
import com.sivert.chat.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA47hQcqg:APA91bEztGqWOkVRWds4U_8mqy7x7enuy7OJd5Nrc91R8TrOt0Sq5sBasVPgOqELvA9JL8QX8pgUMgEvIVWLuesNEIPUn4nSsqtrZrBY7HrCWx1-e2fQuy0N9T7CkBzOU8SZKq2fAkzL"
            }
    )

    @POST("fcm/send")
    Call<MyResponce> sendNotification(@Body Sender body);
}

package com.app.talkzy.Fragment;

import com.app.talkzy.Notifications.MyResponse;
import com.app.talkzy.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(

            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAZopGAhA:APA91bHemtzIj5ips6sSuZ3sw2YdFHQikqXTLgpNdZlSJaYpxMVNouffBeQXlhanDVzSmXPMEzXhdvztmUcuG5-LeV0huPEl-W6V_rNaXJYF8UQ16MUD5bxDmx-bOuEhnlzxEUebBI9l"
            }

    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}

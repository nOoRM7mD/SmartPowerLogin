package com.example.lenovo.ronixtask;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by lenovo on 6/9/2018.
 */

public interface UserClient {
    @GET("/ronix_services/task/srv.php")
    Call<User> getUsers();
    // Call<User> getUsers(@Header("Authorization") String auth);

   /* @POST("/ronix_services/task/srv.php")
    Call<User> basicLogin();*/
}

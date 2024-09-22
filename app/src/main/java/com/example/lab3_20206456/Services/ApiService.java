package com.example.lab3_20206456.Services;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @POST("auth/login")
    Call<User> loginUser(@Body LoginRequest loginRequest);

    @GET("todos/user/{userId}")
    Call<TaskResponse> getUserTasks(@Path("userId") int userId);

    @PUT("todos/{todoId}")
    Call<Task> updateTaskStatus(@Path("todoId") int todoId, @Body Task task);
}

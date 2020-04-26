package com.example.myapplication.data;

import android.util.Log;

import com.example.myapplication.data.model.LoggedInUser;
import com.example.myapplication.data.OkClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(final String username, final String password) {
        final String[] user = {"none"};
        final boolean[] flag = {false};
        final String[] cookie = {"no cookie"};
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkClient OkClient = new OkClient();
                    OkClient.setMode("login",username,password);
                    String response_string = OkClient.getResult();
                    cookie[0] = OkClient.getCookie();
                    try {
                        JSONObject connect = new JSONObject(response_string);
                        if (connect.has("username")) {
                            Log.e("log in ", "succussful");
                            user[0] = connect.get("username").toString();
                            flag[0] = true;
                        } else {
                            Log.e("log in", "failed");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        try {
            //等待链接进程结束
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            // TODO: handle loggedInUser authentication
            if (flag[0]) {
                LoggedInUser User =
                        new LoggedInUser(
                                cookie[0],
                                user[0]);
                return new Result.Success<>(User);
            } else {
                return new Result.Failare<>(new LoggedInUser(java.util.UUID.randomUUID().toString(),username));
            }
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}

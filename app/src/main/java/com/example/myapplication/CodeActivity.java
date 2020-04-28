package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.data.OkClient;
import com.example.myapplication.data.ResolveJson;
import com.example.myapplication.data.QRCodeUtil;
import com.example.myapplication.ui.login.LoginActivity;
import com.example.myapplication.FilmUserActivity;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CodeActivity extends AppCompatActivity {
    private Button c_button;
    private ArrayList<String> i = new ArrayList<>();
    private String cookie, orderString, orderId;
    private ImageView cover;
    private TextView orderInfo, title, Blurb;
    private LinearLayout qr_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);

        orderInfo = findViewById(R.id.info);
        title = findViewById(R.id.title);
        cover = findViewById(R.id.cover);
        Blurb = findViewById(R.id.blurb);
        qr_layout = findViewById(R.id.qr_layout);


        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


        cookie = sharedPreferences.getString("cookie", "");
        orderId = sharedPreferences.getString("recentOrderId", "");

        c_button = findViewById(R.id.back);
        c_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //return
                Intent intent = new Intent(CodeActivity.this, FilmActivity.class);
                startActivity(intent);
            }
        });
        init();
    }

    private void init() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    orderString = new OkClient(cookie).getOrder(orderId);
                    showOrder();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    //未完成
    private void showOrder() throws Exception {
        JSONObject order = new JSONObject(orderString);
        String date;
        String startTime;
        String finishTime;
        String ageType;
        String movieId;
        String totalCost;
        Integer seatId, roomId;
        totalCost = order.getString("totalCost");
        JSONArray tickets = new JSONArray(order.getString("tickets"));
        if (tickets != null) {
            JSONObject baseScreening = new JSONObject(tickets.getJSONObject(0).getString("screening"));
            JSONObject baseTicket = tickets.getJSONObject(0);

            ageType = baseTicket.getString("ageType");
            date = baseScreening.getString("date");
            startTime = baseScreening.getString("time");
            finishTime = baseScreening.getString("finishTime");
            roomId = baseScreening.getInt("auditoriumId");
            movieId = baseScreening.getString("movieId");
            String infoDetails = tickets.length() + " " + ageType + " tickets\nIn " + date + "\nfrom " + startTime + " to "
                    + finishTime + "\nRoom " + roomId.toString() + "\nTotalPrice: " + totalCost;
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                orderInfo.setText(infoDetails);
              }
            });

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String infoDetails = null;
                        Integer width = 450,height = 450;
                        if(tickets.length()>2){
                          width=200;
                          height =200;
                        }
                        Integer finalWidth = width;
                        Integer finalHeight = height;
                        for (int i = 0; i < tickets.length(); i++) {
                            JSONObject ticket = tickets.getJSONObject(i);
                            Integer seatId = ticket.getInt("seatId");
                            String seat = new OkClient(cookie).getSeatsPosition(seatId);
                            String validation = ticket.getString("validation");
                            JSONObject pos = new JSONObject(seat);
                            String seatPos = "( " + pos.getString("row") + " , " + pos.getString("col") + " )";
                            infoDetails += "\nseat: " + seatPos;


                          runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Bitmap bitmap = QRCodeUtil.createQRCodeBitmap(validation, finalWidth, finalHeight);
                                    addQrCode(bitmap);
                                }
                            });
                        }
                        Log.e("info", infoDetails);

                        String movieData = new OkClient().getMovieInfo(movieId);
                        JSONObject movieJSON = new JSONObject(movieData);
                        String Title = movieJSON.getString("name");
                        String blurb = movieJSON.getString("blurb");
                        String coverName = movieJSON.getString("cover");


                        if (coverName != "null") {
                            Thread m = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Bitmap bitmap = new OkClient().getImg(coverName);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                cover.setImageBitmap(bitmap);
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            m.start();
                        }
                        String finalInfoDetails = infoDetails;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                orderInfo.append(finalInfoDetails);
                                title.setText(Title);
                                Blurb.setText(blurb);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }


    }

    private void addQrCode(Bitmap bitmap) {
        ImageView qr_code = new ImageView(CodeActivity.this);
        LinearLayout.LayoutParams qrCodeParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        qr_code.setLayoutParams(qrCodeParams);
        qr_code.setImageBitmap(bitmap);
        qr_layout.addView(qr_code);
    }


}

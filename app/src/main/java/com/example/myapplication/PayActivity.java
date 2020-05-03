package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.data.OkClient;
import com.example.myapplication.data.QRCodeUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**this is the activity to confirm and pay the order
 * it will read the order id which is stored with key word "recentOrderId" in local storage in advance
 * it shows the order data
 * it enables users to confirm or cancel the order*/

public class PayActivity extends AppCompatActivity implements View.OnClickListener {
    private String orderString, orderId, cookie, screenData, movieData, createdTime,father;
    Button wechat, ali, cash, confirm, backReturn, cancel;
    public static int x, c;
    public int[] count = new int[3];
    private TextView orderInfo, title, Blurb, count_down_V;
    private ImageView cover;
    private Intent backIntent;

    public static Integer[] idB = new Integer[]{R.id.ali, R.id.wechat, R.id.cash};

    Button[] sButtons = new Button[]{ali, wechat, cash};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        //determine where to back when back is on
        father = getIntent().getStringExtra("father");
        backIntent = new Intent(PayActivity.this,FilmBookDetailActivity.class);
        if(father!=null) {
            if (father.equals("ticket")) {
                backIntent = new Intent(PayActivity.this, TicketActivity.class);
            }
        }
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        orderId = sharedPreferences.getString("recentOrderId", "");
        cookie = sharedPreferences.getString("cookie", "");

        //initialize the widgets
        confirm = findViewById(R.id.pay_confirm);
        backReturn = findViewById(R.id.pay_return);
        cancel = findViewById(R.id.pay_cancel);
        orderInfo = findViewById(R.id.info);
        title = findViewById(R.id.title);
        cover = findViewById(R.id.cover);
        Blurb = findViewById(R.id.blurb);
        count_down_V = findViewById(R.id.count_down);

        //call the confirm method
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new OkClient(cookie).payOrder(cookie);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.start();
                try {
                    t.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(PayActivity.this, CodeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //call the cancel method
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelOrder();
            }
        });

        //call back method
        backReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PayActivity.this, FilmBookDetailActivity.class);
                startActivity(intent);
            }
        });

        //select pay method
        for (x = 0; x < 3; x++) {
            sButtons[x] = findViewById(idB[x]);
        }
        for (x = 0; x < 3; x++) {
            count[x] = 0;
        }
        for (x = 0; x < 3; x++) {
            sButtons[x].setOnClickListener(this);
        }
        init();
    }

    //rewrite click for pay method
    @Override
    public void onClick(View v) {
        for (x = 0; x < 3; x++) {
            if (v.getId() == idB[x]) break;
        }
        if (count[x] % 2 == 1) {
            sButtons[x].setBackgroundDrawable(getResources().getDrawable(R.drawable.pay0));
            count[x]--;

        } else {
            sButtons[x].setBackgroundDrawable(getResources().getDrawable(R.drawable.pay1));
            for (c = 0; c < 3; c++) {
                if (c != x)
                    sButtons[c].setBackgroundDrawable(getResources().getDrawable(R.drawable.pay0));
                count[c]--;
            }
            count[x]++;
        }
    }

    //initialize order page
    private void init() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    orderString = new OkClient(cookie).getOrder(orderId);
                    getAllData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    //cancel method
    private void cancelOrder() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new OkClient(cookie).cancelOrder(cookie);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        try {
            t.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        startActivity(backIntent);
        finish();
    }

    //method to get the order data
    private void getAllData() throws Exception {
        JSONObject order = new JSONObject(orderString);
        String date;
        String startTime;
        String finishTime;
        String ageType;
        String movieId;
        String totalCost;
        Integer seatId, roomId;
        totalCost = order.getString("totalCost");
        createdTime = order.getString("createdAt");
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
                    + finishTime + "\nRoom " + roomId.toString() + "\nTotalPrice: " + totalCost + "\n";
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
                        String infoDetails = "";
                        for (int i = 0; i < tickets.length(); i++) {
                            JSONObject ticket = tickets.getJSONObject(i);
                            Integer seatId = ticket.getInt("seatId");
                            String seat = new OkClient(cookie).getSeatsPosition(seatId);
                            JSONObject pos = new JSONObject(seat);
                            Character rol = 'A';
                            for (int m = 0; m < pos.getInt("col"); m++) {
                                rol++;
                            }
                            String seatPos = "row " + pos.getString("row") + " , col " + rol;
                            infoDetails += "seat: " + seatPos + "\n";
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                countDown();
            }
        });
    }

    //method to count down for automatic cancel
    private void countDown() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        double passedTime = 0;

      Date date1 = null;
      DateFormat df2 = null;
      try {
          DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
          Date date = df.parse(createdTime);
          SimpleDateFormat df1 = new SimpleDateFormat ("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK);
          date1 = df1.parse(date.toString());
          df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      } catch (Exception e) {

          e.printStackTrace();
      }

        String dateString = df2.format(date1);
        Log.e("date", dateString);
        try {
            Date date = sdf.parse(dateString);
            Date today = sdf.parse(sdf.format(new Date(System.currentTimeMillis())));
            passedTime = today.getTime() - date.getTime()-8*60*60*1000;
            Log.e("passedTIme", String.valueOf(passedTime));
        } catch (Exception e) {
            e.printStackTrace();
        }
        double restTime = 1000 * 60 * 15 - passedTime;
        Log.e("restime", String.valueOf(restTime));
        CountDownTimer timer = new CountDownTimer((long) restTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                double minute = Math.floor(millisUntilFinished / 60 / 1000);
                double seconds = millisUntilFinished / 1000 - minute * 60;
                count_down_V.setText("Order Automatically Canceled in " + (Integer.valueOf((int) minute)) + " : " + (seconds < 10 ? "0" : "") + (Integer.valueOf((int) seconds)));
            }

            @Override
            public void onFinish() {
                String tips = "order over due, automatically canceled";
                Toast toast = Toast.makeText(PayActivity.this, tips, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                cancelOrder();

            }
        }.start();


    }
}

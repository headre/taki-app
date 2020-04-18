package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.data.OkClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class PayActivity extends AppCompatActivity implements View.OnClickListener {
  private String orderString, orderId, cookie, screenData, movieData;
  Button wechat, ali, cash, confirm, backReturn, cancel;
  public static int x, c;
  public int[] count = new int[3];
  private TextView orderInfo,title,Blurb;
  private ImageView cover;

  public static Integer[] idB = new Integer[]{R.id.ali, R.id.wechat, R.id.cash};

  Button[] sButtons = new Button[]{ali, wechat, cash};

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pay);
    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
    orderId = sharedPreferences.getString("recentOrderId", "");
    cookie = sharedPreferences.getString("cookie", "");


    confirm = findViewById(R.id.pay_confirm);
    backReturn = findViewById(R.id.pay_return);
    cancel = findViewById(R.id.pay_cancel);
    orderInfo = findViewById(R.id.info);
    title = findViewById(R.id.title);
    cover = findViewById(R.id.cover);
    Blurb = findViewById(R.id.blurb);

    confirm.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Thread t= new Thread(new Runnable() {
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
        try{
          t.join();
        }catch (Exception e){
          e.printStackTrace();
        }
        Intent intent = new Intent(PayActivity.this, CodeActivity.class);
        startActivity(intent);
        finish();
      }
    });

    cancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Thread t= new Thread(new Runnable() {
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
        try{
          t.join();
        }catch (Exception e){
          e.printStackTrace();
        }
        Intent intent = new Intent(PayActivity.this, FilmBookDetailActivity.class);
        startActivity(intent);
      }
    });

    backReturn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(PayActivity.this, FilmBookDetailActivity.class);
        startActivity(intent);
      }
    });
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

      for (int i = 0; i < tickets.length(); i++) {
        JSONObject ticket = tickets.getJSONObject(i);
        seatId = ticket.getInt("seatId");


        Integer finalSeatId = seatId;
        Thread t = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              String infoDetails = tickets.length() + " " + ageType + " tickets\nIn " + date + " from " + startTime + " to "
                + finishTime + "\nRoom " + roomId.toString() + " seat \nTotalPrice: " + totalCost;
              String seat = new OkClient(cookie).getSeatsPosition(finalSeatId);
              JSONObject pos = new JSONObject(seat);
              String seatPos = "( " + pos.getString("row") + " , " + pos.getString("col") + " )";
              infoDetails.replace("seat", "seat " + seatPos + " ");
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
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    orderInfo.setText(infoDetails);
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
  }
}

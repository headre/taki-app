package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
  private ImageView cover,qr_code;
  private TextView orderInfo, title, Blurb;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_code);

    orderInfo = findViewById(R.id.info);
    title = findViewById(R.id.title);
    cover = findViewById(R.id.cover);
    Blurb = findViewById(R.id.blurb);
    qr_code = findViewById(R.id.img_qrcode);


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
    String validation;
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
      validation = baseTicket.getString("validation");

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
                  Bitmap bitmap = QRCodeUtil.createQRCodeBitmap(validation, 450, 450);
                  qr_code.setImageBitmap(bitmap);

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

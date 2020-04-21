package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.data.OkClient;
import com.example.myapplication.data.QRCodeUtil;
import com.example.myapplication.data.pixelTools;
import com.example.myapplication.ui.login.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class FilmUserActivity extends AppCompatActivity {
  private Button userButton, screenButton, cinemaButton,logoutButton;
  private LinearLayout orders;
  private String cookie, orderList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_film_user);
    screenButton = findViewById(R.id.screen);
    userButton = findViewById(R.id.user);
    cinemaButton = findViewById(R.id.cinema);
  
    orders = findViewById(R.id.orders);
    logoutButton =findViewById(R.id.logout);


    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    cookie = sharedPreferences.getString("cookie", "");
    screenButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(FilmUserActivity.this, FilmUserActivity.class);
        startActivity(intent);
      }
    });

    userButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = null;
        if (cookie != null && cookie != "") {
          intent = new Intent(FilmUserActivity.this, FilmUserActivity.class);
        } else {
          intent = new Intent(FilmUserActivity.this, LoginActivity.class);
        }
        startActivity(intent);
      }
    });

    cinemaButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(FilmUserActivity.this, FilmActivity.class);
        startActivity(intent);
      }
    });



    logoutButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Thread t = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              new OkClient(cookie).logout();
              editor.remove("cookie");
              editor.commit();
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
        Intent intent = new Intent(FilmUserActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
      }
    });

    init();
  }

  private void init() {
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          orderList = new OkClient().getOrders(cookie);
        } catch (Exception e) {
          e.printStackTrace();
        }

        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try {
              orders.removeAllViews();
              JSONArray orderArray = null;
              if (orderList != null) {
                orderArray = new JSONArray(orderList);
                for (int i = 0; i < orderArray.length(); i++) {
                  addOrder(orderArray.getJSONObject(i));
                }
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });

      }
    });
    t.start();
  }

  private void addOrder(JSONObject order) throws Exception {
    final int[] click = {0};

    LinearLayout qrLayout = new LinearLayout(FilmUserActivity.this);
    TextView title = new TextView(FilmUserActivity.this);
    TextView info = new TextView(FilmUserActivity.this);
    int padding2 = pixelTools.dip2px(FilmUserActivity.this, 2);
    int margin5 = pixelTools.dip2px(FilmUserActivity.this, 5);

    final String[] infoDetails = new String[1];
    final String[] Title = new String[1];
    String date;
    String startTime;
    String finishTime;
    String validation;
    String ageType;
    String movieId;
    String totalCost;
    Integer screenId, seatId, roomId;
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
      infoDetails[0] = tickets.length() + " " + ageType + " tickets\nIn " + date + " from " + startTime + " to "
        + finishTime + "\nRoom " + roomId.toString() + " seat \nTotalPrice: " + totalCost;

      for (int i = 0; i < tickets.length(); i++) {
        JSONObject ticket = tickets.getJSONObject(i);
        seatId = ticket.getInt("seatId");
        validation = ticket.getString("validation");


        Integer finalSeatId = seatId;
        Thread t = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              String seat = new OkClient(cookie).getSeatsPosition(finalSeatId);
              JSONObject pos = new JSONObject(seat);
              String seatPos = "( " + pos.getString("row") + " , " + pos.getString("col") + " )";

              infoDetails[0].replace("seat", "seat " + seatPos + " ");
              Log.e("info", infoDetails[0]);
              Title[0] = new JSONObject(new OkClient().getMovieInfo(movieId)).getString("name");
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  title.setText(Title[0]);
                  info.setText(infoDetails[0]);
                }
              });
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
        t.start();
        ImageView qrCode = new ImageView(FilmUserActivity.this);
        LinearLayout.LayoutParams qrParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Bitmap bitmap = QRCodeUtil.createQRCodeBitmap(validation, 100, 100);
        qrCode.setImageBitmap(bitmap);

        qrLayout.addView(qrCode);

      }
    }

    LinearLayout orderLayout = new LinearLayout(FilmUserActivity.this);
    LinearLayout.LayoutParams OLParams = new LinearLayout.LayoutParams(pixelTools.dip2px(FilmUserActivity.this, 200), ViewGroup.LayoutParams.MATCH_PARENT);
    orderLayout.setBackgroundColor(Color.parseColor("#ffffff"));
    OLParams.setMargins(margin5, 0, 0, 0);
    orderLayout.setPadding(padding2, padding2, padding2, padding2);
    orderLayout.setGravity(Gravity.CENTER);
    orderLayout.setOrientation(LinearLayout.VERTICAL);
    orderLayout.setLayoutParams(OLParams);


    LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pixelTools.dip2px(FilmUserActivity.this, 30));
    title.setTextSize(20);
    title.setBackgroundColor(Color.parseColor("#ffffff"));
    title.setGravity(Gravity.CENTER);
    title.setLayoutParams(titleParams);


    LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pixelTools.dip2px(FilmUserActivity.this, 100));
    info.setBackgroundColor(Color.parseColor("#ffffff"));
    info.setGravity(Gravity.CENTER);
    info.setLayoutParams(infoParams);


    LinearLayout.LayoutParams QRLParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pixelTools.dip2px(FilmUserActivity.this, 160));
    qrLayout.setGravity(Gravity.CENTER);
    qrLayout.setOrientation(LinearLayout.HORIZONTAL);
    qrLayout.setBackgroundColor(Color.parseColor("#ffffff"));
    qrLayout.setLayoutParams(QRLParams);

    orderLayout.addView(title);
    orderLayout.addView(info);
    orderLayout.addView(qrLayout);

    orders.addView(orderLayout);

    orderLayout.setClickable(true);
    orderLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (click[0] < 1) {
          orderLayout.setBackgroundColor(Color.parseColor("#03A9F4"));
          click[0]++;
        } else {
          orderLayout.setBackgroundColor(Color.parseColor("#ffffff"));
          click[0]--;
        }
      }
    });
  }
}

package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.data.OkClient;
import com.example.myapplication.data.QRCodeUtil;
import com.example.myapplication.data.pixelTools;
import com.example.myapplication.ui.login.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FilmUserActivity extends AppCompatActivity {
  private Button userButton, screenButton, cinemaButton, logoutButton, refundButton,mailButton;
  private LinearLayout orders,emailTicket;
  private String cookie, orderList;
  private Integer refund = 0;
  private ArrayList<Integer> ticketsList = new ArrayList<>();
  private final Integer maximumTickets = 1;

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_film_user);
    screenButton = findViewById(R.id.screen);
    userButton = findViewById(R.id.user);
    cinemaButton = findViewById(R.id.cinema);
    refundButton = findViewById(R.id.refund);
    mailButton = findViewById(R.id.email);

    orders = findViewById(R.id.orders);
    logoutButton = findViewById(R.id.logout);


    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    cookie = sharedPreferences.getString("cookie", "");

    mailButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (true) {
          String tips = "Tickets are being generated, please wait";
          Toast toast = Toast.makeText(FilmUserActivity.this, tips, Toast.LENGTH_LONG);
          toast.setGravity(Gravity.CENTER, 0, 0);
          toast.show();
          Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                pdfModel(emailTicket);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          });
          t.start();
        }else{
          String tips = "You haven't chosen any tickets";
          Toast toast = Toast.makeText(FilmUserActivity.this, tips, Toast.LENGTH_SHORT);
          toast.setGravity(Gravity.CENTER, 0, 0);
          toast.show();
        }
      }
    });
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
        try {
          t.join();
        } catch (Exception e) {
          e.printStackTrace();
        }
        Intent intent = new Intent(FilmUserActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
      }
    });

    refundButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        refundTickets();
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
    String date = null;
    String startTime;
    String finishTime;
    String validation;
    String ageType;
    String movieId;
    String totalCost;
    Integer screenId, seatId, roomId, orderId;
    orderId = order.getInt("id");
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

    String finalDate = date;
    orderLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String tips = null;
        if (click[0] < 1) {
          if (ticketsList.size() < maximumTickets) {
            orderLayout.setBackgroundColor(Color.parseColor("#03A9F4"));
            if(!refundOk(finalDate)){
              refund--;
            }else {
              refund++;
            }
            for(int i =0;i<tickets.length();i++){
              JSONObject ticket = null;
              try {
                ticket = tickets.getJSONObject(i);
                Integer ticketId = ticket.getInt("id");
                ticketsList.add(ticketId);
              } catch (JSONException e) {
                e.printStackTrace();
              }
            }
            emailTicket = orderLayout;
            click[0]++;
          } else {
            tips = "you can only choose "+maximumTickets.toString()+" tickets";
            Toast toast = Toast.makeText(FilmUserActivity.this, tips, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
          }
        } else {
          if(!refundOk(finalDate)){
            refund++;
          }else {
            refund--;
          }
          emailTicket = null;
          orderLayout.setBackgroundColor(Color.parseColor("#ffffff"));
          for(int i =0;i<tickets.length();i++){
            JSONObject ticket = null;
            try {
              ticket = tickets.getJSONObject(i);
              Integer ticketId = ticket.getInt("id");
              ticketsList.remove(ticketId);
            } catch (JSONException e) {
              e.printStackTrace();
            }

          }
          click[0]--;
        }

        Log.e("list", ticketsList.toString()+"  "+ refund);
      }
    });
  }

  //根据票的放映日期判断是否可以退票
  private Boolean refundOk(String date) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Boolean avail = false;
    try {
      Date today = sdf.parse(sdf.format(new Date()));
      Date targetDate = sdf.parse(date);
      if (today.getTime() < targetDate.getTime()) {
        avail = true;
      }
      Log.e("refundOk", avail.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }

    return avail;
  }

  private void refundTickets() {
    if (refund==0) {
      String tips = "You haven't chosen any tickets";
      Toast toast = Toast.makeText(FilmUserActivity.this, tips, Toast.LENGTH_SHORT);
      toast.setGravity(Gravity.CENTER, 0, 0);
      toast.show();
    /*} else if(refund <maximumTickets) {
      String tips = "THe tickets you chose has over date ones!";
      Toast toast = Toast.makeText(FilmUserActivity.this, tips, Toast.LENGTH_SHORT);
      toast.setGravity(Gravity.CENTER, 0, 0);
      toast.show();*/
    }else{
        for (int i = 0; i < ticketsList.size(); i++) {
          int finalI = i;
          Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                new OkClient(cookie).refund(ticketsList.get(finalI));
                if(finalI==ticketsList.size()-1){
                  String tips = "refund successfully, all fees return to your accounts, please check";
                  Toast toast = Toast.makeText(FilmUserActivity.this, tips, Toast.LENGTH_SHORT);
                  toast.setGravity(Gravity.CENTER, 0, 0);
                  toast.show();

                  init();
                }
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          });
          t.start();
        }
        Log.e("refund", "finished");
      }
    }



  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  private void pdfModel(LinearLayout layout) throws  Exception{
    String path = getApplicationContext().getFilesDir().getPath();
    Log.e("path",path);
    path = "/data/data/com.example.myapplication";
    PdfDocument document = new PdfDocument();
    Log.e("size, width, height", String.valueOf(layout.getWidth())+String.valueOf(layout.getHeight()));
    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(layout.getWidth(),layout.getHeight(),1).create();
    PdfDocument.Page page = document.startPage(pageInfo);
    layout.draw(page.getCanvas());
    document.finishPage(page);
    File file = new File(path+"/test.pdf");
    document.close();
    try{
      new OkClient(cookie).sendTicketToEmail(file);
    }catch (Exception e){
      e.printStackTrace();
    }
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        String tips = "ticket has been sent to your email box, please check";
        Toast toast = Toast.makeText(FilmUserActivity.this, tips, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
      }
    });
  }
}

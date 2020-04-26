package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigDecimal;

import com.example.myapplication.data.OkClient;
import com.example.myapplication.ui.login.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class FilmBookDetailActivity extends AppCompatActivity implements View.OnClickListener{
  private Button bookButton, freshButton,adult,senior,child;
  private double originalTotalPrice, originalPrice,discountedTotalPrice;
  private String [] ageTypeList = {"ADULT","SENIOR","CHILD"};
  private String cookie = null, screenId = null,ageType = ageTypeList[0];
  private Integer row;
  private Integer col;
  private Integer count1 = 0;
  private double discountMag=1;
  public static final int MAX_GRID = 56;
  private ArrayList<String> ticketSelected = new ArrayList<>();
  private ArrayList<HashMap> seatsOfAuditorium = null, seatsTaken = null;
  private JSONObject movieData = null, screenData = null;
  public static int x,c;
  private Boolean showDiscount = false;
  TextView showPrice;


  public int[] count = new int[3];
  public static Integer[] idB = new Integer[]{R.id.adult, R.id.senior, R.id.child};
  Button[] sButtons = new Button[]{adult, senior, child};

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_film_book_detail);
    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
    cookie = sharedPreferences.getString("cookie", "");
    screenId = sharedPreferences.getString("id", "");
    bookButton = findViewById(R.id.book_Dbutton0);
    showPrice = findViewById(R.id.price_total);

    UIInit();


    for (x = 0; x < 3; x++) {
      sButtons[x] = findViewById(idB[x]);
    }
    for (x = 0; x < 3; x++) {
      count[x] = 0;
    }
    for (x = 0; x < 3; x++) {
      sButtons[x].setOnClickListener(this);
    }
    count[0]=1;
    sButtons[0].setBackgroundDrawable(getResources().getDrawable(R.drawable.pay1));

    bookButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(FilmBookDetailActivity.this, FilmActivity.class);
        startActivity(intent);
      }
    });
    freshButton = findViewById(R.id.book_refresh);
    freshButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        UIInit();
      }
    });
  }

  private void UIInit() {
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        OkClient auditoirum = new OkClient(cookie);
        try {
          seatsOfAuditorium = auditoirum.GetAllSeat(new OkClient(cookie).getAuditoruimId(screenId));
          JSONObject size = auditoirum.getSize();
          row = size.getInt("row");
          col = size.getInt("col");
          Log.e("size", row.toString() + ' ' + col.toString());
          seatsTaken = new OkClient(cookie).seatTaken(screenId);
        } catch (Exception e) {
          e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            init(row, col);
          }
        });
      }
    });
    t.start();


    Thread showMovieData = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          screenData = new JSONObject(new OkClient(cookie).getScreenInfo(screenId));
          discountMag = screenData.getDouble("discountedMag");
          originalPrice = screenData.getDouble("originalPrice");
          movieData = new JSONObject(new OkClient(cookie).getMovieInfo(screenData.getString("movieId")));
          Log.e("movieData", movieData.toString());
        } catch (JSONException e) {
          e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try {
              Log.e("finish", "");
              showMovieData();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
      }
    });
    showMovieData.start();

  }

  public void dialog(View v) {
    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    String cookie = sharedPreferences.getString("cookie", "");
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setIcon(R.mipmap.ic_launcher_round);
    if (cookie != "") {
      if (count1 == 0) {
        builder.setTitle("Caution!");
        builder.setMessage("You haven't chosen any seats!");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Toast.makeText(FilmBookDetailActivity.this, "Retry", Toast.LENGTH_SHORT).show();
          }
        });
//            builder.setNegativeButton("cancel", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

      } else {
        builder.setTitle("Order");
        builder.setMessage("You sure your order is as following:\n" + ticketSelected.size() + " tickets");
        builder.setNegativeButton("Pay", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            sendOrder();
            Intent intent = new Intent(FilmBookDetailActivity.this, PayActivity.class);
            startActivity(intent);
          }
        });

        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Thread c = new Thread(new Runnable() {
              @Override
              public void run() {
                try {
                  new OkClient().cancelOrder(cookie);
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            });
            c.start();
            try {
              c.join();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        });

      }
    } else {
      builder.setTitle("Caution");
      builder.setMessage("You haven't login yet\ngo to login?");
      builder.setNegativeButton("Go to Login", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          Intent intent = new Intent(FilmBookDetailActivity.this, LoginActivity.class);
          startActivity(intent);
        }
      });

      builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
        }
      });

    }
    AlertDialog alertDialog = builder.create();
    alertDialog.show();

  }


  private void sendOrder() {
    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          String orderInfo = new OkClient().sendTicket(ticketSelected, cookie, screenId);
          Log.e("order", orderInfo);
          String id = new JSONObject(orderInfo).getString("id");
          Log.e("id", id);
          editor.putString("recentOrderId", id);
          editor.commit();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    t.start();
    try {
      t.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }


  private void init(Integer row, Integer col) {

    LinearLayout layout = findViewById(R.id.seats);
    layout.removeAllViews();
    for (Integer i = 0; i < row; i++) {
      layout.addView(newRow(i + 1, col));
    }
    layout.addView(buttonBar(col));
  }

  private LinearLayout buttonBar(Integer col) {
    LinearLayout line = new LinearLayout(FilmBookDetailActivity.this);

    TextView header = new TextView(FilmBookDetailActivity.this);
    LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dip2px(FilmBookDetailActivity.this, 43));
    header.setGravity(Gravity.CENTER);
    header.setTextSize(15);
    header.setBackgroundResource(R.drawable.shape_screen);
    header.setLayoutParams(textParams);
    line.addView(header);

    Character pos = 'A';
    for (int i = 0; i < col; i++) {
      TextView seat = new TextView(FilmBookDetailActivity.this);
      LinearLayout.LayoutParams seatParams = new LinearLayout.LayoutParams(dip2px(FilmBookDetailActivity.this, 33), dip2px(FilmBookDetailActivity.this, 30), 1);
      seatParams.setMargins(dip2px(FilmBookDetailActivity.this, 10), 0, 0, 0);
      seat.setBackgroundResource(R.drawable.shape_screen);
      seat.setGravity(Gravity.CENTER);
      seat.setLayoutParams(seatParams);
      seat.setText(pos.toString());
      line.addView(seat);
      pos++;
    }
    return line;
  }

  private LinearLayout newRow(Integer row, Integer col) {
    LinearLayout line = new LinearLayout(FilmBookDetailActivity.this);
    line.setId(row);
    TextView header = new TextView(FilmBookDetailActivity.this);
    LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dip2px(FilmBookDetailActivity.this, 43));
    header.setGravity(Gravity.CENTER);
    header.setTextSize(15);
    header.setBackgroundResource(R.drawable.shape_screen);
    header.setLayoutParams(textParams);
    header.setText(row.toString());
    line.addView(header);
    /*
    if (col < 5) {
      col = 5;
    }*/
    for (int i = 0; i < col; i++) {
      Button seat = addSeat(row, i + 1);
      line.addView(seat);
    }
    return line;
  }

  private Button addSeat(Integer row, Integer col) {
    Button seat = new Button(FilmBookDetailActivity.this);
    LinearLayout.LayoutParams seatParams = new LinearLayout.LayoutParams(dip2px(FilmBookDetailActivity.this, 33), dip2px(FilmBookDetailActivity.this, 30), 1);
    seatParams.setMargins(dip2px(FilmBookDetailActivity.this, 10), 0, 0, 0);
    seat.setBackgroundResource(R.drawable.seat_unable);
    seat.setGravity(Gravity.CENTER);
    seat.setLayoutParams(seatParams);
    JSONObject seatData = null;
    Integer seatId = 0;
    final Boolean[] time = {false};

    for (HashMap<HashMap, JSONObject> seatPosAndInfo : seatsOfAuditorium) {
      HashMap<Integer, Integer> position = new HashMap<>();
      position.put(row, col);
      if (seatPosAndInfo.get(position) != null) {
        seatData = seatPosAndInfo.get(position);
        seat.setBackgroundResource(R.drawable.seat_free);
        try {
          if (seatData.getBoolean("isVip")) {
            seat.setBackgroundResource(R.drawable.vip);
          }
          seatId = seatData.getInt("id");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    for (HashMap<HashMap, JSONObject> seatPosAndInfo : seatsTaken) {
      HashMap<Integer, Integer> position = new HashMap<>();
      position.put(row, col);
      if (seatPosAndInfo.get(position) != null) {
        seat.setBackgroundResource(R.drawable.seat_unable);
        seat.setEnabled(false);
      }
    }


    Integer finalSeatId = seatId;
    JSONObject finalSeatData = seatData;
    seat.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!time[0]) {
          seat.setBackgroundDrawable(getResources().getDrawable(R.drawable.seat_check));
          count1++;
          ticketSelected.add("{\"ageType\":"+ageType+",\"seatId\":" + finalSeatId.toString() + "}");
          originalTotalPrice += originalPrice;
          discountedTotalPrice+=originalPrice*discountMag;
          time[0] = true;
        } else {
          try {
            if (finalSeatData.getBoolean("isVip")) {
              seat.setBackgroundDrawable(getResources().getDrawable(R.drawable.vip));
            }else {
              seat.setBackgroundDrawable(getResources().getDrawable(R.drawable.seat_free));
            }

          } catch (Exception e) {
            e.printStackTrace();
          }
          count1--;
          ticketSelected.remove("{\"ageType\":"+ageType+",\"seatId\":" + finalSeatId.toString() + "}");
          originalTotalPrice -= originalPrice;
          discountedTotalPrice-=originalPrice*discountMag;
          time[0] = false;
        }
        if(originalTotalPrice<0){
          originalTotalPrice=0;
        }
        if(discountedTotalPrice<0){
          discountedTotalPrice=0;
        }
        showPrice();
      }
    });
    return seat;
  }

  private void showPrice(){
    BigDecimal t = new BigDecimal(showDiscount?discountedTotalPrice:originalTotalPrice);
    showPrice.setText("Total Price: " + t.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
  }

  private void showMovieData() throws JSONException {
    ImageView cover = findViewById(R.id.cover);
    TextView title = findViewById(R.id.title);
    TextView info = findViewById(R.id.info);

    String coverName = movieData.getString("cover");

    title.setText(movieData.getString("name"));
    info.setText("In " + screenData.getString("date") + "\nfrom " + screenData.getString("time") + " to " + screenData.getString("finishTime"));
    if (coverName != "null") {
      Thread m = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Bitmap bitmap = new OkClient(cookie).getImg(coverName);
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
  }

  private int dip2px(Context context, float dipValue) {
    Resources r = context.getResources();
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, r.getDisplayMetrics());
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
      ageType = ageTypeList[x];
      if(ageType!=ageTypeList[0]){
        showDiscount = true;
      }else{
        showDiscount =false;
      }
      for (c = 0; c < 3; c++) {
        if (c != x)
          sButtons[c].setBackgroundDrawable(getResources().getDrawable(R.drawable.pay0));
        count[c]--;
      }
      count[x]++;
    }
    showPrice();
  }
}


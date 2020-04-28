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
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.myapplication.data.OkClient;
import com.example.myapplication.data.pixelTools;
import com.example.myapplication.ui.login.LoginActivity;

import org.json.JSONArray;
import org.json.JSONObject;


import android.widget.DatePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ScreenActivity extends AppCompatActivity {
  private Button screenButton, userButton, cinemaButton,datePickButton;
  private String cookie, movieData, screenData;
  private String date = "";
  private LinearLayout covers, screens;
  int year, month, day;
  public int tag = 1,todayOrTomorrowOrSearch=0;
  DatePicker datePicker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_screen);
    screenButton = findViewById(R.id.screen);
    userButton = findViewById(R.id.user);
    cinemaButton = findViewById(R.id.cinema);
   
    datePickButton =findViewById(R.id.datepick);

    covers = findViewById(R.id.covers);
    screens = findViewById(R.id.screens);


    datePicker = findViewById(R.id.datePicker1);
    ((LinearLayout) ((ViewGroup) datePicker.getChildAt(0)).getChildAt(0)).setVisibility(View.GONE);
    Calendar calendar = Calendar.getInstance();
    year = calendar.get(Calendar.YEAR);
    month = calendar.get(Calendar.MONTH);
    day = calendar.get(Calendar.DAY_OF_MONTH);

    datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
      @Override
      public void onDateChanged(DatePicker datePicker, int year1, int month1, int day1) {

        ScreenActivity.this.year = year1;
        ScreenActivity.this.month = month1+1;
        ScreenActivity.this.day = day1;
        //show(year, month, day);
        date = String.valueOf(year) + '-' + String.valueOf(month) + '-' + String.valueOf(day);
        screenSearch(date);
        LinearLayout off = findViewById(R.id.date_picker);
        off.setVisibility(View.INVISIBLE);
        tag = 1;
      }
    });


    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
    cookie = sharedPreferences.getString("cookie", "");

   

    datePickButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        change();
        todayOrTomorrowOrSearch =2;
       
        datePickButton.setBackgroundResource(R.color.date);
      }
    });

    screenButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(ScreenActivity.this, ScreenActivity.class);
        startActivity(intent);
      }
    });

    userButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = null;
        if (cookie != null && cookie != "") {
          intent = new Intent(ScreenActivity.this, FilmUserActivity.class);
        } else {
          intent = new Intent(ScreenActivity.this, LoginActivity.class);
        }
        startActivity(intent);
      }
    });

    cinemaButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(ScreenActivity.this, FilmActivity.class);
        startActivity(intent);
      }
    });


    init();
  }


  private void show(int i, int i1, int i2) {
    String str = i + "年" + (1 + i1) + "月" + i2 + '日';
    //用Toast显示变化后的日期
    Toast.makeText(ScreenActivity.this, str, Toast.LENGTH_SHORT).show();
  }


  public void date_picker(View view) {
    change();
  }

  public void change() {
    if (tag == 0) {
      LinearLayout off = findViewById(R.id.date_picker);
      off.setVisibility(View.INVISIBLE);
      tag = 1;
    } else {
      LinearLayout off = findViewById(R.id.date_picker);
      off.setVisibility(View.VISIBLE);
      tag = 0;
    }
  }

  private void screenSearch(String date){
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        screenData = new OkClient().screenSearch(date,null,0,20);
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try{
              screens.removeAllViews();
              JSONArray screensData = new JSONArray(screenData);
              if(screensData.length()<=0){
                showNoData();
              }else {
                for (int i = 0; i < screensData.length(); i++) {
                  addNewScreen(screensData.getJSONObject(i));
                }
              }
            }catch (Exception e){
              e.printStackTrace();
            }
          }
        });
      }
    });
    t.start();
  }

  private void init() {
    Date today = new Date(System.currentTimeMillis());
    String date = new SimpleDateFormat("yyyy-MM-dd").format(today);
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          movieData = new OkClient().getReleasedOrNotMovie(1);
        }catch (Exception e){
          e.printStackTrace();
        }
        screenData = new OkClient().getScreensData();
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try {
              covers.removeAllViews();
              screens.removeAllViews();
              JSONArray moviesData = new JSONArray(movieData);
              JSONArray screensData = new JSONArray(screenData);
              for (int i = 0; i < moviesData.length(); i++) {
                addNewMovie(moviesData.getJSONObject(i));
              }
              screenSearch(date);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
      }
    });
    t.start();

  }

  private void addNewMovie(JSONObject movieData) throws Exception {
    ImageView cover = new ImageView(ScreenActivity.this);
    cover.setClickable(true);
    LinearLayout.LayoutParams coverParams = new LinearLayout.LayoutParams(pixelTools.dip2px(ScreenActivity.this, 100), ViewGroup.LayoutParams.MATCH_PARENT, 0);
    coverParams.setMargins(pixelTools.dip2px(ScreenActivity.this, 10), 0, 0, 0);
    cover.setImageResource(R.mipmap.ic_launcher_round);
    cover.setBackgroundColor(Color.parseColor("#f1f1f1"));
    cover.setLayoutParams(coverParams);

    String coverName = movieData.getString("cover");
    String id = movieData.getString("id");

    cover.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("movie", id);
        editor.commit();
        Intent intent = new Intent(ScreenActivity.this, FilmBookActivity.class);
        startActivity(intent);

      }
    });


    if (coverName != "null") {
      Thread m = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Log.e("cookie", cookie);
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
    covers.addView(cover);
  }

  private void addNewScreen(JSONObject screenData) throws Exception {
    Integer movieId = screenData.getInt("movieId");
    Log.e("id",movieId.toString());

    int margin10 = pixelTools.dip2px(ScreenActivity.this, 10);
    int margin20 = pixelTools.dip2px(ScreenActivity.this, 20);

    LinearLayout screenLayout = new LinearLayout(ScreenActivity.this);
    LinearLayout.LayoutParams SLParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pixelTools.dip2px(ScreenActivity.this, 80));
    SLParams.setMargins(margin20, margin10, margin20, 0);
    screenLayout.setPadding(0, 0, 0, 1);
    screenLayout.setOrientation(LinearLayout.HORIZONTAL);
    screenLayout.setBackgroundColor(Color.parseColor("#000000"));
    screenLayout.setLayoutParams(SLParams);


    ImageView cover = new ImageView(ScreenActivity.this);
    LinearLayout.LayoutParams coverParams = new LinearLayout.LayoutParams(pixelTools.dip2px(ScreenActivity.this, 100), ViewGroup.LayoutParams.MATCH_PARENT, 4);
    cover.setImageResource(R.mipmap.ic_launcher_round);
    cover.setBackgroundColor(Color.parseColor("#f1f1f1"));
    cover.setLayoutParams(coverParams);
      Thread m = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            JSONObject movie = new JSONObject(new OkClient().getMovieInfo(movieId.toString()));
            String coverName = movie.getString("cover");
            Log.e("cookie", cookie);
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


    LinearLayout infoLayout = new LinearLayout(ScreenActivity.this);
    LinearLayout.LayoutParams ILParams = new LinearLayout.LayoutParams(pixelTools.dip2px(ScreenActivity.this, 265), ViewGroup.LayoutParams.MATCH_PARENT);
    infoLayout.setPadding(0, 0, 0, pixelTools.dip2px(ScreenActivity.this, 8));
    infoLayout.setBackgroundColor(Color.parseColor("#ffffff"));
    infoLayout.setOrientation(LinearLayout.HORIZONTAL);
    infoLayout.setGravity(Gravity.RIGHT);
    infoLayout.setLayoutParams(ILParams);

    TextView info = new TextView(ScreenActivity.this);
    LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(pixelTools.dip2px(ScreenActivity.this, 250), ViewGroup.LayoutParams.MATCH_PARENT, 8);
    info.setBackgroundColor(Color.parseColor("#ffffff"));
    info.setGravity(Gravity.CENTER);
    info.setTextColor(Color.parseColor("#000000"));
    info.setTextSize(15);
    info.setLayoutParams(infoParams);
    info.setText("In "+screenData.getString("date")+"\nfrom " + screenData.getString("time") + " to " + screenData.getString("finishTime") + "\nprice: " + screenData.getString("originalPrice"));


    infoLayout.addView(info);
    screenLayout.addView(cover);
    screenLayout.addView(infoLayout);

    //把所有东西塞进layout中

    screens.addView(screenLayout);
    String id = screenData.getString("id");
    screenLayout.setClickable(true);
    screenLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(ScreenActivity.this, FilmBookDetailActivity.class);
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("id", id);
        editor.commit();
        startActivity(intent);
      }
    });
  }

  private void showNoData(){
    TextView nodata = new TextView(ScreenActivity.this);
    LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 8);
    nodata.setBackgroundColor(Color.parseColor("#ffffff"));
    nodata.setGravity(Gravity.CENTER);
    nodata.setTextColor(Color.parseColor("#000000"));
    nodata.setTextSize(15);
    nodata.setLayoutParams(infoParams);
    nodata.setText("No screenings on this day");
    screens.addView(nodata);

  }

}




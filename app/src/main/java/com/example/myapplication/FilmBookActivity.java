package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.example.myapplication.data.OkClient;
import com.example.myapplication.data.pixelTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class FilmBookActivity extends AppCompatActivity {
  private Button rbutton,fbutton,datePickButton;
  private String cookie = null,movieId;
  private LinearLayout screenlist;
  public int tag = 1;
  private String date = "";
  int year, month, day;
  DatePicker datePicker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Date today = new Date(System.currentTimeMillis());
    date = new SimpleDateFormat("yyyy-MM-dd").format(today);
    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    cookie = sharedPreferences.getString("cookie","");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_film_book);
    rbutton = findViewById(R.id._return);
    fbutton = findViewById(R.id._refresh);




    datePicker = findViewById(R.id.datePicker1);
    ((LinearLayout) ((ViewGroup) datePicker.getChildAt(0)).getChildAt(0)).setVisibility(View.GONE);
    Calendar calendar = Calendar.getInstance();
    year = calendar.get(Calendar.YEAR);
    month = calendar.get(Calendar.MONTH);
    day = calendar.get(Calendar.DAY_OF_MONTH);

    datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
      @Override
      public void onDateChanged(DatePicker datePicker, int year1, int month1, int day1) {
        FilmBookActivity.this.year = year1;
        FilmBookActivity.this.month = month1+1;
        FilmBookActivity.this.day = day1;
        //show(year, month, day);
        date = String.valueOf(year) + '-' + String.valueOf(month) + '-' + String.valueOf(day);
        LinearLayout off = findViewById(R.id.date_picker);
        init(date);
        off.setVisibility(View.INVISIBLE);
        tag = 1;
      }
    });


    screenlist = findViewById(R.id.screens_list);
    init(date);
    rbutton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        editor.remove("movie");
        Intent intent = new Intent(FilmBookActivity.this,FilmActivity.class);
        startActivity(intent);
      }
    });

    fbutton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        init(date);
      }
    });
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
        String screenData = new OkClient().screenSearch(date,movieId,0,20);
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try{
              screenlist.removeAllViews();
              JSONArray screensData = new JSONArray(screenData);
              if(screensData.length()<=0){
                showNoData();
              }else {
                for (int i = 0; i < screensData.length(); i++) {
                  //addNewScreen(screensData.getJSONObject(i));
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



  private void init(String date) {
    screenlist.removeAllViews();
    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    movieId = sharedPreferences.getString("movie", "");
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        String movieInfo = new OkClient().getMovieInfo(movieId);
        //String movieScreens = new OkClient().getMovieScreen(movieId);
        String movieScreens = new OkClient().screenSearch(date,movieId,0,20);
        try {
          JSONArray screens = new JSONArray(movieScreens);
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              try {
                if(screens.length()!=0) {
                  for (int i = 0; i < screens.length(); i++) {
                    JSONObject screen = screens.getJSONObject(i);
                    screenlist.addView(setScreensLayout(screen.toString()));

                  }
                }else{
                  showNoData();
                }
                setMovieInfo(movieInfo);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          });
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    t.start();

  }

  private void setMovieInfo(String data)throws Exception {
    JSONObject movieInfo = new JSONObject(data);

    String name = movieInfo.getString("name");
    String blurb = movieInfo.getString("blurb");
    String coverName = movieInfo.getString("cover");
    String director = movieInfo.getString("director");

    //生成包含所有actor的列表
    String[] actors = movieInfo.getString("leadActors").split(",");

    ImageView coverV = findViewById(R.id.cover);
    TextView titleV = findViewById(R.id.name);
    TextView blurbV = findViewById(R.id.blurb);

    titleV.setText(name);
    blurbV.setText(blurb);

    if(coverName!="null"){
      Thread m = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Bitmap bitmap = new OkClient(cookie).getImg(coverName);
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                coverV.setImageBitmap(bitmap);
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

  private LinearLayout setScreensLayout(String data) throws Exception {
    JSONObject screen = new JSONObject(data);

    String id = screen.getString("id");
    String date = screen.getString("date");
    String startTime = screen.getString("time");
    String finishTime = screen.getString("finishTime");
    String price = screen.getString("originalPrice");
    String room = screen.getString("auditoriumId");

    LinearLayout layout = new LinearLayout(FilmBookActivity.this);

    int padding = pixelTools.dip2px(FilmBookActivity.this, 2);
    int margin = pixelTools.dip2px(FilmBookActivity.this, 10);

    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, pixelTools.dip2px(FilmBookActivity.this, 120));
    layoutParams.setMargins(margin, margin, margin, margin);
    layout.setPadding(padding, padding, padding, padding);
    layout.setBackgroundColor(Color.parseColor("#000000"));
    layout.setOrientation(LinearLayout.HORIZONTAL);
    layout.setLayoutParams(layoutParams);

    TextView content = new TextView(FilmBookActivity.this);
    LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(pixelTools.dip2px(FilmBookActivity.this, 10), LinearLayout.LayoutParams.MATCH_PARENT, 8);
    content.setBackgroundColor(Color.parseColor("#146974"));
    content.setTextColor(Color.parseColor("#ffffff"));
    content.setTextSize(20);
    content.setLayoutParams(contentParams);
    content.setText("date: "+date+"\nfrom "+startTime+" to "+finishTime+"\nprice: "+price+"\nroom: "+room);

    Button ticketing = new Button(FilmBookActivity.this);
    LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(pixelTools.dip2px(FilmBookActivity.this, 10), LinearLayout.LayoutParams.MATCH_PARENT, 3);
    ticketing.setBackgroundResource(R.drawable.ticket);
    ticketing.setLayoutParams(buttonParams);

    layout.addView(content);
    layout.addView(ticketing);

    ticketing.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(FilmBookActivity.this,FilmBookDetailActivity.class);
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("id",id);
        editor.commit();
        startActivity(intent);
      }
    });

    return layout;

  }
  private void showNoData(){
    TextView nodata = new TextView(FilmBookActivity.this);
    LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 8);
    nodata.setBackgroundColor(Color.parseColor("#ffffff"));
    nodata.setGravity(Gravity.CENTER);
    nodata.setTextColor(Color.parseColor("#000000"));
    nodata.setTextSize(15);
    nodata.setLayoutParams(infoParams);
    nodata.setText("No screenings on this day");
    screenlist.addView(nodata);

  }
}


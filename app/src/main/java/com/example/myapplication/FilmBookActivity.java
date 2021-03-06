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

/**this is the activity to show the detail of the movie
 * it will read the movie id which is stored with key word "movie" in local storage in advance
 * it will build a page showing all information of the movie and screenings
 * it shows screenings of today as default and enables users to select different days
 * users can jump to other hot-showing movies by click the movies in the hot-showing bar*/

public class FilmBookActivity extends AppCompatActivity {
  private Button rbutton,fbutton,datePickButton;
  private String cookie = null,movieId,movieData;
  private LinearLayout screenlist,covers;
  public int tag = 1;
  private String date = "";
  int year, month, day;
  DatePicker datePicker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    //initialize today's date
    Date today = new Date(System.currentTimeMillis());
    date = new SimpleDateFormat("yyyy-MM-dd").format(today);

    //get the movie id in local storage
    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    cookie = sharedPreferences.getString("cookie","");

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_film_book);

    //initialize widgets
    rbutton = findViewById(R.id._return);
    fbutton = findViewById(R.id._refresh);
    covers = findViewById(R.id.covers);

    //initialize date picker widgets
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

    //initialize the screenlist
    screenlist = findViewById(R.id.screens_list);
    init(date);

    //set return button
    rbutton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        editor.remove("movie");
        Intent intent = new Intent(FilmBookActivity.this,FilmActivity.class);
        startActivity(intent);
        finish();
      }
    });

    //set refresh button
    fbutton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        init(date);
      }
    });
  }



  //set date picker's function
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

  //set hot-showing bar's movies
  private void addNewMovie(JSONObject movieData) throws Exception {
    ImageView cover = new ImageView(FilmBookActivity.this);
    cover.setClickable(true);
    LinearLayout.LayoutParams coverParams = new LinearLayout.LayoutParams(pixelTools.dip2px(FilmBookActivity.this, 100), ViewGroup.LayoutParams.MATCH_PARENT, 0);
    coverParams.setMargins(pixelTools.dip2px(FilmBookActivity.this, 10), 0, 0, 0);
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
        Date today = new Date(System.currentTimeMillis());
        date = new SimpleDateFormat("yyyy-MM-dd").format(today);
        init(date);

      }
    });

    //set hot-showing movies' posters
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


  //initialize the screen list by different days and initialize the movie data
  private void init(String date) {
    screenlist.removeAllViews();
    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    movieId = sharedPreferences.getString("movie", "");
    Thread m = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          movieData = new OkClient().getReleasedOrNotMovie(1);
        }catch (Exception e){
          e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try {
              covers.removeAllViews();
              JSONArray moviesData = new JSONArray(movieData);
              for (int i = 0; i < moviesData.length(); i++) {
                addNewMovie(moviesData.getJSONObject(i));
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
      }
    });
    m.start();

    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        String movieInfo = new OkClient().getMovieInfo(movieId);
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

  //set the movie data into the widgets
  private void setMovieInfo(String data)throws Exception {
      JSONObject movieInfo = new JSONObject(data);

      String name = movieInfo.getString("name");
      String blurb = movieInfo.getString("blurb");
      String coverName = movieInfo.getString("cover");
      String director = movieInfo.getString("director");

      ImageView coverV = findViewById(R.id.cover);
      TextView titleV = findViewById(R.id.name);
      TextView blurbV = findViewById(R.id.blurb);

      titleV.setText(name);
      blurbV.setText(blurb);

      if (!coverName.equals("null")) {
          setTitle(name);//Title of activity
          if (coverName != "null") {
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
  }

  //set the screen list with the iniliazed screen data
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
    content.setText("date: "+date+"\n"+startTime+"-"+finishTime+"\nprice: "+price+"\nroom: "+room);

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
        editor.apply();
        startActivity(intent);
      }
    });

    return layout;

  }

  //when no data available, give some information
  private void showNoData(){
    screenlist.removeAllViews();
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


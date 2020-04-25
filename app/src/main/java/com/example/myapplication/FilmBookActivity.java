package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.data.OkClient;
import com.example.myapplication.data.pixelTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class FilmBookActivity extends AppCompatActivity {
  private Button rbutton,fbutton;
  private String cookie = null;
  private LinearLayout screenlist;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    cookie = sharedPreferences.getString("cookie","");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_film_book);
    rbutton = findViewById(R.id._return);
    fbutton = findViewById(R.id._refresh);

    screenlist = findViewById(R.id.screens_list);
    init();
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
        init();
      }
    });
  }

  private void init() {
    screenlist.removeAllViews();
    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    String movieId = sharedPreferences.getString("movie", "");
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        String movieInfo = new OkClient().getMovieInfo(movieId);
        String movieScreens = new OkClient().getMovieScreen(movieId);
        try {
          JSONArray screens = new JSONArray(movieScreens);
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              try {
                for (int i = 0; i < screens.length(); i++) {
                  JSONObject screen = screens.getJSONObject(i);
                  screenlist.addView(setScreensLayout(screen.toString()));

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
}


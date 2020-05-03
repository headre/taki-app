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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.data.OkClient;
import com.example.myapplication.data.pixelTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**this is the activity for users to search movies and show search results*/

public class FilmSearchActivity extends AppCompatActivity {
    private Button Button,cinemaButton,userButton,screenButton,searchButton;
    private String keyword,resultList;
    private EditText editText;
    private LinearLayout resultDisplayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_film_search);

        //initialize widgets
        cinemaButton = findViewById(R.id.cinema);
        userButton = findViewById(R.id.user);
        screenButton = findViewById(R.id.screen);
        Button= findViewById(R.id.search_return);
        searchButton = findViewById(R.id.search_button);
        editText = findViewById(R.id.search_view);
        resultDisplayList = findViewById(R.id.results_list);

        //if search function is called in other pages, read the keyword first
        keyword = getIntent().getStringExtra("keyword");
        editText.setText(keyword);

        Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //return
                Intent intent = new Intent(FilmSearchActivity.this, FilmActivity.class);
                startActivity(intent);
                finish();
            }
        });


        cinemaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //return
                Intent intent = new Intent(FilmSearchActivity.this, FilmActivity.class);
                startActivity(intent);
                finish();
            }
        });

        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //return
                Intent intent = new Intent(FilmSearchActivity.this, FilmUserActivity.class);
                startActivity(intent);
                finish();
            }
        });

        screenButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent  intent = new Intent(FilmSearchActivity.this,ScreenActivity.class);
            startActivity(intent);
            finish();
          }
        });

        //call search method
        searchButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            keyword = editText.getText().toString();
            showResult();
          }
        });
        initResult();
    }

    //initialize the search result
    private void initResult(){
      if(keyword!=null&&keyword!=""){
        showResult();
      }
    }

    //method to search and show the result
    private void showResult(){
      resultDisplayList.removeAllViews();
      if(keyword!=null&&keyword!=""){
        Thread t = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              String response = new OkClient().getSearch(keyword);
              if(new JSONObject(response).has("content")) {
                resultList = new JSONObject(new OkClient().getSearch(keyword)).getString("content");
              }
            }catch (Exception e){
              e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                try {
                  if(resultList!=null) {
                  JSONArray results = new JSONArray(resultList);
                    for (int i = 0; i < results.length(); i++) {
                      addResult(results.getJSONObject(i));
                    }
                  }
                } catch (JSONException e) {
                  e.printStackTrace();
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
            });
          }
        });
        t.start();
      }
    }

    //add the movies that corresponds to the keyword into the list
    private void addResult(JSONObject result) throws Exception{
      int margin10 = pixelTools.dip2px(FilmSearchActivity.this,10);
      int margin20 = pixelTools.dip2px(FilmSearchActivity.this,20);

      String coverName = result.getString("cover");
      String name = result.getString("name");
      String blurb = result.getString("blurb");
      String id = result.getString("id");

      LinearLayout resultLayout = new LinearLayout(FilmSearchActivity.this);
      LinearLayout.LayoutParams SLParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pixelTools.dip2px(FilmSearchActivity.this,143));
      SLParams.setMargins(margin20,margin10,margin20,0);
      resultLayout.setPadding(0,0,0,1);
      resultLayout.setOrientation(LinearLayout.HORIZONTAL);
      resultLayout.setBackgroundColor(Color.parseColor("#000000"));
      resultLayout.setLayoutParams(SLParams);

      ImageView cover = new ImageView(FilmSearchActivity.this);
      LinearLayout.LayoutParams imgViewParams = new LinearLayout.LayoutParams(pixelTools.dip2px(FilmSearchActivity.this,108),ViewGroup.LayoutParams.MATCH_PARENT,4);
      cover.setLayoutParams(imgViewParams);
      cover.setImageResource(R.mipmap.ic_launcher_round);
      cover.setBackgroundColor(Color.parseColor("#ECECEC"));
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

      //set layout and texts
      LinearLayout movieText = new LinearLayout(FilmSearchActivity.this);
      LinearLayout.LayoutParams textBox = new LinearLayout.LayoutParams(pixelTools.dip2px(FilmSearchActivity.this,265),ViewGroup.LayoutParams.MATCH_PARENT);
      movieText.setOrientation(LinearLayout.VERTICAL);
      movieText.setBackgroundColor(Color.parseColor("#ffffff"));
      movieText.setGravity(Gravity.RIGHT);
      movieText.setLayoutParams(textBox);

      TextView context = new TextView(FilmSearchActivity.this);
      LinearLayout.LayoutParams contextparams = new LinearLayout.LayoutParams(pixelTools.dip2px(FilmSearchActivity.this,250),ViewGroup.LayoutParams.MATCH_PARENT,8);
      context.setBackgroundColor(Color.parseColor("#ffffff"));
      context.setGravity(Gravity.CENTER);
      context.setTextColor(Color.parseColor("#000000"));
      context.setTextSize(18);
      context.setLayoutParams(contextparams);
      context.setText(name+"\n"+blurb);

      movieText.addView(context);
      resultLayout.addView(cover);
      resultLayout.addView(movieText);

      resultDisplayList.addView(resultLayout);

      resultLayout.setClickable(true);
      resultLayout.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
          SharedPreferences.Editor editor = sharedPreferences.edit();
          editor.putString("movie",id);
          editor.commit();
          Intent intent = new Intent(FilmSearchActivity.this,FilmBookActivity.class);
          startActivity(intent);
          finish();
        }
      });
    }

}

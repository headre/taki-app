package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.data.OkClient;
import com.example.myapplication.data.QRCodeUtil;
import com.example.myapplication.data.pixelTools;
import com.example.myapplication.ui.login.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TicketActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private Button uButton, cButton, screenButton;
    private TextView title, body;
    private ScrollView scrollView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private String cookie, orderList;
    private LinearLayout orders, ticketlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        ticketlist = findViewById(R.id.orders_list);
        scrollView = findViewById(R.id.scroll_view);
        cButton = findViewById(R.id.cinema);
        screenButton = findViewById(R.id.screen);
        uButton = findViewById(R.id.user);

        //获取本地存储数据
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        cookie = sharedPreferences.getString("cookie", "");
        swipeRefreshLayout.setOnRefreshListener(this);
        init();

        cButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TicketActivity.this, FilmActivity.class);
                startActivity(intent);
            }
        });

        //用户界面模块

        uButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                String cookie = sharedPreferences.getString("cookie", "");
                if (cookie != null && cookie != "") {
                    intent = new Intent(TicketActivity.this, FilmUserActivity.class);
                } else {
                    intent = new Intent(TicketActivity.this, LoginActivity.class);
                }
                startActivity(intent);
            }
        });


        screenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TicketActivity.this, ScreenActivity.class);
                startActivity(intent);
            }
        });
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
                            ticketlist.removeAllViews();
                            JSONArray orderArray = null;
                            if (orderList != null) {
                                orderArray = new JSONArray(orderList);
                                for (int i = 0; i < orderArray.length(); i++) {
                                    if (Double.valueOf(orderArray.getJSONObject(i).getString("totalCost")) != 0) {
                                        addOrder(orderArray.getJSONObject(i));
                                    }
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
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                View contentView = scrollView.getChildAt(0);

                if (contentView.getMeasuredHeight() == scrollView.getScrollY() + scrollView.getHeight()) {

                }
                swipeRefreshLayout.setEnabled(scrollView.getScrollY() == 0);
                if (scrollView.getScrollY() > 0) {
                }
            }
        });
    }


    private void addOrder(JSONObject order) throws JSONException {
        Integer id = order.getInt("id");
        String dateString = order.getString("createdAt");
        Double totalCost = order.getDouble("totalCost");
        TextView title = new TextView(TicketActivity.this);
        JSONArray tickets = new JSONArray(order.getString("tickets"));
        String number = String.valueOf(tickets.length());
        JSONObject baseScreening = new JSONObject(tickets.getJSONObject(0).getString("screening"));

        Thread t = new Thread(new Runnable() {
            @Override
            public void run(){
                JSONObject movieData = null;
                String movieId = null;
                String name = null;
                String covername = null;
                try {
                    movieData = new JSONObject(new OkClient().getMovieInfo(baseScreening.getString("movieId")));
                    Log.e("movieData",movieData.toString());
                    movieId = movieData.getString("id");
                    name = movieData.getString("name");
                    covername = movieData.getString("cover");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                //新建最外层layout
                LinearLayout layout = new LinearLayout(TicketActivity.this);

                //设置相关margin和padding
                int margin = dip2px(TicketActivity.this, 20);
                int margin10 = pixelTools.dip2px(TicketActivity.this, 10);
                int lpadding = dip2px(TicketActivity.this, 1);
                int tpadding = dip2px(TicketActivity.this, 8);


                LinearLayout.LayoutParams lLayoutlayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip2px(TicketActivity.this, 143));
                lLayoutlayoutParams.setMargins(margin, margin10, margin, 0);
                layout.setLayoutParams(lLayoutlayoutParams);
                // 设置属性
                layout.setBackgroundColor(Color.parseColor("#000000"));    //
                layout.setPadding(0, 0, 0, 1);
                layout.setOrientation(LinearLayout.HORIZONTAL);

                //以下为下载图片部分
                ImageView cover = new ImageView(TicketActivity.this);
                LinearLayout.LayoutParams imgViewParams = new LinearLayout.LayoutParams(dip2px(TicketActivity.this, 108), ViewGroup.LayoutParams.MATCH_PARENT, 4);
                cover.setLayoutParams(imgViewParams);
                cover.setImageResource(R.mipmap.ic_launcher_round);
                cover.setBackgroundColor(Color.parseColor("#ECECEC"));
                if (covername != "null") {
                    String finalCovername = covername;
                    Thread m = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.e("cookie", cookie);
                                Bitmap bitmap = new OkClient(cookie).getImg(finalCovername);
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


                //设置文本
                LinearLayout movieText = new LinearLayout(TicketActivity.this);
                LinearLayout.LayoutParams textBox = new LinearLayout.LayoutParams(dip2px(TicketActivity.this, 265), ViewGroup.LayoutParams.MATCH_PARENT);
                movieText.setPadding(tpadding, tpadding, tpadding, tpadding);
                movieText.setOrientation(LinearLayout.VERTICAL);
                movieText.setBackgroundColor(Color.parseColor("#ffffff"));
                movieText.setGravity(Gravity.RIGHT);
                movieText.setLayoutParams(textBox);

                LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(dip2px(TicketActivity.this, 250), ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                title.setBackgroundColor(Color.parseColor("#ffffff"));
                title.setGravity(Gravity.LEFT);
                title.setTextColor(Color.parseColor("#000000"));
                title.setTextSize(20);
                title.setLayoutParams(titleParams);
                title.setText(name + " " + number + " tickets");

                TextView context = new TextView(TicketActivity.this);
                LinearLayout.LayoutParams contextparams = new LinearLayout.LayoutParams(dip2px(TicketActivity.this, 250), ViewGroup.LayoutParams.WRAP_CONTENT, 3);
                context.setBackgroundColor(Color.parseColor("#ffffff"));
                context.setGravity(Gravity.LEFT);
                context.setTextColor(Color.parseColor("#000000"));
                context.setTextSize(18);
                context.setLayoutParams(contextparams);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = null;
                String dateS = null;
                try {
                    date = sdf.parse(dateString);
                    dateS = sdf.format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                context.setText("Total price: " + totalCost + "\n Created at  " + dateS);

                layout.setClickable(true);
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("recentOrderId", String.valueOf(id));
                        editor.commit();
                        Intent intent = new Intent(TicketActivity.this, CodeActivity.class);
                        startActivity(intent);
                    }
                });
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        movieText.addView(title);
                        movieText.addView(context);

                        //把所有东西塞进layout中
                        layout.addView(cover);
                        layout.addView(movieText);
                        ticketlist.addView(layout);

                    }
                });
            }
        });
        t.start();

    }

    private int dip2px(Context context, float dipValue) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, r.getDisplayMetrics());
    }


    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        init();
        Log.e("fresh", "finished");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);
    }

    private void showNoData() {
        TextView nodata = new TextView(TicketActivity.this);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 8);
        nodata.setBackgroundColor(Color.parseColor("#ffffff"));
        nodata.setGravity(Gravity.CENTER);
        nodata.setTextColor(Color.parseColor("#000000"));
        nodata.setTextSize(15);
        nodata.setLayoutParams(infoParams);
        nodata.setText("No movies");
        ticketlist.addView(nodata);

    }
}

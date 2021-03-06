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

/**this is the activity to show the orders of current user who has logined
 * it will generate widgets according to how many data it receives
 * it will refresh when the user scroll to the head of the page */

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

        //initialize widgets
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        ticketlist = findViewById(R.id.orders_list);
        scrollView = findViewById(R.id.scroll_view);
        cButton = findViewById(R.id.cinema);
        screenButton = findViewById(R.id.screen);
        uButton = findViewById(R.id.user);

        //read local cookie
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        cookie = sharedPreferences.getString("cookie", "");
        swipeRefreshLayout.setOnRefreshListener(this);
        init();

        //jump to unreleased movies page
        cButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TicketActivity.this, FilmActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //button to jump to the user page( if not logined-without local cookie, jump to login page)
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
                finish();
            }
        });


        //jump to hot showing page
        screenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TicketActivity.this, ScreenActivity.class);
                startActivity(intent);
            }
        });
    }

    //refresh when it is restarted
    @Override
    protected void onRestart(){
        super.onRestart();
        init();
    }

    //get the orders data and build UI
    private void init() {
        ticketlist.removeAllViews();
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
                                    JSONObject order = orderArray.getJSONObject(i);
                                    Log.e("order",order.toString());
                                    Log.e("completed",order.getString("completed"));
                                    JSONArray tickets = order.getJSONArray("tickets");
                                    Log.e("length",String.valueOf(tickets.length()));
                                    Boolean show  = false;
                                    if(tickets.length()!=0) {
                                        if (order.getBoolean("refunded")) {
                                            show = false;
                                        } else if (order.getBoolean("completed") && !tickets.getJSONObject(0).getString("validation").equals("null")) {
                                            show = true;
                                        } else if (!order.getBoolean("completed") && tickets.getJSONObject(0).getString("validation").equals("null")) {
                                            show = true;
                                        }
                                    }
                                    if(show){
                                        addOrder(order);
                                    }
                                }
                            }else{
                                showNoData();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });
        t.start();

        //ensure swipe refresh will only be enabled when user is at the top of the page
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


    //function to return a layout with movies data input
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
                Boolean statusB = null;
                try {
                    movieData = new JSONObject(new OkClient().getMovieInfo(baseScreening.getString("movieId")));
                    Log.e("movieData",movieData.toString());
                    movieId = movieData.getString("id");
                    name = movieData.getString("name");
                    covername = movieData.getString("cover");
                    statusB = order.getBoolean("completed");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                //build outer layout
                LinearLayout layout = new LinearLayout(TicketActivity.this);

                //set margin and padding variables
                int margin = dip2px(TicketActivity.this, 20);
                int margin10 = pixelTools.dip2px(TicketActivity.this, 10);
                int lpadding = dip2px(TicketActivity.this, 1);
                int tpadding = dip2px(TicketActivity.this, 8);

                //set layout params
                LinearLayout.LayoutParams lLayoutlayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip2px(TicketActivity.this, 143));
                lLayoutlayoutParams.setMargins(margin, margin10, margin, 0);
                layout.setLayoutParams(lLayoutlayoutParams);
                layout.setBackgroundColor(Color.parseColor("#000000"));    //
                layout.setPadding(0, 0, 0, 1);
                layout.setOrientation(LinearLayout.HORIZONTAL);

                //download the poster
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


                //set the orders' text information layout
                LinearLayout movieText = new LinearLayout(TicketActivity.this);
                LinearLayout.LayoutParams textBox = new LinearLayout.LayoutParams(dip2px(TicketActivity.this, 265), ViewGroup.LayoutParams.MATCH_PARENT);

                //set the orders' text information
                movieText.setOrientation(LinearLayout.VERTICAL);
                movieText.setBackgroundColor(Color.parseColor("#000000"));
                movieText.setGravity(Gravity.RIGHT);
                movieText.setLayoutParams(textBox);

                LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                title.setBackgroundColor(Color.parseColor("#ffffff"));
                title.setGravity(Gravity.LEFT);
                title.setTextColor(Color.parseColor("#000000"));
                title.setTextSize(20);
                title.setLayoutParams(titleParams);
                title.setText(name + " " + number + " tickets");

                TextView context = new TextView(TicketActivity.this);
                LinearLayout.LayoutParams contextparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 3);
                context.setBackgroundColor(Color.parseColor("#ffffff"));
                contextparams.setMargins(0,1,0,0);
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

                //set the order status
                TextView status = new TextView(TicketActivity.this);
                String statusS = "completed";
                LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                statusParams.setMargins(0,1,0,0);
                status.setBackgroundColor(Color.parseColor("#ffffff"));
                status.setGravity(Gravity.RIGHT);
                status.setTextColor(Color.parseColor("#000000"));
                if(!statusB){
                    statusS = "uncompleted";
                    status.setTextColor(Color.parseColor("#ff0000"));
                }

                status.setTextSize(18);
                status.setLayoutParams(statusParams);
                status.setText(statusS);


                //give the layout a function to jump to the code detail page when clicked
                layout.setClickable(true);
                Boolean finalStatusB = statusB;
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("recentOrderId", String.valueOf(id));
                        editor.commit();
                        Intent intent = new Intent(TicketActivity.this, CodeActivity.class);
                        if(!finalStatusB){
                            intent = new Intent(TicketActivity.this,PayActivity.class);
                            intent.putExtra("father","ticket");
                        }
                        startActivity(intent);
                    }
                });
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        movieText.addView(title);
                        movieText.addView(context);
                        movieText.addView(status);

                        //fill up the outer layout
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


    //rewrite onRefresh()
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

    //when no data available, give some information
    private void showNoData() {
        ticketlist.removeAllViews();
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

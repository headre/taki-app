package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.data.OkClient;
import com.example.myapplication.data.ResolveJson;
import com.example.myapplication.data.QRCodeUtil;
import com.example.myapplication.ui.login.LoginActivity;
import com.example.myapplication.FilmUserActivity;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**this is the activity to show the completed order
 * it will read the order id which is stored in local storage in advance
 * it will build a page showing all information of the order
 * it supports refund function
 * it supports send-email function which will create a pdf file first*/


public class CodeActivity extends AppCompatActivity {
    private Button refundButton, mailButton;
    private ArrayList<String> i = new ArrayList<>();
    private String cookie, orderString, orderId, date,time;
    private ImageView cover;
    private TextView orderInfo, title, Blurb;
    private LinearLayout qr_layout, emailTicket;
    private ArrayList<Integer> ticketsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);

        //initialize widgets
        orderInfo = findViewById(R.id.info);
        title = findViewById(R.id.title);
        cover = findViewById(R.id.cover);
        Blurb = findViewById(R.id.blurb);
        qr_layout = findViewById(R.id.qr_layout);
        mailButton = findViewById(R.id.email);
        emailTicket = findViewById(R.id.main);

        //read cookie and the latest order in local storage
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        cookie = sharedPreferences.getString("cookie", "");
        orderId = sharedPreferences.getString("recentOrderId", "");

        //add email method to button
        mailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tips = "Tickets are being generated, please wait";
                Toast toast = Toast.makeText(CodeActivity.this, tips, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Thread t = new Thread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
            }
        });

        //add refund function to button
        refundButton = findViewById(R.id.refund);
        refundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refundTickets();
                if(refundOk(date)) {
                    Intent intent = new Intent(CodeActivity.this, FilmActivity.class);
                    startActivity(intent);
                }
            }
        });
        init();
    }

    //init the page and set the data
    private void init() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    orderString = new OkClient(cookie).getOrder(orderId);
                    showOrder();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    //show order information
    private void showOrder() throws Exception {
        JSONObject order = new JSONObject(orderString);
        String finishTime;
        String ageType;
        String movieId;
        String totalCost;
        Integer roomId;
        totalCost = order.getString("totalCost");
        JSONArray tickets = new JSONArray(order.getString("tickets"));
        if (tickets != null) {
            JSONObject baseScreening = new JSONObject(tickets.getJSONObject(0).getString("screening"));
            JSONObject baseTicket = tickets.getJSONObject(0);

            ageType = baseTicket.getString("ageType");
            date = baseScreening.getString("date");
            time = baseScreening.getString("time");
            finishTime = baseScreening.getString("finishTime");
            roomId = baseScreening.getInt("auditoriumId");
            movieId = baseScreening.getString("movieId");
            String infoDetails = tickets.length() + " " + ageType + " tickets\nIn " + date + "\nfrom " + time + " to "
                    + finishTime + "\nRoom " + roomId.toString() + "\nTotalPrice: " + totalCost + "\n";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    orderInfo.setText(infoDetails);
                }
            });

            //get the tickets' detail information of the order
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String infoDetails = "";
                        Integer width = 450, height = 450;
                        if (tickets.length() > 2) {
                            width = 200;
                            height = 200;
                        }
                        Integer finalWidth = width;
                        Integer finalHeight = height;
                        for (int i = 0; i < tickets.length(); i++) {
                            JSONObject ticket = tickets.getJSONObject(i);
                            Integer seatId = ticket.getInt("seatId");
                            ticketsList.add(ticket.getInt("id"));
                            String seat = new OkClient(cookie).getSeatsPosition(seatId);
                            String validation = ticket.getString("validation");
                            JSONObject pos = new JSONObject(seat);
                            Character rol = 'A';
                            for (int m = 0; m < pos.getInt("col"); m++) {
                                rol++;
                            }
                            String seatPos = "row " + pos.getString("row") + " , col " + rol;
                            infoDetails += "seat: " + seatPos + "\n";


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Bitmap bitmap = QRCodeUtil.createQRCodeBitmap(validation, finalWidth, finalHeight);
                                    addQrCode(bitmap);
                                }
                            });
                        }
                        Log.e("info", infoDetails);

                        String movieData = new OkClient().getMovieInfo(movieId);
                        JSONObject movieJSON = new JSONObject(movieData);
                        String Title = movieJSON.getString("name");
                        String blurb = movieJSON.getString("blurb");
                        String coverName = movieJSON.getString("cover");


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
                        String finalInfoDetails = infoDetails;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                orderInfo.append(finalInfoDetails);
                                title.setText(Title);
                                Blurb.setText(blurb);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }


    }

    //add qr code imageview to the main layout
    private void addQrCode(Bitmap bitmap) {
        ImageView qr_code = new ImageView(CodeActivity.this);
        LinearLayout.LayoutParams qrCodeParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        qr_code.setLayoutParams(qrCodeParams);
        qr_code.setImageBitmap(bitmap);
        qr_layout.addView(qr_code);
    }

    //determine if refund is allowable according to the time( the rest of time must be more than 30 min)
    private Boolean refundOk(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Boolean avail = false;
        try {
            String dateS = sdf.format(sdf.parse(date));
            String dateAndTime = dateS+' '+time;
            Log.e("detailTime",dateAndTime);
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date targetDate = sdf1.parse(dateAndTime);
            Date today = sdf1.parse(sdf1.format(new Date(System.currentTimeMillis())));
            long minutes_30 = 30*60*1000;
            if (today.getTime()<targetDate.getTime()-minutes_30) {
                avail = true;
            }
            Log.e("refundOk", avail.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return avail;
    }

    //method to refund all the tickets of the order
    private void refundTickets() {
        if (!refundOk(date)) {
            String tips = "THe tickets you chose has over date ones!";
            Toast toast = Toast.makeText(CodeActivity.this, tips, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            for (int i = 0; i < ticketsList.size(); i++) {
                int finalI = i;
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new OkClient(cookie).refund(ticketsList.get(finalI));
                            if (finalI == ticketsList.size() - 1) {
                                String tips = "refund successfully, all fees return to your accounts, please check";
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(CodeActivity.this, tips, Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();
                                    }
                                });

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


    //method to print out the page in pdf
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void pdfModel(LinearLayout layout) throws Exception {
        String path = getApplicationContext().getFilesDir().getPath();
        Log.e("path", path);
        path = "/data/data/com.example.myapplication";
        PdfDocument document = new PdfDocument();
        Log.e("size, width, height", String.valueOf(layout.getWidth()) + " " + String.valueOf(layout.getHeight()));
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(layout.getWidth(), layout.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        layout.draw(page.getCanvas());
        document.finishPage(page);
        File file = new File(path + "/test.pdf");
        FileOutputStream outputStream = new FileOutputStream(file);
        try {
            document.writeTo(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        document.close();
        try {
            new OkClient(cookie).sendTicketToEmail(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String tips = "ticket has been sent to your email box, please check";
                Toast toast = Toast.makeText(CodeActivity.this, tips, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

}

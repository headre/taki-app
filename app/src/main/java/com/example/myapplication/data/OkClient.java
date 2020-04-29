package com.example.myapplication.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import androidx.annotation.Nullable;
import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class OkClient {
    private static OkHttpClient okHttpClient = new OkHttpClient();
    private String url, result = new String(), cookie = "";
    private Integer rowMax = 0, colMax = 0;
    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ssfFactory;
    }
    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.sslSocketFactory(createSSLSocketFactory());
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        okHttpClient = builder.build();
    }
    public OkClient() {
        this.url = "https://106.12.203.34:8443/";
    }

    public OkClient(String cookie) {
        this.url = "https://106.12.203.34:8443/";
        this.cookie = cookie;
    }

    private void ResetUrl() {
        this.url = "https://106.12.203.34:8443/";
    }

    public String getResult() {
        return result;
    }

    public String getCookie() {
        if (cookie == null || cookie == "") {
            return "cookie is empty";
        }
        return cookie;
    }

    //设置各种请求
    public void setMode(String mode) {
        switch (mode) {
            case "movies":
                url += "movies";
                getMovies();
                ResetUrl();
                break;
            case "screenings":
                url += "screenings";
                getScreenings();
                ResetUrl();
                break;
            default:
                getMovies();
                ResetUrl();
                break;
        }
    }

    public String getReleasedOrNotMovie(Integer flag) throws Exception{
        Request request = new Request.Builder()
                .url(url+"movies/findmovie?s=20&q="+flag)
                .build();
        result = okHttpClient.newCall(request).execute().body().string();
        result = new JSONObject(result).getString("content");
        return result;
    }

    public String getMoviesData() {
        try {
            setMode("movies");
            result = new JSONObject(result).getString("content");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getScreensData() {
        try {
            setMode("screenings");
            result = new JSONObject(result).getString("content");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getScreenInfo(String id) {
        url += "screenings/" + id;
        setMode("default");
        return getResult();
    }

    public String getMovieScreen(String id) {
        url += "movies/" + id;
        setMode("default");
        return getResult();
    }

    public String getMovieInfo(String id) {
        url += "movies/" + id + "/info";
        setMode("default");
        return getResult();
    }

    public void setMode(String mode, String id) {
        url += mode;
        url += "/" + id;
        if (mode == "movies") {
            url += "/info";
        }
        mode = "withid";
        setMode(mode);
    }

    public void setMode(String mode, String username, String password) {
        switch (mode) {
            case "movies":
                setMode(mode);
                break;
            case "login":
                url += "login";
                login(username, password);
                ResetUrl();
                break;
            case "screenings":
                setMode(mode);
                break;
        }
    }



    //登录模块
    public void login(String username, String password) {
        okHttpClient = new OkHttpClient.Builder().cookieJar(new CookieJar() {
            private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                cookieStore.put(url, cookies);
                cookie = cookies.get(0).value();
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookies = cookieStore.get(HttpUrl.parse("https://106.12.203.34:8443/"));
                return cookies != null ? cookies : new ArrayList<Cookie>();
            }
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .sslSocketFactory(createSSLSocketFactory())
        .hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        }).build();
        RequestBody body = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            result = response.body().string();
            Log.e("response", result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //获取电影列表模块
    private void getMovies() {
        final Request request = new Request.Builder()
                .url(url + "?s=20")
                .get()//默认就是GET请求
                .build();
        try {
            result = okHttpClient.newCall(request).execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //这玩意好像和上面那个是同一个东西
    public void getScreenings() {
        final Request request = new Request.Builder()
                .url(url + "?s=20")
                .get()//默认就是GET请求
                .build();
        try {
            result = okHttpClient.newCall(request).execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * public void getMovieIdFromScreen(String screenId){
     * okHttpClient = new OkHttpClient();
     * setMode("movies",screenId);
     * <p>
     * }
     */

    //上传选座信息
    public String sendTicket(ArrayList<String> ticketsList, String cookie, String screenId) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, ticketsList.toString());

        Request confirm = new Request.Builder()
                .url(url + "orders/screenings/" + screenId)
                .header("cookie", "JSESSIONID=" + cookie)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        String result = okHttpClient.newCall(confirm).execute().body().string();
        return result;
    }

    public void payOrder(String cookie) throws IOException {
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request confirm = new Request.Builder()
                .url(url + "orders/pay")
                .header("cookie", "JSESSIONID=" + cookie)
                .put(body)
                .build();
        result = okHttpClient.newCall(confirm).execute().body().string();
        Log.e("confirm response", result);
    }

    public void cancelOrder(String cookie) throws IOException {
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request confirm = new Request.Builder()
                .url(url + "orders/cancel")
                .header("cookie", "JSESSIONID=" + cookie)
                .put(body)
                .build();
        result = okHttpClient.newCall(confirm).execute().body().string();
        Log.e("cancel response", result);
    }

    public String getOrder(String id) throws Exception {
        Request request = new Request.Builder()
                .url(url + "orders/" + id)
                .header("cookie", "JSESSIONID=" + cookie)
                .build();
        result = okHttpClient.newCall(request).execute().body().string();
        Log.e("get order response", result);
        return result;

    }


    public String getOrders(String cookie) throws Exception {
        Request request = new Request.Builder()
                .url(url + "users/orders")
                .header("cookie", "JSESSIONID=" + cookie)
                .build();
        result = okHttpClient.newCall(request).execute().body().string();
        return result;
    }

    public Bitmap getImg(String name) throws Exception {
        Request request = new Request.Builder()
                .url(url + "file/" + name)
                .addHeader("Cookie", "JSESSIONID=" + cookie)
                .build();
        ResponseBody body = okHttpClient.newCall(request).execute().body();
        InputStream in = body.byteStream();
        Bitmap bitmap = BitmapFactory.decodeStream(in);
        return bitmap;
    }

    public String getSearch(String keyword) throws Exception {
        Request request = new Request.Builder()
                .url(url + "movies/search?q=" + keyword)
                .build();
        result = okHttpClient.newCall(request).execute().body().string();
        return result;
    }

    public String screenSearch(String date, @Nullable String movie, @Nullable Integer page, @Nullable Integer contentCount) {
        if (movie == null) {
            movie = "";
        }
        if (page == null) {
            page = 1;
        }
        if (contentCount == null) {
            contentCount = 10;
        }
        String params = "screenings/search?q=" + date + "&m=" + movie + "&p=" + page + "&s=" + contentCount;
        Request request = new Request.Builder()
                .url(url + params)
                .build();
        try {
            result = okHttpClient.newCall(request).execute().body().string();
            JSONObject object = new JSONObject(result);
            if (movie != "") {
                result = object.getString("content");
            } else {
                result = new JSONObject(object.getString("screenings")).getString("content");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //获取影厅可用的所有座位
    public ArrayList<HashMap> GetAllSeat(String auditoriumId) {
        ArrayList<HashMap> seats = new ArrayList<>();
        //OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url(url+"seats/auditoriums/" + auditoriumId)
                .method("GET", null)
                .addHeader("Cookie", "JSESSIONID=" + cookie)
                .build();
        try {
            result = okHttpClient.newCall(request).execute().body().string();
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject seat = jsonArray.getJSONObject(i);
                int row = seat.getInt("row");
                int col = seat.getInt("col");
                if (rowMax < row)
                    rowMax = row;
                if (colMax < col)
                    colMax = col;
                HashMap<Integer, Integer> position = new HashMap<>();
                position.put(row, col);
                HashMap<HashMap, JSONObject> seatData = new HashMap<>();
                seatData.put(position, seat);
                seats.add(seatData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return seats;
    }

    //获取需要建立的影厅尺寸
    public JSONObject getSize() throws Exception {
        JSONObject size = new JSONObject();
        size.put("row", this.rowMax);
        size.put("col", this.colMax);
        return size;
    }

    //获取当前排片被占用的座位
    public ArrayList<HashMap> seatTaken(String screenId) throws Exception {
        Request request = new Request.Builder()
                .url(url + "tickets/screenings/" + screenId)
                .header("Cookie", "JSESSIONID=" + cookie)
                .build();
        String result = okHttpClient.newCall(request).execute().body().string();
        Log.e("seatsTaken", result);
        JSONArray jsonArray = new JSONArray(result);
        ArrayList<HashMap> seats = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject seat = jsonArray.getJSONObject(i);
            int row = seat.getInt("row");
            int col = seat.getInt("col");
            if (row > rowMax)
                rowMax = row;
            if (col > colMax)
                colMax = col;
            HashMap<Integer, Integer> position = new HashMap<>();
            position.put(row, col);
            HashMap<HashMap, JSONObject> seatData = new HashMap<>();
            seatData.put(position, seat);
            seats.add(seatData);
        }
        return seats;
    }

    //通过screenId获取博放映厅的编号id
    public String getAuditoruimId(String screenId) throws Exception {
        Request request = new Request.Builder()
                .get()
                .url(url + "screenings/" + screenId)
                .header("Cookie", "JSESSIONID=" + cookie)
                .build();
        String result = okHttpClient.newCall(request).execute().body().string();
        String id = new JSONObject(result).getString("auditoriumId");
        return id;
    }

    public void logout() throws IOException {
        Request request = new Request.Builder()
                .get()
                .url(url + "logout")
                .addHeader("Cookie", "JSESSIONID=" + cookie)
                .build();
        String result = okHttpClient.newCall(request).execute().body().string();
        Log.e("response", result);
    }

    public String getSeatsPosition(Integer id) throws Exception {
        Request request = new Request.Builder()
                .url(url + "seats/" + id)
                .addHeader("Cookie", "JSESSIONID=" + cookie)
                .build();
        String result = okHttpClient.newCall(request).execute().body().string();
        return result;
    }

    public String register(String username, String email, String password, String id_code) throws Exception {
        MediaType mediaType = MediaType.parse("application/json");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        jsonObject.put("email", email);
        jsonObject.put("password", password);
        jsonObject.put("idcode", id_code);
        Log.e("body", jsonObject.toString());
        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());
        Request request = new Request.Builder()
                .url(url + "register")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        result = okHttpClient.newCall(request).execute().body().string();
        Log.e("response", result);
        return result;
    }

    public String sendIdCode(String email) throws Exception {
        MediaType mediaType = MediaType.parse("application/json");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("address", email);
        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());
        Request request = new Request.Builder()
                .url(url + "register/send")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        result = okHttpClient.newCall(request).execute().body().string();
        Log.e("response", result);
        return result;
    }

    public String refund(Integer id) throws Exception {
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url(url + "refund/" + id)
                .addHeader("Cookie", "JSESSIONID=" + cookie)
                .addHeader("Content-Type", "application/json")
                .put(body)
                .build();
        result = okHttpClient.newCall(request).execute().body().string();
        Log.e("response", result);
        return result;
    }

    public String sendTicketToEmail(File file) throws Exception {
        //OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("title", "Your ticket")
                .addFormDataPart("body", "this is the movie ticket you want")
                .addFormDataPart("file", "test.pdf",
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                file))
                .build();
        Request request = new Request.Builder()
                .url(url + "user/send")
                .method("POST", body)
                .addHeader("Cookie", "JSESSIONID=" + cookie)
                .build();
        result = okHttpClient.newCall(request).execute().body().string();
        Log.e("response", result);
        return result;
    }

    public String getAuditoriumInfo(String id) throws Exception {
        Request request = new Request.Builder()
                .url(url + "auditoriums/" + id)
                .addHeader("Cookie", "JSESSIONID=" + cookie)
                .build();
        result = okHttpClient.newCall(request).execute().body().string();
        return result;
    }

    public void sendMessage(String tel_number) throws Exception{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("number",tel_number);
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());
        Request request = new Request.Builder()
                .url(url+"register/send")
                .post(body)
                .build();
        result = okHttpClient.newCall(request).execute().body().string();
        Log.e("response",result);
    }
}

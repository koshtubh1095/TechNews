package news.com.sample.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import news.com.sample.Callback;
import news.com.sample.model.NewsEntity;
import news.com.sample.service.NewsFetchService;
import news.com.sample.storage.DataQuery;

public class AutoStartReceiver extends BroadcastReceiver implements Callback {
    private Context mContext;

    @Override
    public void onReceive(final Context context, Intent arg) {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        if (arg.getAction().equalsIgnoreCase("ACTION_CALL_API")) {
            Log.d("app", "Intent Received ::" + arg.getAction());
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    URL url = null;
                    try {
                        url = new URL("https://candidate-test-data-moengage.s3.amazonaws.com/Android/news-api-feed/staticRespon\n" +
                                "se.json");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        String readStream = readStream(con.getInputStream());
                        mContext = context;
                        onResult(readStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 0);
        } else {
            Log.d("app", "Intent Received ::" + arg.getAction());
            Intent intent = new Intent(context, NewsFetchService.class);
            context.startService(intent);
        }
    }

    private static String readStream(InputStream in) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in));) {

            String nextLine = "";
            while ((nextLine = reader.readLine()) != null) {
                sb.append(nextLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    @Override
    public void onResult(final String data) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject;

                try {
                    jsonObject = new JSONObject(data);
                    JSONArray resultArray = jsonObject.getJSONArray("articles");
                    for (int i = 0; i < resultArray.length(); i++) {
                        JSONObject newsObject = resultArray.getJSONObject(i);
                        newsObject.put("timeStamp", String.valueOf(System.currentTimeMillis()));
                        NewsEntity newsEntity = new NewsEntity(newsObject);
                        DataQuery.insertNews(mContext, newsEntity);
                    }
                } catch (JSONException e) {
                    Log.e("app", "fail to parse json string");
                }
            }
        }, 0);
    }
}

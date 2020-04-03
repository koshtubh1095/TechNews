package news.com.sample.ui;

import com.facebook.drawee.backends.pipeline.Fresco;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import news.com.sample.Callback;
import news.com.sample.storage.DataQuery;
import news.com.sample.model.NewsEntity;
import news.com.sample.storage.NewsProvider;
import news.com.sample.R;

public class MainActivity
        extends ListActivity
        implements Callback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private List<NewsEntity> newsItemList;
    private Handler handler = new Handler(Looper.getMainLooper());
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fresco.initialize(this);
        searchView = (SearchView) findViewById(R.id.searchView);

        newsItemList = new ArrayList<>();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.saved_articles) {
            ArrayList<NewsEntity> newsEntities = DataQuery.getUserSavedNews();
            if (newsEntities.size() > 0) {
                Intent intent = new Intent(MainActivity.this, UserSavedActivity.class);
                Bundle data = new Bundle();
                data.putParcelableArrayList("newsEntities", newsEntities);
                intent.putExtra("data", data);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "No saved articles found", Toast.LENGTH_SHORT).show();
            }
            Log.d("app", "No of records Saved : " + newsEntities.size());
        } else if (id == R.id.action_sort_by_desc) {
            ArrayList<NewsEntity> newsEntities = DataQuery.getNewsSortByDesc();
            if (!newsEntities.isEmpty()) {
                attachView(newsEntities);
            } else {
                Toast.makeText(getApplicationContext(), "Cannot sort as data is empty", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.action_sort_by_asc) {
            ArrayList<NewsEntity> newsEntities = DataQuery.getNewsSortByAsc();
            if (!newsEntities.isEmpty()) {
                attachView(newsEntities);
            } else {
                Toast.makeText(getApplicationContext(), "Cannot sort as data is empty", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadResource(final Callback callback) {

        try {
            NewsProvider.startMediaProvider(getApplicationContext());
            List<NewsEntity> news = DataQuery.getNews(getApplicationContext());
            if (!news.isEmpty()) {
                attachView(news);
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        URL url = null;
                        try {
                            url = new URL("https://candidate-test-data-moengage.s3.amazonaws.com/Android/news-api-feed/staticRespon\n" +
                                    "se.json");
                            HttpURLConnection con = (HttpURLConnection) url.openConnection();
                            String readStream = readStream(con.getInputStream());
                            callback.onResult(readStream);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, 0);

            }
        } catch (Exception e) {
            Log.e("app", "Exception::" + e.toString());
        }
//            }
//        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
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
        handler.postDelayed(new Runnable() {
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
                        DataQuery.insertNews(getApplicationContext(), newsEntity);
                        newsItemList.add(newsEntity);

                    }
                } catch (JSONException e) {
                    Log.e(TAG, "fail to parse json string");
                }
                attachView(newsItemList);

            }
        }, 0);

    }

    private void attachView(final List<NewsEntity> newsEntities) {
        final NewsListAdapter adapter = new NewsListAdapter(MainActivity.this, R.layout.list_item_news, newsEntities);
        setListAdapter(adapter);

        ListView listView = getListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewsEntity newsEntity = newsEntities.get(position);
                String title = newsEntity.getTitle();
                Intent intent = new Intent(MainActivity.this, DetailViewActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("summary", newsEntity.getDescription());
                intent.putExtra("imageURL", newsEntity.getUrlToImage());
                intent.putExtra("newsId", newsEntity.getId());
                intent.putExtra("url", newsEntity.getUrl());
                intent.putExtra("isSaved", newsEntity.getIsOffline());
                intent.putExtra("fromActivity", MainActivity.class.getSimpleName());
                startActivity(intent);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                List<NewsEntity> resultSet = new ArrayList<>();
                for (NewsEntity d : newsEntities) {
                    try {
                        if (d.getAuthor() != null && d.getSource().getString("id").toLowerCase().contains(query.toLowerCase())) {
                            resultSet.add(d);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (!resultSet.isEmpty()) {
                    attachView(resultSet);
                } else {
                    Toast.makeText(MainActivity.this, "No Match found", Toast.LENGTH_LONG).show();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() == 0) {
                    attachView(DataQuery.getNews(getApplicationContext()));
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        loadResource(this);
    }
}

package news.com.sample.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.List;

import news.com.sample.model.NewsEntity;
import news.com.sample.R;

public class UserSavedActivity extends ListActivity {

    private List<NewsEntity> newsItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_saved);
        Fresco.initialize(this);

        newsItemList = getIntent().getBundleExtra("data").getParcelableArrayList("newsEntities");
        attachView(newsItemList);
    }

    private void attachView(final List<NewsEntity> newsEntities) {
        NewsListAdapter adapter = new NewsListAdapter(UserSavedActivity.this, R.layout.list_item_news, newsItemList);
        setListAdapter(adapter);

        ListView listView = getListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewsEntity newsEntity = newsEntities.get(position);
                String title = newsEntity.getTitle();
                Intent intent = new Intent(UserSavedActivity.this, DetailViewActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("summary", newsEntity.getDescription());
                intent.putExtra("imageURL", newsEntity.getUrlToImage());
                intent.putExtra("newsId", newsEntity.getId());
                intent.putExtra("url", newsEntity.getUrl());
                intent.putExtra("fromActivity", UserSavedActivity.class.getSimpleName());
                startActivity(intent);
            }
        });
    }
}

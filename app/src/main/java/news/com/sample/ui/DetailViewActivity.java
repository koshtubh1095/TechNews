package news.com.sample.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.imagepipeline.request.ImageRequest;

import news.com.sample.storage.DataQuery;
import news.com.sample.R;

/**
 * News detail view
 */
public class DetailViewActivity extends Activity {
    private int newsId;
    private String storyURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle extras = getIntent().getExtras();
        newsId = extras.getInt("newsId");
        String title = extras.getString("title");
        String summary = extras.getString("summary");
        String imageURL = extras.getString("imageURL");
        storyURL = extras.getString("url");
        String activityFrom = extras.getString("fromActivity");
        String isSaved = extras.getString("isSaved");

        TextView titleView = (TextView) findViewById(R.id.title);
        DraweeView imageView = (DraweeView) findViewById(R.id.news_image);
        TextView summaryView = (TextView) findViewById(R.id.summary_content);
        Button saveOfflineLink = (Button) findViewById(R.id.save_offline_link);

        titleView.setText(title);
        summaryView.setText(summary);

        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(ImageRequest.fromUri(Uri.parse(imageURL)))
                .setOldController(imageView.getController()).build();
        imageView.setController(draweeController);
        if (activityFrom.equalsIgnoreCase("MainActivity")) {
            if (isSaved.equalsIgnoreCase("true")) {
                saveOfflineLink.setVisibility(View.VISIBLE);
                saveOfflineLink.setEnabled(false);
            } else {
                saveOfflineLink.setVisibility(View.VISIBLE);
                saveOfflineLink.setEnabled(true);
            }
        } else {
            saveOfflineLink.setVisibility(View.GONE);
        }
    }

    public void onSaveOfflineClicked(View view) {
        if (DataQuery.updateNewsIdtoOffline(getApplicationContext(), newsId)) {
            Toast.makeText(getApplicationContext(), "Offline save successful", Toast.LENGTH_SHORT).show();
            DetailViewActivity.this.finish();
        } else {
            Toast.makeText(getApplicationContext(), "Offline save not successful", Toast.LENGTH_SHORT).show();
        }
    }

    public void onViewFullArticleClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(storyURL));
        startActivity(intent);
    }
}

package news.com.sample.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This represents a news item
 */
public class NewsEntity implements Parcelable {
    private static final String TAG = NewsEntity.class.getSimpleName();

    public NewsEntity() {

    }

    private int id;
    private JSONObject source;
    private String author;
    private String title;
    private String description;
    private String url;
    private String urlToImage;
    private String publishedAt;
    private String content;
    private String isOffline;
    private String timeStamp;

    public JSONObject getSource() {
        return source;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public String getContent() {
        return content;
    }


    public String getIsOffline() {
        return isOffline;
    }

    public void setIsOffline(String isOffline) {
        this.isOffline = isOffline;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public NewsEntity(JSONObject jsonObject) {
        try {
            if (jsonObject != null) {
                source = new JSONObject();
                if (jsonObject.has("source")) {
                    source = jsonObject.getJSONObject("source");
                }
                author = jsonObject.getString("author");
                title = jsonObject.getString("title");
                description = jsonObject.getString("description");
                url = jsonObject.getString("url");
                urlToImage = jsonObject.getString("urlToImage");
                publishedAt = jsonObject.getString("publishedAt");
                content = jsonObject.getString("content");
                timeStamp = jsonObject.getString("timeStamp");
                isOffline = jsonObject.getString("isOffline");
            }
        } catch (JSONException exception) {
            Log.e(TAG, exception.getMessage());
        }
    }

    public void setSource(JSONObject source) {
        this.source = source;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUrlToImage(String urlToImage) {
        this.urlToImage = urlToImage;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    protected NewsEntity(Parcel in) {
        id = in.readInt();
        author = in.readString();
        title = in.readString();
        description = in.readString();
        url = in.readString();
        urlToImage = in.readString();
        publishedAt = in.readString();
        content = in.readString();
        timeStamp = in.readString();
        isOffline = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(author);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(url);
        dest.writeString(urlToImage);
        dest.writeString(publishedAt);
        dest.writeString(content);
        dest.writeString(timeStamp);
        dest.writeString(isOffline);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NewsEntity> CREATOR = new Creator<NewsEntity>() {
        @Override
        public NewsEntity createFromParcel(Parcel in) {
            return new NewsEntity(in);
        }

        @Override
        public NewsEntity[] newArray(int size) {
            return new NewsEntity[size];
        }
    };
}

package news.com.sample.storage;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import news.com.sample.model.NewsEntity;
import news.com.sample.storage.NewsProvider;

public class DataQuery {

    private static final int TRANSACTION_LIMIT = 300;

    public static void prepareStatement(Context context, Uri uri, ContentValues values, String selection, String[] projection, ArrayList<ContentProviderOperation> operations, int queryType) {
        try {
            Log.d("app", "prepareStatement - uri: " + uri + " | values: " + values + " | selection: " + selection
                    + " | projection: " + projection + " | operations: " + operations + " queryType: " + queryType);

            ContentProviderOperation.Builder opBuild = null;
            switch (queryType) {

                case NewsProvider.TYPE_INSERT:
                    opBuild = ContentProviderOperation.newInsert(uri).withValues(values).withYieldAllowed(true);
                    break;

                case NewsProvider.TYPE_UPDATE:
                    opBuild = ContentProviderOperation.newUpdate(uri).withValues(values).withSelection(selection, projection)
                            .withYieldAllowed(true);
                    break;

                case NewsProvider.TYPE_DELETE:
                    opBuild = ContentProviderOperation.newDelete(uri).withSelection(selection, projection)
                            .withYieldAllowed(true);
                    break;

                default:
                    break;
            }

            if (opBuild != null) {
                operations.add(opBuild.build());

                if (operations.size() <= TRANSACTION_LIMIT) {
                    try {
                        context.getContentResolver().applyBatch(NewsProvider.AUTHORITY, operations);
                    } catch (Exception e) {
                        Log.e("app", Log.getStackTraceString(e));
                    } finally {
                        operations.clear();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("app", "Exception while preparing " + uri.getPath() + " " + Log.getStackTraceString(e));
        }
    }

    public static synchronized void insertNews(Context context, NewsEntity newsEntity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NewsProvider.SOURCE, newsEntity.getSource().toString());
        contentValues.put(NewsProvider.AUTHOR, newsEntity.getAuthor());
        contentValues.put(NewsProvider.TITLE, newsEntity.getTitle());
        contentValues.put(NewsProvider.DESCRIPTION, newsEntity.getDescription());
        contentValues.put(NewsProvider.URL, newsEntity.getUrl());
        contentValues.put(NewsProvider.URL_TO_IMAGE, newsEntity.getUrlToImage());
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat output = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS");

        Date d = null;
        try {
            d = input.parse(newsEntity.getPublishedAt());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formatted = output.format(d);
        contentValues.put(NewsProvider.PUBLISHED_AT, formatted);
        contentValues.put(NewsProvider.CONTENT, newsEntity.getContent());
        contentValues.put(NewsProvider.CREATED_AT, newsEntity.getTimeStamp());
        ArrayList<ContentProviderOperation> insertNewsTable = new ArrayList<>();
        prepareStatement(context, NewsProvider.NEWS_URI,
                contentValues, null, null, insertNewsTable, NewsProvider.TYPE_INSERT);
    }

    public static synchronized List<NewsEntity> getNews(Context context) {
        final List<NewsEntity> list = new ArrayList<>();


        String selectQuery;
        SQLiteDatabase db;
        Cursor c = null;
        String myPath = "/data/user/0/news.com.technewssample/databases/partners.db";

        db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        selectQuery = "SELECT * FROM news";

        try {
            c = db.rawQuery(selectQuery, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        NewsEntity newsEntity = new NewsEntity();
                        newsEntity.setId(c.getInt(c.getColumnIndex(NewsProvider._ID)));
                        newsEntity.setSource(new JSONObject(c.getString(c.getColumnIndex(NewsProvider.SOURCE))));
                        newsEntity.setAuthor(c.getString(c.getColumnIndex(NewsProvider.AUTHOR)));
                        newsEntity.setTitle(c.getString(c.getColumnIndex(NewsProvider.TITLE)));
                        newsEntity.setDescription(c.getString(c.getColumnIndex(NewsProvider.DESCRIPTION)));
                        newsEntity.setUrl(c.getString(c.getColumnIndex(NewsProvider.URL)));
                        newsEntity.setUrlToImage(c.getString(c.getColumnIndex(NewsProvider.URL_TO_IMAGE)));
                        newsEntity.setPublishedAt(c.getString(c.getColumnIndex(NewsProvider.PUBLISHED_AT)));
                        newsEntity.setContent(c.getString(c.getColumnIndex(NewsProvider.CONTENT)));
                        newsEntity.setTimeStamp(c.getString(c.getColumnIndex(NewsProvider.CREATED_AT)));
                        newsEntity.setIsOffline(c.getString(c.getColumnIndex(NewsProvider.IS_OFFLINE)));
                        list.add(newsEntity);
                    } while (c.moveToNext());
                }
            }
        } catch (Exception e) {
            Log.e("app", "getNews ex: " + e.toString());
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }

        Log.d("app", "got all news from table" + list.size());


        return list;

    }

    public static boolean updateNewsIdtoOffline(Context context, int id) {
        ArrayList<ContentProviderOperation> updateNewsTable = new ArrayList<>();
        try {
            ContentValues values = new ContentValues();
            values.put(NewsProvider.IS_OFFLINE, "true");

            prepareStatement(context, NewsProvider.NEWS_URI,
                    values, NewsProvider._ID + "=?", new String[]{String.valueOf(id)},
                    updateNewsTable, NewsProvider.TYPE_UPDATE);

            Log.d("app", "updateNewsToOffline " + id);
        } catch (Exception e) {
            Log.e("app", Log.getStackTraceString(e));
            return false;
        }
        return true;
    }

    public static synchronized ArrayList<NewsEntity> getUserSavedNews() {

        ArrayList<NewsEntity> list = new ArrayList<>();
        String selectQuery;
        SQLiteDatabase db;
        Cursor c = null;
        String myPath = "/data/user/0/news.com.technewssample/databases/partners.db";

        db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        selectQuery = "SELECT * FROM news WHERE isOffline='true'";

        try {
            c = db.rawQuery(selectQuery, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        NewsEntity newsEntity = new NewsEntity();
                        newsEntity.setId(c.getInt(c.getColumnIndex(NewsProvider._ID)));
                        newsEntity.setSource(new JSONObject(c.getString(c.getColumnIndex(NewsProvider.SOURCE))));
                        newsEntity.setAuthor(c.getString(c.getColumnIndex(NewsProvider.AUTHOR)));
                        newsEntity.setTitle(c.getString(c.getColumnIndex(NewsProvider.TITLE)));
                        newsEntity.setDescription(c.getString(c.getColumnIndex(NewsProvider.DESCRIPTION)));
                        newsEntity.setUrl(c.getString(c.getColumnIndex(NewsProvider.URL)));
                        newsEntity.setUrlToImage(c.getString(c.getColumnIndex(NewsProvider.URL_TO_IMAGE)));
                        newsEntity.setPublishedAt(c.getString(c.getColumnIndex(NewsProvider.PUBLISHED_AT)));
                        newsEntity.setContent(c.getString(c.getColumnIndex(NewsProvider.CONTENT)));
                        newsEntity.setTimeStamp(c.getString(c.getColumnIndex(NewsProvider.CREATED_AT)));
                        newsEntity.setIsOffline(c.getString(c.getColumnIndex(NewsProvider.IS_OFFLINE)));
                        list.add(newsEntity);
                    } while (c.moveToNext());
                }
            }
        } catch (Exception e) {
            Log.e("app", "getNews ex: " + e.toString());
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }

        Log.d("app", "got all news from table" + list.size());

        return list;

    }

    public static synchronized ArrayList<NewsEntity> getNewsSortByDesc() {

        ArrayList<NewsEntity> list = new ArrayList<>();
        String selectQuery;
        SQLiteDatabase db;
        Cursor c = null;
        String myPath = "/data/user/0/news.com.technewssample/databases/partners.db";

        db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        selectQuery = "select *  from news order by news.publishedAt desc";

        try {
            c = db.rawQuery(selectQuery, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        NewsEntity newsEntity = new NewsEntity();
                        newsEntity.setId(c.getInt(c.getColumnIndex(NewsProvider._ID)));
                        newsEntity.setSource(new JSONObject(c.getString(c.getColumnIndex(NewsProvider.SOURCE))));
                        newsEntity.setAuthor(c.getString(c.getColumnIndex(NewsProvider.AUTHOR)));
                        newsEntity.setTitle(c.getString(c.getColumnIndex(NewsProvider.TITLE)));
                        newsEntity.setDescription(c.getString(c.getColumnIndex(NewsProvider.DESCRIPTION)));
                        newsEntity.setUrl(c.getString(c.getColumnIndex(NewsProvider.URL)));
                        newsEntity.setUrlToImage(c.getString(c.getColumnIndex(NewsProvider.URL_TO_IMAGE)));
                        newsEntity.setPublishedAt(c.getString(c.getColumnIndex(NewsProvider.PUBLISHED_AT)));
                        newsEntity.setContent(c.getString(c.getColumnIndex(NewsProvider.CONTENT)));
                        newsEntity.setTimeStamp(c.getString(c.getColumnIndex(NewsProvider.CREATED_AT)));
                        newsEntity.setIsOffline(c.getString(c.getColumnIndex(NewsProvider.IS_OFFLINE)));
                        list.add(newsEntity);
                    } while (c.moveToNext());
                }
            }
        } catch (Exception e) {
            Log.e("app", "getNews ex: " + e.toString());
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }

        Log.d("app", "got all news from table" + list.size());

        return list;

    }

    public static synchronized ArrayList<NewsEntity> getNewsSortByAsc() {

        ArrayList<NewsEntity> list = new ArrayList<>();
        String selectQuery;
        SQLiteDatabase db;
        Cursor c = null;
        String myPath = "/data/user/0/news.com.technewssample/databases/partners.db";

        db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        selectQuery = "select *  from news order by news.publishedAt asc";

        try {
            c = db.rawQuery(selectQuery, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        NewsEntity newsEntity = new NewsEntity();
                        newsEntity.setId(c.getInt(c.getColumnIndex(NewsProvider._ID)));
                        newsEntity.setSource(new JSONObject(c.getString(c.getColumnIndex(NewsProvider.SOURCE))));
                        newsEntity.setAuthor(c.getString(c.getColumnIndex(NewsProvider.AUTHOR)));
                        newsEntity.setTitle(c.getString(c.getColumnIndex(NewsProvider.TITLE)));
                        newsEntity.setDescription(c.getString(c.getColumnIndex(NewsProvider.DESCRIPTION)));
                        newsEntity.setUrl(c.getString(c.getColumnIndex(NewsProvider.URL)));
                        newsEntity.setUrlToImage(c.getString(c.getColumnIndex(NewsProvider.URL_TO_IMAGE)));
                        newsEntity.setPublishedAt(c.getString(c.getColumnIndex(NewsProvider.PUBLISHED_AT)));
                        newsEntity.setContent(c.getString(c.getColumnIndex(NewsProvider.CONTENT)));
                        newsEntity.setTimeStamp(c.getString(c.getColumnIndex(NewsProvider.CREATED_AT)));
                        newsEntity.setIsOffline(c.getString(c.getColumnIndex(NewsProvider.IS_OFFLINE)));
                        list.add(newsEntity);
                    } while (c.moveToNext());
                }
            }
        } catch (Exception e) {
            Log.e("app", "getNews ex: " + e.toString());
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }

        Log.d("app", "got all news from table" + list.size());

        return list;

    }
}

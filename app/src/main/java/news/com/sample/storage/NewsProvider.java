package news.com.sample.storage;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class NewsProvider extends ContentProvider {

    public static final String AUTHORITY = "news.com.technewssample.provider";


    private static SQLiteDatabase mDatabase;
    public static final String NEWS_TABLE_NAME = "news";


    public static final Uri NEWS_URI = Uri.parse("news://" + AUTHORITY + "/" + NEWS_TABLE_NAME);
    public static final String _ID = "news_id";
    public static final String SOURCE = "source";
    public static final String AUTHOR = "author";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String URL = "url";
    public static final String URL_TO_IMAGE = "urlToImage";
    public static final String PUBLISHED_AT = "publishedAt";
    public static final String CONTENT = "content";
    public static final String CREATED_AT = "createdAt";
    public static final String IS_OFFLINE = "isOffline";

    private static HashMap<String, String> CONTENT_PROJECTION_MAP;
    public static DatabaseHelper mDbHelper;

    public static final int TYPE_INSERT = 1;
    public static final int TYPE_UPDATE = 2;
    public static final int TYPE_DELETE = 3;


    public static final int NEWS_ = 1;
    public static final int NEWS_ID_ = 2;
    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "news", NEWS_);
        uriMatcher.addURI(AUTHORITY, "news/#", NEWS_ID_);
    }

    public static final int DATABASE_VERSION = 1;

    static final String CREATE_NEWS_DB_TABLE =
            " CREATE TABLE IF NOT EXISTS " + NEWS_TABLE_NAME +
                    " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + SOURCE + " TEXT, "
                    + AUTHOR + " TEXT, "
                    + TITLE + " TEXT, "
                    + DESCRIPTION + " TEXT, "
                    + URL + " TEXT, "
                    + URL_TO_IMAGE + " TEXT, "
                    + PUBLISHED_AT + " DATE, "
                    + CONTENT + " TEXT, "
                    + CREATED_AT + " DATE, "
                    + IS_OFFLINE + " TEXT DEFAULT 'false')";

    static final String SET_ENCODING =
            " PRAGMA encoding = 'UTF-8'";

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private final Context mContext;

        DatabaseHelper(Context context) {
            super(context, "/data/user/0/news.com.technewssample/databases/partners.db", null, DATABASE_VERSION, null);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTables(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + NEWS_TABLE_NAME);
                db.execSQL(CREATE_NEWS_DB_TABLE);
            } catch (Exception e) {
//            Logger.e(TAG,"Creating Tables " + Log.getStackTraceString(e));
            }
        }

        private void createTables(SQLiteDatabase db) {
            try {
                db.execSQL(SET_ENCODING);
                db.execSQL(CREATE_NEWS_DB_TABLE);

            } catch (Exception e) {
            }
        }

        private void dropTables(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + NEWS_TABLE_NAME);
        }
    }

    @Override
    public boolean onCreate() {
        startMediaProvider(getContext());
        return true;
    }


    public static void startMediaProvider(Context context) {
        //DeviceDetails details = TransportSdk.Companion.getInstance(context).getDeviceType();
//        mDbLocation = Environment.getExternalStorageDirectory() + File.separator+"koshtubh"+ File.separator + MEDIA_DB_NAME;
        /*Recreate mDbHelper*/
        mDbHelper = new DatabaseHelper(context);
        openDatabase(context);
    }

    public static synchronized SQLiteDatabase openDatabase(Context context) {
        try {
            if (mDbHelper == null) {
                mDbHelper = new DatabaseHelper(context);
            }

            if (mDatabase == null || !mDatabase.isOpen()) {
                //opening new database
                mDatabase = mDbHelper.getWritableDatabase();
            }

        } catch (Exception e) {
            Log.e("app", e.toString());
        }
        return mDatabase;
    }


    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        SQLiteDatabase db = null;
        ContentProviderResult[] results = null;
        try {
            db = openDatabase(getContext());
            db.beginTransaction();
            final int numOperations = operations.size();
            results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                try {
                    results[i] = operations.get(i).apply(this, results, i);
                } catch (Exception e) {
                    Log.d("app", "Exception in applyBatch :: " + e.toString());
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("app", "Exception in applyBatch :: " + e.toString());
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
        return results;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        try {
            openDatabase(getContext());
            switch (uriMatcher.match(uri)) {

                case NEWS_:
                    long rowIDContent = mDatabase.insert(NEWS_TABLE_NAME, "", values);
                    if (rowIDContent > 0) {
                        Uri _uri = ContentUris.withAppendedId(NEWS_URI, rowIDContent);
                        getContext().getContentResolver().notifyChange(_uri, null);
                        return _uri;
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } catch (Exception e) {
            Log.d("app", "Exception in inserting :: " + e.toString());
        } finally {

        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String groupBy = null;

        try {
            switch (uriMatcher.match(uri)) {
                case NEWS_:
                    qb.setTables(NEWS_TABLE_NAME);
                    qb.setProjectionMap(CONTENT_PROJECTION_MAP);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } catch (Exception e) {
            Log.d("app", "Exception in querying :: " + e.toString());
        } finally {

        }

        Cursor cursor = null;
        try {
            openDatabase(getContext());
            cursor = qb.query(mDatabase, projection, selection, selectionArgs, groupBy, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (Exception e) {
            Log.d("app", "Exception in openingDb while querying :: " + e.toString());
        }
        return cursor;
    }

    public void getItem() {

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        try {
            openDatabase(getContext());
            switch (uriMatcher.match(uri)) {
                case NEWS_:
                    count = mDatabase.delete(NEWS_TABLE_NAME, selection, selectionArgs);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } catch (Exception e) {
            Log.d("app", "Exception in deleting :: " + e.toString());
        } finally {

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        try {
            openDatabase(getContext());
            switch (uriMatcher.match(uri)) {
                case NEWS_:
                    count = mDatabase.update(NEWS_TABLE_NAME, values, selection, selectionArgs);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } catch (Exception e) {
            Log.d("app", "Exception in updating :: " + e.toString());
            count = -1;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {

            case NEWS_:
                return "vnd.android.cursor.dir/vnd.news";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}

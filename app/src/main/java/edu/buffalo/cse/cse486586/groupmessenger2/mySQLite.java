package edu.buffalo.cse.cse486586.groupmessenger2;

/**
 * Created by seongsu on 2015-02-14.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;



public class mySQLite extends SQLiteOpenHelper {
    public static final Uri CONTENT_URI = Uri.parse("content://edu.buffalo.cse.cse486586.groupmessenger.provider2");
    private static final String TAG = "DBAdapter";

    // DB Fields
    public static final String KEY_ROWID = "_id";
    public static final int COL_ROWID = 0;

    // Setup your fields here:
    public static final String KEY_KEYCOLUMN = "key";
    public static final String KEY_VALUECOLUMN = "value";


    public static final int COL_KEY = 1;
    public static final int COL_VALUE = 2;


    public static final String[] ALL_KEYS = new String[] {KEY_ROWID, KEY_KEYCOLUMN, KEY_VALUECOLUMN};


    public static final String DATABASE_NAME = "MyDb";
    public static final String DATABASE_TABLE = "mainTable";
    public static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE_SQL =
            "create table " + DATABASE_TABLE
                    + " ("
                    + KEY_KEYCOLUMN + " TEXT PRIMARY KEY , "
                    + KEY_VALUECOLUMN + " text"


                    + ");";


    private final Context context;

    private DatabaseHelper myDBHelper;
    private SQLiteDatabase db;

    /////////////////////////////////////////////////////////////////////
    //	Public methods:
    /////////////////////////////////////////////////////////////////////

    mySQLite(Context ctx) {
        super(ctx,TAG,null,DATABASE_VERSION);
        this.context = ctx;
        myDBHelper = new DatabaseHelper(context);
    }


    public mySQLite open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        myDBHelper.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public Uri insertRow(Uri uri, ContentValues values) {

        long value=db.insertWithOnConflict(DATABASE_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE );
        uri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(value));
//        ContentValues initialValues = new ContentValues();
//        initialValues.put(KEY_KEYCOLUMN, values.getAsString("key"));
//        initialValues.put(KEY_VALUECOLUMN, values.getAsString("value"));

        return uri;
//        return db.insert(DATABASE_TABLE, null, initialValues);
    }


    public boolean deleteRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        return db.delete(DATABASE_TABLE, where, null) != 0;
    }

    public void deleteAll() {
        Cursor c = getAllRows();
        long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
        if (c.moveToFirst()) {
            do {
                deleteRow(c.getLong((int) rowId));
            } while (c.moveToNext());
        }
        c.close();
    }


    public Cursor getAllRows() {
        String where = null;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }


    public Cursor getRow(Uri uri, String[] projection, String selection, String[] selectionArgs,
                         String sortOrder) {

//        db = myDBHelper.getWritableDatabase();
//        String where = "key="+"'"+key+"'";
//
//        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
//                where, null, null, null, null, null);
////
//
//        if (c != null) {
//            c.moveToFirst();
//        }.
        String  selectQuery = "";
        if(selection!=null)
            selectQuery  = "SELECT * FROM "+DATABASE_TABLE+" WHERE "+KEY_KEYCOLUMN+" = '"+selection+"'";
        else
            selectQuery = "SELECT * FROM "+DATABASE_TABLE;


        Cursor cursor = db.rawQuery(selectQuery, null);
        return cursor;

    }


    public boolean updateRow(long rowId, String name, int studentNum, String favColour) {
        String where = KEY_ROWID + "=" + rowId;



        ContentValues newValues = new ContentValues();
        newValues.put(KEY_KEYCOLUMN, name);
        newValues.put(KEY_VALUECOLUMN, studentNum);

        // Insert it into the database.
        return db.update(DATABASE_TABLE, newValues, where, null) != 0;
    }



    /////////////////////////////////////////////////////////////////////
    //	Private Helper Classes:
    /////////////////////////////////////////////////////////////////////


    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(DATABASE_CREATE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading application's database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data!");

            // Destroy old database:
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);

            // Recreate new database:
            onCreate(_db);
        }
    }
}


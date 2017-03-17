package mobileappdev.assassingame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/16/2017
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static String DATABASE_NAME;
    // Contacts table name
    private static final String TABLE_PLAYERS = "players";
    // Contacts Table Columns names
    private static final String KEY_EMAIL_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_IS_ALIVE = "is_alive";
    private static final String KEY_CHARACTER = "character";

    public DatabaseHandler(Context context, String gameName) {
        super(context, gameName, null, DATABASE_VERSION);
        DATABASE_NAME = gameName;
    }

    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PLAYERS_TABLE = "CREATE TABLE " + TABLE_PLAYERS + "("
                + KEY_EMAIL_ID + " TEXT PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_CHARACTER + " TEXT,"
                + KEY_IS_ALIVE + " INTEGER"
                + ")";
        db.execSQL(CREATE_PLAYERS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//         Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYERS);

        // Create tables again
        onCreate(db);
    }

    public void addPlayer(Player player) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EMAIL_ID, player.getEmailID());
        values.put(KEY_NAME, player.getName());
        values.put(KEY_CHARACTER, player.getCharacterType().toString());
        values.put(KEY_IS_ALIVE, player.isAlive());

        // Inserting Row
        db.insert(TABLE_PLAYERS, null, values);
        db.close(); // Closing database connection
    }

    public Player getPlayer(String emailID) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PLAYERS,
                new String[] { KEY_EMAIL_ID, KEY_NAME, KEY_CHARACTER, KEY_IS_ALIVE },
                KEY_EMAIL_ID + " = ?",
                new String[] { emailID }, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        if (cursor == null) {
            Log.e("Ajit", "Cursor is null for emailID :" + emailID);
            return null;
        }
        Player player = new Player(cursor.getString(0), cursor.getString(1),
                Character.getCharacterFrom(cursor.getString(2)),
                Integer.parseInt(cursor.getString(3)) == 1);
        cursor.close();
        return player;
    }

    public List<Player> getAllPlayers() {
        List<Player> playerList = new LinkedList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PLAYERS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Player player = new Player(cursor.getString(0), cursor.getString(1),
                        Character.getCharacterFrom(cursor.getString(2)),
                        Integer.parseInt(cursor.getString(3)) == 1);
                playerList.add(player);
            } while (cursor.moveToNext());
        }

//        cursor.close();
        return playerList;
    }

    public int getPlayersCount() {
        String countQuery = "SELECT  * FROM " + TABLE_PLAYERS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
//        cursor.close();
        return cursor.getCount();
    }

    public int updatePlayer(String emailID, boolean isAlive) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_IS_ALIVE, isAlive);

        // updating row
        return db.update(TABLE_PLAYERS, values, KEY_EMAIL_ID + " = ?",
                new String[] {emailID});
    }

    public void deletePlayer(String emailID) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYERS, KEY_EMAIL_ID + " = ?", new String[] {emailID});
        db.close();
    }
}

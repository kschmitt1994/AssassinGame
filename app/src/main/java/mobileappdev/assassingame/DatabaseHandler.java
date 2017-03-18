package mobileappdev.assassingame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private static final String TABLE_PLAYERS = "players1";
    // Contacts Table Columns names
    private static final String KEY_EMAIL_ID = "email_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_IS_ALIVE = "is_alive";
    private static final String KEY_CHARACTER = "character";
    private static final String KEY_INVITATION_STATUS = "invitation_status";

    public DatabaseHandler(Context context, String gameName) {
        super(context, gameName, null, DATABASE_VERSION);
        DATABASE_NAME = gameName;
    }

    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // Creating Tables
    @Override
    public synchronized void onCreate(SQLiteDatabase db) {
        String CREATE_PLAYERS_TABLE = "CREATE TABLE " + TABLE_PLAYERS + "("
                + KEY_NAME + " TEXT PRIMARY KEY,"
                + KEY_EMAIL_ID + " TEXT,"
                + KEY_CHARACTER + " TEXT,"
                + KEY_IS_ALIVE + " INTEGER,"
                + KEY_INVITATION_STATUS + " TEXT"
                + ")";
        db.execSQL(CREATE_PLAYERS_TABLE);
    }

    // Upgrading database
    @Override
    public synchronized void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//         Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYERS);

        // Create tables again
        onCreate(db);
    }

    public synchronized void addPlayer(Player player) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, player.getName());
        values.put(KEY_EMAIL_ID, player.getEmailID());
        values.put(KEY_CHARACTER, player.getGameCharacterType().toString());
        values.put(KEY_IS_ALIVE, player.isAlive());
        values.put(KEY_INVITATION_STATUS, player.getInvitationStatus().toString());

        // Inserting Row
        db.insert(TABLE_PLAYERS, null, values);
        db.close(); // Closing database connection
    }

    public synchronized Player getPlayer(String userName) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PLAYERS,
                new String[] { KEY_NAME, KEY_EMAIL_ID, KEY_CHARACTER, KEY_IS_ALIVE, KEY_INVITATION_STATUS },
                KEY_NAME + " = ?",
                new String[] { userName }, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        if (cursor == null) {
            Log.e("Ajit", "Cursor is null for userName :" + userName);
            return null;
        }
        Player player = new Player(cursor.getString(1), cursor.getString(0),
                GameCharacter.getCharacterFrom(cursor.getString(2)),
                Integer.parseInt(cursor.getString(3)) == 1);
        cursor.close();
        return player;
    }

    public synchronized List<Player> getAllPlayers() {
        List<Player> playerList = new LinkedList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PLAYERS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Player player = new Player(cursor.getString(0), cursor.getString(1),
                        GameCharacter.getCharacterFrom(cursor.getString(2)),
                        Integer.parseInt(cursor.getString(3)) == 1,
                        InvitationStatus.getStatusFrom(cursor.getString(4)));
                playerList.add(player);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return playerList;
    }

    public synchronized Set<String> getAllPlayerNames() {
        return getAllName2PlayerMap().keySet();
    }

    public synchronized Map<String, Player> getAllName2PlayerMap() {
        Map<String, Player> nameList = new HashMap<>();
        String selectQuery = "SELECT  * FROM " + TABLE_PLAYERS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Player player = new Player(cursor.getString(0), cursor.getString(1),
                        GameCharacter.getCharacterFrom(cursor.getString(2)),
                        Integer.parseInt(cursor.getString(3)) == 1,
                        InvitationStatus.getStatusFrom(cursor.getString(4)));
                nameList.put(cursor.getString(0), player);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return nameList;
    }

    public synchronized int getPlayersCount() {
        String countQuery = "SELECT  * FROM " + TABLE_PLAYERS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public synchronized int updatePlayerInviationStatus(String userName, InvitationStatus status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_INVITATION_STATUS, status.toString());

        // updating row
        return db.update(TABLE_PLAYERS, values, KEY_NAME + " = ?", new String[] {userName});
    }

    public synchronized void deletePlayer(String userName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYERS, KEY_NAME + " = ?", new String[] {userName});
        db.close();
    }


}

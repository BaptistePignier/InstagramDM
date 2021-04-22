package com.pignier.instagramdm.Utils;

import com.pignier.instagramdm.R;
import com.pignier.instagramdm.Model.*;
import android.content.Context;
import android.content.ContentValues;
import com.pignier.instagramdm.Utils.Functions;

import android.util.Log;
import java.util.ArrayList;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteTransactionListener;

//https://www.tutlane.com/tutorial/android/android-sqlite-database-with-examples
public class DatabaseHelper extends SQLiteOpenHelper {
	public static int DB_VERSION = 1;

	private static DatabaseHelper sInstance;
	public static final String DB_NAME = "instaDM.db";

	public static final String THREAD_TABLE_NAME = "THREAD";
	public static final String THREAD_DB_ID = "_id"; // Must be _id for CursorAdapter usage
	public static final String THREAD_TITLE = "Titles";
	public static final String THREAD_IMAGE = "Image";
	public static final String THREAD_HAS_NEWER = "HasNewer";
	public static final String THREAD_IS_GROUP = "IsGroup";
	public static final String THREAD_MESSAGE_TABLE_INDEX = "MessageTableIndex";
	public static final String THREAD_INSTA_ID = "ThreadId";
	public static final String THREAD_IS_REQUESTED = "IsRequested";


	public static final String IGSESSION_TABLE_NAME = "IGSESSION";
	public static final String IGSESSION_ID = "id";
	public static final String IGSESSION_SESSION_ID = "SessionID";
	public static final String IGSESSION_CSRF_TOKEN = "CrsfToken";

	public static final String MESSAGE_TABLE_NAME = "MESSAGES";
	public static final String MESSAGE_ID = "_id"; // Must be _id for CursorAdapter usage
	public static final String MESSAGE_TYPE = "Type";
	public static final String MESSAGE_TEXT = "Text";
	public static final String MESSAGE_IMAGE = "Image";
	public static final String MESSAGE_AUDIO = "audio";
	public static final String MESSAGE_OWNER = "Owner";
	public static final String MESSAGE_INSTA_ID = "MessageID";
	public static final String MESSAGE_AUDIO_PLAYED = "AudioPlayed";
	public static final String MESSAGE_THREAD_ID = "ThreadID";


	String TAG = "INSTAGRAMDM";
	String LOCALTAG = "DBHelper : ";
	Context ctx;
	Functions f = new Functions();

	public DatabaseHelper(Context ctx){
		super(ctx,DB_NAME, null, DB_VERSION);
		this.ctx = ctx;
		
	}
	// Create multiple instance of DatabaseHelper may create conflicts if database is writed by multiple instance
	public static synchronized DatabaseHelper getInstance(Context appcContext) {
    	if (sInstance == null) {
      		sInstance = new DatabaseHelper(appcContext);
    	}
    	return sInstance;
  	}
  	public static void removeInstance(){
  		if (sInstance != null){
  			sInstance = null;
  		}
  	}
	

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		// Drop older table if exist
		db.execSQL("DROP TABLE IF EXISTS " + THREAD_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + IGSESSION_TABLE_NAME);
		// Create tables again
		onCreate(db);
	}
	@Override
	public void onCreate(SQLiteDatabase db){
		createOrRecreateTheadTable(db);
		createOrRecreateSessionTable(db);
	}
	
	/// CREATING

	public void createOrRecreateTheadTable(SQLiteDatabase db){
		String DROP_THREAD_TABLE = "DROP TABLE IF EXISTS " + THREAD_TABLE_NAME + ";";
		db.execSQL(DROP_THREAD_TABLE);
		
		String CREATE_THREAD_TABLE = "CREATE TABLE " + THREAD_TABLE_NAME +" ("+
			THREAD_DB_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
			THREAD_TITLE +" TEXT, "+
			THREAD_IMAGE +" BLOB, "+
			THREAD_HAS_NEWER + " INTEGER, "+
			THREAD_IS_GROUP + " INTEGER, "+
			THREAD_INSTA_ID + " TEXT, "+
			THREAD_IS_REQUESTED + " INTEGER);";
		db.execSQL(CREATE_THREAD_TABLE);
	}
	public void createOrRecreateSessionTable(SQLiteDatabase db){
		String DROP_SESSION_TABLE = "DROP TABLE IF EXISTS " + IGSESSION_TABLE_NAME + ";";
		db.execSQL(DROP_SESSION_TABLE);

		String CREATE_SESSION_TABLE = "CREATE TABLE " + IGSESSION_TABLE_NAME +" ("+
			IGSESSION_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
			IGSESSION_SESSION_ID +" TEXT, "+
			IGSESSION_CSRF_TOKEN + " TEXT);";
		db.execSQL(CREATE_SESSION_TABLE);
	}
	public void createOrRecreateMessageTable(SQLiteDatabase db){
		String DROP_MESSAGE_TABLE = "DROP TABLE IF EXISTS " + MESSAGE_TABLE_NAME + ";";
		db.execSQL(DROP_MESSAGE_TABLE);

		String CREATE_MESSAGE_TABLE = "CREATE TABLE " + MESSAGE_TABLE_NAME +" ("+
				MESSAGE_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
				MESSAGE_TYPE +" TEXT, "+
				MESSAGE_TEXT +" TEXT, "+
				MESSAGE_IMAGE + " BLOB, "+
				MESSAGE_AUDIO + " BLOB, "+
				MESSAGE_OWNER + " TEXT, "+
				MESSAGE_INSTA_ID + " TEXT, "+
				MESSAGE_AUDIO_PLAYED + " INTEGER, "+
				MESSAGE_THREAD_ID + " TEXT);";
		db.execSQL(CREATE_MESSAGE_TABLE);
	}

	// DELETING


	public void flushAll(){
		this.ctx.deleteDatabase(DB_NAME);
	}

	// READING

	public ArrayList<ThreadModel> threadsReader(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "+ THREAD_TABLE_NAME,null);

		ArrayList<ThreadModel> threads = new ArrayList<ThreadModel>();
		while (cursor.moveToNext()){
			ThreadModel thread = f.getThreadFromCursor(cursor);
			ArrayList<MessageModel> messages = messagesReader(thread.getInstaID());
			thread.setMessages(messages);
			threads.add(thread);
		}
		cursor.close();
		db.close();
		return threads;
	}
	
	public ArrayList<MessageModel> messagesReader(String threadID){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "+ MESSAGE_TABLE_NAME+" WHERE "+MESSAGE_THREAD_ID+"="+threadID,null);
		ArrayList<MessageModel> messages = new ArrayList<MessageModel>();
		while (cursor.moveToNext()){
			MessageModel message = f.getMessageFromCursor(cursor);
			messages.add(message);
		}
		cursor.close();
		db.close();
		return messages;
	}

	public IGSession sessionReader(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "+ IGSESSION_TABLE_NAME,null);
		IGSession session = null;
		while (cursor.moveToNext()){
			session = new IGSession(
			cursor.getString(cursor.getColumnIndex(IGSESSION_SESSION_ID)),
			cursor.getString(cursor.getColumnIndex(IGSESSION_CSRF_TOKEN)));
		}
		cursor.close();
		db.close();
		return session;
	}
	public ThreadModel getThreadByPosition(int positionOnListView){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "+THREAD_TABLE_NAME+" WHERE "+THREAD_DB_ID+"="+positionOnListView,null);
		cursor.moveToFirst();
		ThreadModel thread = f.getThreadFromCursor(cursor);
		cursor.close();
		db.close();
		return thread;
	}


	// WRITING

	public void threadsWriter(ArrayList<ThreadModel> threads){
		
		new DBTrans(this){
			@Override
			public void work(){
				createOrRecreateTheadTable(this.db);
				createOrRecreateMessageTable(this.db);
				for (ThreadModel thread: threads){
					for (MessageModel message : thread.getMessages()){
						writeSingleMessage(message,this.db);
					}
					
					long newRowId = this.db.insert(THREAD_TABLE_NAME,null, thread.extract());
				}
			}
		}.start();
	}

	public void sessionWriter(IGSession session){
		new DBTrans(this){
			@Override
			public void work(){
				ContentValues cValues = session.extract();
				long newRowId = db.insert(IGSESSION_TABLE_NAME,null, cValues);
			}
		}.start();
	} 


	//This method wite only titles and no messages unlinke conversationsWriter
	public void writeTitle(ArrayList<ThreadModel> threads){
		new DBTrans(this){
			@Override
			public void work(){
				createOrRecreateTheadTable(this.db);
				for (ThreadModel thread: threads){
					long newRowId = this.db.insert(THREAD_TABLE_NAME,null, thread.extract());
				}
			}
		}.start();

	}
	public void addMessageTo(MessageModel message){
		new DBTrans(this){
			@Override
			public void work(){
				writeSingleMessage(message,this.db);
			}
		}.start();
	}
	
	
	// UPDATING


	// https://stackoverflow.com/a/45778613
	public void updateConversation(ThreadModel thread){
		new DBTrans(this){
			@Override
			public void work(){
				this.db.update(THREAD_TABLE_NAME, thread.extract(), THREAD_INSTA_ID+"=?", new String[]{thread.getInstaID()});
			}
		}.start();

	}
	
	public void updateMessage(MessageModel message){
		new DBTrans(this){
			@Override
			public void work(){
				this.db.update(MESSAGE_TABLE_NAME, message.extract(), MESSAGE_INSTA_ID+"=?", new String[]{message.getItemID()});
			}
		}.start();

	}

	
	

	// INFORMATION
	
	
	

	
	
	
	public boolean AreThereRequestedConversations(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "+ THREAD_TABLE_NAME+" WHERE "+THREAD_IS_REQUESTED+"=1",null);
		boolean result = (cursor.getCount() > 0);
		cursor.close();
		db.close();
		return result;
	}

	public boolean isThereSession(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "+ IGSESSION_TABLE_NAME,null);
		
		boolean tableExists = (cursor.getCount() == 1);
		cursor.close();
		db.close();
		return tableExists;
	}
	public boolean AreThereConversations(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "+ THREAD_TABLE_NAME,null);
		boolean tableExists = (cursor.getCount() > 0);
		cursor.close();
		db.close();
		return tableExists;
	}
	


	// Internal method


	public void writeSingleMessage(MessageModel message,SQLiteDatabase db){
		ContentValues cValues = message.extract();
		long newRowId = db.insert(MESSAGE_TABLE_NAME,null, cValues);
		
	}

	

	public abstract class DBTrans {
		SQLiteDatabase db;
		DatabaseHelper helper;
		public DBTrans(DatabaseHelper helper) {
			this.db = helper.getWritableDatabase();
			this.helper = helper;
		}
		public void start(){
			
			db.beginTransactionWithListener(new SQLiteTransactionListener() {
				@Override
				public void onBegin() {}
				@Override
				public void onCommit() {
					//helper.updateVersion();
				}
				@Override
				public void onRollback() {}
			});
			try{
				work();
				db.setTransactionSuccessful();
			}catch(SQLiteException e){
				Log.e(TAG, LOCALTAG+Log.getStackTraceString(e));
			}finally {
				db.endTransaction();
				// close here may cause an error : 
				// java.lang.IllegalStateException: attempt to re-open an already-closed object:
				// database iis closed by openhelper is service directly
			}
		}
		public abstract void work();
	}
}
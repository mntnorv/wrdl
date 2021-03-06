package com.mntnorv.wrdl.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WrdlDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "gamestates.db";
	private static final int DATABASE_VERSION = 3;

	public WrdlDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		GameStatesTable.onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		GameStatesTable.onUpgrade(db, oldVersion, newVersion);
	}
}

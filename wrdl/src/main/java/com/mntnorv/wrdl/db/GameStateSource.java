package com.mntnorv.wrdl.db;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.mntnorv.wrdl.GameState;

public class GameStateSource extends AbstractDatabaseSource<GameState> {
	public GameStateSource(Context context, LoaderManager loaderManager) {
		super(context, loaderManager);
	}

	@Override
	protected Uri getBaseUri() {
		return WrdlContentProvider.GAME_STATES_URI;
	}

	@Override
	protected Uri getItemUri(int id) {
		return getBaseUri().buildUpon().appendPath(Integer.toString(id)).build();
	}

	@Override
	protected Uri getItemUri(GameState item) {
		return getItemUri(item.getId());
	}

	@Override
	protected ContentValues getContentValuesFromObject(GameState item) {
		return item.toContentValues();
	}

	@Override
	protected GameState createDataObjectFromCursor(Cursor cursor) {
		return GameState.fromCursor(cursor);
	}

	@Override
	protected String getSortOrder(Bundle args) {
		return GameStatesTable.COLUMN_ID + " DESC";
	}
}

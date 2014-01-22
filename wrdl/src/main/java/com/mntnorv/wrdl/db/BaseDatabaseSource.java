package com.mntnorv.wrdl.db;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

public abstract class BaseDatabaseSource<T> {
	protected static final String ITEM_ID_KEY = "itemId";

	private Context mContext;
	private LoaderManager mLoaderManager;

	private final int MIN_LOADER_ID;
	private final int MAX_LOADER_ID;
	int nextId;

	SparseArray<PublishSubject<List<T>>> listeners =
			new SparseArray<PublishSubject<List<T>>>();

	public BaseDatabaseSource(Context context, LoaderManager loaderManager) {
		MIN_LOADER_ID = LoaderIdManager.getNextId();
		MAX_LOADER_ID = MIN_LOADER_ID + LoaderIdManager.getMaxIdsPerLoader();
		nextId = MIN_LOADER_ID;

		mContext = context;
		mLoaderManager = loaderManager;
	}

	/**
	 * Starts a new loader
	 * @param args - loader arguments
	 */
	protected Observable<List<T>> startLoader(Bundle args) {
		int currentId = getNextId();
		PublishSubject<List<T>> subject = PublishSubject.create();
		listeners.append(currentId, subject);
		mLoaderManager.initLoader(currentId, args, callbacks);
		return subject;
	}

	/**
	 * Cancels a loader by its id
	 * @param id of the loader to cancel
	 */
	public final void cancelLoader(int id) {
		mLoaderManager.destroyLoader(id);
		listeners.remove(id);
	}

	/**
	 * Gets the next available loader id
	 * @return the next available loader id
	 */
	private final synchronized int getNextId() {
		int nextValidId = nextId;
		nextId++;

		if (nextId == MAX_LOADER_ID) {
			nextId = MIN_LOADER_ID;
		}

		return nextValidId;
	}

	//================================================================================
	// Getters
	//================================================================================

	protected Context getContext() {
		return mContext;
	}

	//================================================================================
	// Abstract methods
	//================================================================================

	/**
	 * Creates an object (of class {@code T}) from a cursor
	 * @param cursor - database result cursor
	 * @return an object of class {@code T}
	 */
	protected abstract T createDataObjectFromCursor(Cursor cursor);

	/**
	 * Get the base Uri pointing to all items
	 * @return the base Uri
	 */
	protected abstract Uri getBaseUri();

	/**
	 * Get a Uri pointing to a specific item
	 * @param id of the item
	 * @return the item's Uri
	 */
	protected abstract Uri getItemUri(int id);

	/**
	 * Get all columns names to load for this data.
	 * @return an array of column names to load
	 */
	protected abstract String[] getDataColumns();

	//================================================================================
	// Overridable protected methods
	//================================================================================

	/**
	 * Get selection string from the bundle passed to a loader.
	 * @param args - the loader's arguments
	 * @return the selection string if it exists in the arguments,
	 * {@code null} otherwise
	 */
	protected String getSelectionFromBundle(Bundle args) {
		return null;
	}

	/**
	 * Get selection argument array from the bundle passed to a loader.
	 * @param args - the loader's arguments
	 * @return the selection argument array if it exists in the arguments,
	 * {@code null} otherwise
	 */
	protected String[] getSelectionArgsFromBundle(Bundle args) {
		return null;
	}

	/**
	 * Get sort order from the bundle passed to a loader.
	 * @param args - the loader's arguments
	 * @return the sort order if it exists in the arguments,
	 * {@code null} otherwise
	 */
	protected String getSortOrderFromBundle(Bundle args) {
		return null;
	}

	//================================================================================
	// Loader callbacks
	//================================================================================
	private LoaderManager.LoaderCallbacks<Cursor> callbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			Uri uri = getBaseUri();

			if (args != null) {
				if (args.containsKey(ITEM_ID_KEY)) {
					uri = getItemUri(args.getInt(ITEM_ID_KEY));
				}
			}

			CursorLoader cursorLoader = new CursorLoader(
					mContext,
					uri,
					getDataColumns(),
					getSelectionFromBundle(args),
					getSelectionArgsFromBundle(args),
					getSortOrderFromBundle(args));

			return cursorLoader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			PublishSubject<List<T>> subject = listeners.get(loader.getId());
			listeners.remove(loader.getId());
			if (subject != null) {
				List<T> results = new ArrayList<T>();
				data.moveToFirst();
				while (!data.isAfterLast()) {
					results.add(createDataObjectFromCursor(data));
					data.moveToNext();
				}
				data.close();

				subject.onNext(results);
				subject.onCompleted();
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
		}
	};
}

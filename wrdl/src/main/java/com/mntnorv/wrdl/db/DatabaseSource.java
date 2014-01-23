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

public abstract class DatabaseSource<T> {
	protected static final String ITEM_ID_KEY = "itemId";
	protected static final String ITEM_COUNT_KEY = "itemCount";
	protected static final String FIRST_ITEM_KEY = "firstItem";
	protected static final String ORDER_BY_KEY = "orderBy";

	private Context mContext;
	private LoaderManager mLoaderManager;

	private final int MIN_LOADER_ID;
	private final int MAX_LOADER_ID;
	int nextId;

	SparseArray<PublishSubject<List<T>>> listeners =
			new SparseArray<PublishSubject<List<T>>>();

	public DatabaseSource(Context context, LoaderManager loaderManager) {
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

	private String getLimitStringFromBundle(Bundle args) {
		StringBuilder limitString = new StringBuilder();

		if (args.containsKey(ITEM_COUNT_KEY)) {
			limitString
					.append("LIMIT ")
					.append(args.getInt(ITEM_COUNT_KEY));
		}

		if (args.containsKey(FIRST_ITEM_KEY)) {
			if (limitString.length() > 0) {
				limitString.append(" ");
			}

			limitString
					.append("OFFSET ")
					.append(args.getInt(FIRST_ITEM_KEY));
		}

		if (limitString.length() > 0) {
			return limitString.toString();
		} else {
			return null;
		}
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

	//================================================================================
	// Overridable protected methods
	//================================================================================

	/**
	 * Get all columns names to load for this data.
	 * @return an array of column names to load
	 */
	protected String[] getDataColumns() {
		return null;
	}

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
			String sortLimitString = null;

			if (args != null) {
				if (args.containsKey(ITEM_ID_KEY)) {
					uri = getItemUri(args.getInt(ITEM_ID_KEY));
				} else {
					sortLimitString = "";
					String sortOrder = getSortOrderFromBundle(args);
					String limitString = getLimitStringFromBundle(args);

					if (sortOrder != null) {
						sortLimitString += sortOrder;
					}

					if (limitString != null) {
						sortLimitString += limitString;
					}

					if (sortLimitString.equals("")) {
						sortLimitString = null;
					}
				}
			}

			CursorLoader cursorLoader = new CursorLoader(
					mContext,
					uri,
					getDataColumns(),
					getSelectionFromBundle(args),
					getSelectionArgsFromBundle(args),
					sortLimitString
			);

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

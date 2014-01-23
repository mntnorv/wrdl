package com.mntnorv.wrdl.db;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.mntnorv.wrdl.EditableDataSource;

import java.util.List;

import rx.Observable;

public abstract class AbstractDatabaseSource<T> extends DatabaseSource<T> implements EditableDataSource<T> {
	private ContentResolver mContentResolver;

	public AbstractDatabaseSource(Context context, LoaderManager loaderManager) {
		super(context, loaderManager);
		mContentResolver = context.getContentResolver();
	}

	@Override
	public Observable<List<T>> getItems(int numberOfItems, int startAt) {
		Bundle args = new Bundle();
		args.putInt(ITEM_COUNT_KEY, numberOfItems);
		args.putInt(FIRST_ITEM_KEY, startAt);
		return startLoader(args);
	}

	@Override
	public Observable<List<T>> getAllItems() {
		return startLoader(null);
	}

	@Override
	public Observable<List<T>> getItemById(int id) {
		Bundle args = new Bundle();
		args.putInt(ITEM_ID_KEY, id);
		return startLoader(args);
	}

	@Override
	public int addItem(T item) {
		Uri itemUri = mContentResolver.insert(getBaseUri(), getContentValuesFromObject(item));
		return Integer.parseInt(itemUri.getLastPathSegment());
	}

	@Override
	public void updateItem(T item) {
		mContentResolver.update(getItemUri(item), getContentValuesFromObject(item), null, null);
	}

	@Override
	public void removeItem(T item) {
		mContentResolver.delete(getItemUri(item), null, null);
	}

	@Override
	public void removeAllItems() {
		mContentResolver.delete(getBaseUri(), null, null);
	}

	//================================================================================
	// Abstract methods
	//================================================================================

	/**
	 * Creates content values from an object
	 * @param item to convert to content values
	 * @return content values created from the passed object
	 */
	protected abstract ContentValues getContentValuesFromObject(T item);

	/**
	 * Get a Uri pointing to a specific item
	 * @param item to get the Uri of
	 * @return the item's Uri
	 */
	protected abstract Uri getItemUri(T item);
}

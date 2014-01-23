package com.mntnorv.wrdl;

import java.util.List;

import rx.Observable;

public interface ImutableDataSource<T> {
	/**
	 * Get a list of items.
	 * @param numberOfItems - the number of items to get.
	 * @param startAt - the first item to be returned (starting at 0).
	 * @return an Observable with the loaded items.
	 */
	public Observable<List<T>> getItems(int numberOfItems, int startAt);

	/**
	 * Get all items from this source.
	 * @return an Observable with the loaded items.
	 */
	public Observable<List<T>> getAllItems();

	/**
	 * Get an item by its id.
	 * @param itemId - the id of the item to get.
	 * @return an Observable with the loaded items.
	 */
	public Observable<List<T>> getItemById(final int itemId);
}

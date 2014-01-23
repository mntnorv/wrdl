package com.mntnorv.wrdl;

public interface EditableDataSource<T> extends ImutableDataSource<T> {
	/**
	 * Adds an item to the data repository.
	 * @param item - the item to add
	 * @return the id of the added item
	 */
	public int addItem(T item);

	/**
	 * Updates an item already in the repository.
	 * @param item - the updated item
	 */
	public void updateItem(T item);

	/**
	 * Removes an item from the data repository.
	 * @param item - the item to be removed
	 */
	public void removeItem(T item);

	/**
	 * Removes all items from the data repository.
	 */
	public void removeAllItems();
}

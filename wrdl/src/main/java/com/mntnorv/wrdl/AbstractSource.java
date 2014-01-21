package com.mntnorv.wrdl;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Func1;

public abstract class AbstractSource<T> {
	/**
	 * Get a list of items. Returns an observable that calls onNext() on every
	 * loaded item.
	 * @param numberOfItems - the number of items to get.
	 * @param startAt - the first item to be returned (starting at 0).
	 */
	public Observable<List<T>> getItems(final int numberOfItems, final int startAt) {
		return createListObservable(new GetListFunc<T>() {
			@Override
			public List<T> getList() {
				return loadItems(numberOfItems, startAt);
			}
		});
	}

	/**
	 * Get all items from this source. Loads asynchronously and returns items
	 * through the specified listener.
	 */
	public Observable<List<T>> getAllItems() {
		return createListObservable(new GetListFunc<T>() {
			@Override
			public List<T> getList() {
				return loadAllItems();
			}
		});
	}

	/**
	 * Get an item by its id. Loads asynchronously and returns the item
	 * through the specified listener.
	 * @param itemId - the id of the item to get.
	 */
	public Observable<List<T>> getItemById(final int itemId) {
		return createListObservable(new GetListFunc<T>() {
			@Override
			public List<T> getList() {
				return loadItemById(itemId);
			}
		});
	}

	/**
	 * Get a list of items. This method is used by
	 * {@link AbstractSource#getItems(int, int)}.
	 * Asynchronous loading is handled by the caller and should not be
	 * implemented in this method.
	 * @param numberOfItems - the number of items to get.
	 * @param startAt - the first item to be returned (starting at 0).
	 * @return the list of items loaded.
	 */
	protected abstract List<T> loadItems(int numberOfItems, int startAt);

	/**
	 * Get all items from this source. This method is used by
	 * {@link AbstractSource#getAllItems()}.
	 * Asynchronous loading is handled by the caller and should not be
	 * implemented in this method.
	 * @return the list of items loaded.
	 */
	protected abstract List<T> loadAllItems();

	/**
	 * Get an item by its id. This method is used by
	 * {@link #getItemById(int)}.
	 * Asynchronous loading is handled by the caller and should not be
	 * implemented in this method.
	 * @param itemId - the id of the item to get.
	 * @return the list of items loaded.
	 */
	protected abstract List<T> loadItemById(int itemId);

	/**
	 * Clone the this source into an identical source.
	 */
	public abstract Object clone();

	/**
	 * Create an asynchronous list observable that uses a {@link GetListFunc} to
	 * get the list.
	 * @param getListFunc - a function that returns the list
	 * @return a list observable
	 */
	private Observable<List<T>> createListObservable(final GetListFunc<T> getListFunc) {
		return Observable.create(new Func1<Observer<List<T>>, Subscription>() {
			@Override
			public Subscription call(Observer<List<T>> listObserver) {
				try {
					List<T> result = getListFunc.getList();
					listObserver.onNext(result);
					listObserver.onCompleted();
				} catch (Exception ex) {
					listObserver.onError(ex);
				}
				return Subscriptions.empty();
			}
		});
	}

	/**
	 * Get list function interface. Used to simplify creating async list
	 * observables.
	 * @param <T>
	 */
	private interface GetListFunc<T> {
		public List<T> getList();
	}
}

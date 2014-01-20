package com.mntnorv.wrdl.dict;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Func1;

public class DictionaryProvider {
	private static Dictionary dict;
	private static int mOpenedDict = 0;

	public static Observable<Dictionary> getDictionary(final Context context, final int resourceId) {
		return Observable.create(new Func1<Observer<Dictionary>, Subscription>() {
			@Override
			public Subscription call(Observer<Dictionary> dictionaryObserver) {

				try {
					if (mOpenedDict != resourceId) {
						dict = openDictionary(context, resourceId);
					}

					dictionaryObserver.onNext(dict);
					dictionaryObserver.onCompleted();
				} catch (IOException e) {
					dictionaryObserver.onError(e);
				}

				return Subscriptions.empty();
			}
		});
	}

	private static Dictionary openDictionary(Context context, int resourceId) throws IOException {
		InputStream stream = context.getResources().openRawResource(resourceId);
		Dictionary dict = new Dictionary(stream);
		stream.close();
		mOpenedDict = resourceId;
		return dict;
	}
}

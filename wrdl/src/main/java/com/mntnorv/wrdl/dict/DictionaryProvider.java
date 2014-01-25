package com.mntnorv.wrdl.dict;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

public class DictionaryProvider {
	private static Dictionary dict;
	private static int mOpenedDict = 0;

	public static Observable<Dictionary> getDictionary(final Context context, final int resourceId) {
		return Observable.create(new Observable.OnSubscribeFunc<Dictionary>() {
			@Override
			public Subscription onSubscribe(Observer<? super Dictionary> observer) {
				try {
					if (mOpenedDict != resourceId) {
						dict = openDictionary(context, resourceId);
					}

					observer.onNext(dict);
					observer.onCompleted();
				} catch (IOException e) {
					observer.onError(e);
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

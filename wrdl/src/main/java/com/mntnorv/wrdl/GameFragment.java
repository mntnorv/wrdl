package com.mntnorv.wrdl;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mntnorv.wrdl.dict.Dictionary;
import com.mntnorv.wrdl.dict.DictionaryProvider;

import rx.Observer;
import rx.concurrency.Schedulers;

public class GameFragment extends Fragment {
	public GameFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_game, container, false);

		DictionaryProvider.getDictionary(getActivity(), R.raw.sowpods3)
				.subscribeOn(Schedulers.newThread())
				.observeOn(Schedulers.currentThread())
				.subscribe(new DictionaryObserver());

		return rootView;
	}

	private class DictionaryObserver implements Observer<Dictionary> {
		@Override
		public void onNext(Dictionary dictionary) {}

		@Override
		public void onError(Throwable throwable) {}

		@Override
		public void onCompleted() {}
	}
}

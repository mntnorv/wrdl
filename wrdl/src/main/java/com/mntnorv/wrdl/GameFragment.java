package com.mntnorv.wrdl;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mntnorv.wrdl.db.GameStateSource;
import com.mntnorv.wrdl.dict.Dictionary;
import com.mntnorv.wrdl.dict.DictionaryProvider;
import com.mntnorv.wrdl.dict.LetterGrid;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.concurrency.Schedulers;
import rx.util.functions.Action1;

public class GameFragment extends Fragment {
	public GameFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_game, container, false);

		DictionaryProvider.getDictionary(getActivity(), R.raw.sowpods3)
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new DictionaryObserver());

		new GameStateSource(getActivity(), getLoaderManager())
				.getAllItems()
				.subscribeOn(Schedulers.newThread())
				.observeOn(Schedulers.currentThread())
				.subscribe(new Action1<List<GameState>>() {
					@Override
					public void call(List<GameState> gameStates) {
						Log.d("GameStates", Integer.toString(gameStates.size()));
					}
				});

		return rootView;
	}

	private class DictionaryObserver implements Observer<Dictionary> {
		@Override
		public void onNext(Dictionary dictionary) {
			Toast.makeText(
					getActivity().getApplicationContext(),
					Integer.toString(
						new LetterGrid(new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P"}, 4, 4).getWordsInGrid(dictionary).size()
					),
					Toast.LENGTH_SHORT
			).show();
		}

		@Override
		public void onError(Throwable throwable) {}

		@Override
		public void onCompleted() {}
	}
}

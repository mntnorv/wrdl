package com.mntnorv.wrdl.dict;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LetterGrid {
	private List<Letter> mGrid;
	private int mMaxWordLength;

	/**
	 * Create a new LetterGrid.
	 *
	 * @param letters - letter array. Length must be equal to
	 *                {@code columns*rows}.
	 * @param columns - number of columns in the grid.
	 * @param rows    - number of rows in the grid.
	 */
	public LetterGrid(String[] letters, int columns, int rows) {
		if (letters.length != columns * rows) {
			throw new IllegalArgumentException("Size of letters array must be columns*rows");
		}

		mGrid = new ArrayList<Letter>();
		mMaxWordLength = 8;

		int[] directions = {
				-columns,     // UP
				 columns,     // DOWN
				-1,           // LEFT
				 1,           // RIGHT
				-columns - 1, // UP LEFT
				-columns + 1, // UP RIGHT
				 columns - 1, // DOWN LEFT
				 columns + 1  // DOWN RIGHT
		};

		for (int i = 0; i < columns * rows; i++) {
			mGrid.add(new Letter(letters[i]));
		}

		for (int i = 0; i < columns * rows; i++) {
			for (int j = 0; j < 8; j++) {
				int row = i / columns;
				int col = i % columns;

				// If on the left side, do not wrap to the right
				if (col == 0 && (j == 2 || j == 4 || j == 6)) {
					continue;
				}

				// If on the right side, do not wrap to the left
				if (col == (columns - 1) && (j == 3 || j == 5 || j == 7)) {
					continue;
				}

				// If on the top, do not wrap to the bottom
				if (row == 0 && (j == 0 || j == 4 || j == 5)) {
					continue;
				}

				// If on the bottom, do not wrap to the top
				if (row == (rows - 1) && (j == 1 || j == 6 || j == 7)) {
					continue;
				}

				mGrid.get(i).bordering.add(mGrid.get(i + directions[j]));
			}
		}
	}

	/**
	 * Finds all words (from the given Dictionary) in the grid.
	 *
	 * @param dict - dictionary
	 * @return a list of words found in the grid
	 */
	public Set<String> getWordsInGrid(Dictionary dict) {
		Set<String> words = new HashSet<String>();
		iterateWordsRecursive(mGrid, dict, "", words);

		Log.d("LetterGrid", Integer.toString(words.size()));

		return words;
	}

	/**
	 * Gets number of words (from the given Dictionary) in the grid
	 *
	 * @param dict - dictionary
	 * @return number of words found
	 */
	public int getWordCountInGrid(Dictionary dict) {
		return getWordsInGrid(dict).size();
	}

	/**
	 * Set maximum length of word to search for
	 *
	 * @param maxWordLength
	 */
	public void setMaxWordLength(int maxWordLength) {
		this.mMaxWordLength = maxWordLength;
	}

	/**
	 * Recursive word search in current LetterGrid
	 *
	 * @param lGrid - list of all letters to check in current recursive call
	 * @param dict  - dictionary
	 * @param word  - current word, filled with each deeper recursive call
	 * @param words - list of words found
	 */
	private void iterateWordsRecursive(List<Letter> lGrid, Dictionary dict, String word, Set<String> words) {
		if (word.length() <= mMaxWordLength) {
			for (Letter current : lGrid) {
				if (!current.used) {
					if (dict.contains(word + current.string)) {
						words.add(word + current.string);
					}

					if (dict.containsPrefix(word + current.string)) {
						current.used = true;
						iterateWordsRecursive(current.bordering, dict, word + current.string, words);
						current.used = false;
					}
				}
			}
		}
	}

	/**
	 * Element used in LetterGrid
	 */
	private class Letter {
		public List<Letter> bordering;
		public String string;
		public boolean used;

		public Letter(String str) {
			string = str.toUpperCase(Locale.US);
			used = false;
			bordering = new ArrayList<Letter>(8);
		}
	}
}

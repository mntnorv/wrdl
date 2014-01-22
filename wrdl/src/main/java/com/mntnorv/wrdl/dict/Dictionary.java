package com.mntnorv.wrdl.dict;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Dictionary {
	private List<byte[]> mWordList;

	public Dictionary(InputStream stream) throws IOException {
		BufferedInputStream bufStream = new BufferedInputStream(stream);
		mWordList = new ArrayList<byte[]>();

		readWords(bufStream);
	}

	private void readWords(BufferedInputStream inputStream) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] readBuffer = new byte[1024];

		int bytesRead;
		while (inputStream.available() > 0) {
			bytesRead = inputStream.read(readBuffer);
			for (int i = 0; i < bytesRead; i++) {
				if (readBuffer[i] != '\n') {
					outputStream.write(readBuffer[i]);
				} else {
					mWordList.add(outputStream.toByteArray());
					outputStream.reset();
				}
			}
		}

		Log.d("Dictionary", Integer.toString(mWordList.size()));

		outputStream.close();
	}

	private byte[] encodeWord(String word) {
		byte[] encoded = new byte[word.length()];

		for (int i = 0; i < word.length(); i++) {
			encoded[i] = (byte) word.charAt(i);
		}

		return encoded;
	}

	private String decodeWord(byte[] word) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < word.length; i++) {
			builder.append((char) word[i]);
		}

		return builder.toString();
	}

	public boolean contains(String word) {
		return Collections.binarySearch(mWordList, encodeWord(word), dictComparator) >= 0;
	}

	public boolean containsPrefix (String prefix) {
		int result = Collections.binarySearch(mWordList, encodeWord(prefix), dictComparator);

		if (result >= 0) {
			return true;
		} else if ((-result-1) < mWordList.size()) {
			return decodeWord(mWordList.get(-result-1)).startsWith(prefix);
		} else {
			return false;
		}
	}

	private Comparator<byte[]> dictComparator = new Comparator<byte[]> () {
		@Override
		public int compare(byte[] lhs, byte[] rhs) {
			int minLength = lhs.length > rhs.length ? rhs.length : lhs.length;

			for (int i = 0; i < minLength; i++) {
				if (lhs[i] != rhs[i]) {
					return lhs[i] - rhs[i];
				}
			}

			return lhs.length - rhs.length;
		}
	};
}

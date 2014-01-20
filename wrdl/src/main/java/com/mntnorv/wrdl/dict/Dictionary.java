package com.mntnorv.wrdl.dict;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Dictionary {
	private List<byte[]> words;

	public Dictionary(InputStream stream) {
		BufferedInputStream bufStream = new BufferedInputStream(stream);
		words = new ArrayList<byte[]>(200000);

		readWords(bufStream);
	}

	private void readWords(BufferedInputStream stream) {

	}
}

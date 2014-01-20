package com.mntnorv.wrdl.dict;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Dictionary {
	private List<byte[]> words;

	public Dictionary(InputStream stream) throws IOException {
		BufferedInputStream bufStream = new BufferedInputStream(stream);
		words = new ArrayList<byte[]>(200000);

		readWords(bufStream);
	}

	private void readWords(BufferedInputStream inputStream) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		int byteRead;
		while ((byteRead = inputStream.read()) != -1) {
			if (byteRead != '\n') {
				outputStream.write(byteRead);
			} else {
				words.add(outputStream.toByteArray());
				outputStream.reset();
			}
		}

		Log.d("Dictionary", Integer.toString(words.size()));

		outputStream.close();
	}
}

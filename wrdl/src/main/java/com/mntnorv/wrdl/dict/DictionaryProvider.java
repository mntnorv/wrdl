package com.mntnorv.wrdl.dict;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class DictionaryProvider {
    private static Dictionary dict;
    private static int mOpenedDict = 0;

    public static Dictionary getDictionary(Context context, int resourceId) {
        if (mOpenedDict != resourceId) {
            dict = openDictionary(context, resourceId);
        }

        return dict;
    }

    private static Dictionary openDictionary(Context context, int resourceId) {
        try {
            GZIPInputStream gzipStream = new GZIPInputStream(context.getResources().openRawResource(resourceId));
            Dictionary dict = new Dictionary(gzipStream);
            gzipStream.close();
            mOpenedDict = resourceId;
            return dict;
        } catch (IOException e) {
            Log.e("DictionaryProvider", "unable to open dictionary from resource " + Integer.toString(resourceId));
            mOpenedDict = 0;
            return null;
        }
    }
}

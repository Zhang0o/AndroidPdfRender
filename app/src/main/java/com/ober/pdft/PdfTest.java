package com.ober.pdft;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by ober on 20-7-29.
 */
public class PdfTest {

    public static final String TAG = "PdfTest";

    static final String[] LIBS = {
            "pdfium.cr",
            "icuuc" ,
            "icuuc_hidden_visibility.cr",
            "icui18n_hidden_visibility.cr",
            "icui18n",
            "chrome_zlib.cr",
            "c++_chrome.cr"
    };

    public static void init() {
        for(String s : LIBS) {
            Log.d(TAG, "System loadLibrary " + s);
            System.loadLibrary(s);
        }

    }

    public static native int decodePdf(int fd, Bitmap bmp);

}

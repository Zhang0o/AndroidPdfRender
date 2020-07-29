package com.ober.pdft;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.TextView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {

    static {
        PdfTest.init();

        System.loadLibrary("native-lib");
    }

    private File target;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            prepareTest();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());



        ParcelFileDescriptor file;
        try {
             file = ParcelFileDescriptor.open(target,
                    ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        int fd = getNumFd(file);

        Bitmap bmp = Bitmap.createBitmap(2048, 2048, Bitmap.Config.ARGB_8888);
        PdfTest.decodePdf(fd, bmp);


    }


    public native String stringFromJNI();


    private void prepareTest() throws IOException {
        target = new File(getCacheDir(), "test.pdf");
        AssetManager am = getAssets();
        InputStream is = am.open("test.pdf");
        FileOutputStream fos = new FileOutputStream(target);
        byte[] buf = new byte[1024 * 8];
        int r;
        while ((r = is.read(buf)) != -1) {
            fos.write(buf, 0, r);
        }

        is.close();
        fos.close();
    }

    public static int getNumFd(ParcelFileDescriptor fdObj) {
        try {
            Field fdField = FileDescriptor.class.getDeclaredField("descriptor");
            fdField.setAccessible(true);

            return fdField.getInt(fdObj.getFileDescriptor());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

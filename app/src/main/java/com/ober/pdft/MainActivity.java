package com.ober.pdft;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
    private ImageView imageView;

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

        imageView = findViewById(R.id.image);

        findViewById(R.id.btn_native).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setImageBitmap(null);
                renderWithAndroid();
            }
        });

        findViewById(R.id.btn_opdf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setImageBitmap(null);
                renderWithOPdf();
            }
        });

    }

    private void renderWithAndroid() {
        ParcelFileDescriptor file;
        try {
            file = ParcelFileDescriptor.open(target,
                    ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Bitmap bmp = Bitmap.createBitmap(2048, 2048, Bitmap.Config.ARGB_8888);

        if(Build.VERSION.SDK_INT>21) {
            try {
                bmp.eraseColor(Color.TRANSPARENT);
                PdfRenderer pdfRenderer = new PdfRenderer(file);
                PdfRenderer.Page page = pdfRenderer.openPage(0);
                Rect rect = new Rect();
                rect.set(0, 0, 2048, 2048);
                Matrix matrix = new Matrix();
                matrix.postScale(2.0f, 2.0f);
                Log.i("PdfRenderer", "page opened");
                page.render(bmp, rect, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                Log.i("PdfRenderer", "page rendered");
                page.close();
                pdfRenderer.close();
                file.close();
                imageView.setImageBitmap(bmp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void renderWithOPdf() {
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

        try {
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        imageView.setImageBitmap(bmp);
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

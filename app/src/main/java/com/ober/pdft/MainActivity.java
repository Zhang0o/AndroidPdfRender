package com.ober.pdft;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ober.pdft.s.SurfaceDemoActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            prepareTestResource();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TextView tv = findViewById(R.id.sample_text);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToDemo();
            }
        });
    }

    private void goToDemo() {
        Intent intent = new Intent(this, SurfaceDemoActivity.class);
        startActivity(intent);
    }

    private void prepareTestResource() throws IOException {
        File target = new File(getCacheDir(), "test.pdf");
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
}

package com.ober.pdft.s;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ober.opdf.surface.OPdfCallback;
import com.ober.opdf.surface.render.ViewGestureHelper;
import com.ober.opdf.surface.render.OPdfSurfaceRenderer;
import com.ober.pdft.R;

import java.io.File;

/**
 * Created by ober on 2020/7/30.
 */
public class SurfaceDemoActivity extends AppCompatActivity {

    private static final String TAG = "OPdf";

    SurfaceView surfaceView;
    ProgressBar progressBar;

    OPdfSurfaceRenderer mRenderer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_demo);

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.setZOrderOnTop(true);
        surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        progressBar = findViewById(R.id.progressBar);
        File pdfFile = new File(getCacheDir(), "test.pdf");

        mRenderer = new OPdfSurfaceRenderer(surfaceView, pdfFile, 0);
        //improve drawing result when transforming, it will cause some performance lost
        mRenderer.setOptimizeTransformDrawing(true);

        ViewGestureHelper.bindView(surfaceView, mRenderer);
        mRenderer.setOPdfCallback(new OPdfCallbackImpl());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class OPdfCallbackImpl implements OPdfCallback {

        @Override
        public void onPdfPageOpen(int pageWidth, int pageHeight) {
            Log.i(TAG, "onPdfPageOpen " + pageWidth + "," + pageHeight);
        }

        @Override
        public void onPdfPreviewReady() {
            Log.i(TAG, "onPdfPreviewReady");
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onPdfClick(int x, int y, int pdfWidth, int pdfHeight) {
            Log.i(TAG, "onPdfClick " + x + "," + y);
            if(x >= 0 && x <= pdfWidth && y >= 0 && y <= pdfHeight) {
                Toast.makeText(SurfaceDemoActivity.this,
                        "click pdf(" + x + "," + y + ")", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onScaleBegin(ScaleGestureDetector detector) {
            Log.i(TAG, "onScaleBegin");
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            Log.i(TAG, "onScaleEnd");
        }

        @Override
        public void onSurfaceCreated() {
            Log.i(TAG, "onSurfaceCreated");
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSurfaceChanged(int width, int height) {
            Log.i(TAG, "onSurfaceChanged " + width + "," + height);
        }

        @Override
        public void onSurfaceDestroyed() {
            Log.i(TAG, "onSurfaceDestroyed");
        }
    }
}

package com.ober.opdf.surface;

import android.view.ScaleGestureDetector;

/**
 * Created by ober on 2020/8/6.
 */
public interface OPdfCallback {

    void onPdfPageOpen(int pageWidth, int pageHeight);

    void onPdfPreviewReady();

    void onPdfClick(int x, int y, int pdfWidth, int pdfHeight);

    void onScaleBegin(ScaleGestureDetector detector);

    void onScaleEnd(ScaleGestureDetector detector);

    void onSurfaceCreated();

    void onSurfaceChanged(int width, int height);

    void onSurfaceDestroyed();
}

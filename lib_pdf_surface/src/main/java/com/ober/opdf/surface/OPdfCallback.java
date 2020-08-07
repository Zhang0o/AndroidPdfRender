package com.ober.opdf.surface;

import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;

/**
 * Handle some callbacks of OPdf Renderer
 *
 * All of functions called in Main-Thread, so do not block them too long.
 *
 * It can be set to OPdfSurfaceRenderer optionally.
 * @see com.ober.opdf.surface.render.OPdfSurfaceRenderer#setOPdfCallback(OPdfCallback)
 *
 * Created by ober on 2020/8/6.
 */
public interface OPdfCallback {

    /**
     * Called when pdf page opened success
     * @param pageWidth pdf page width
     * @param pageHeight pdf page height
     */
    void onPdfPageOpen(int pageWidth, int pageHeight);

    /**
     * Called when pdf page preview bitmap renderer, so content is available to see now
     */
    void onPdfPreviewReady();

    /**
     * Called when click on pdf
     * (x,y) is base on pdf coordinate.
     * example:
     *      x<0 means on the left of pdf.
     *      y>pdfHeight means below the pdf.
     *
     * @param x click position X (pdf relative coordinates)
     * @param y click position Y (pdf relative coordinates)
     * @param pdfWidth pdf page width
     * @param pdfHeight pdf page height
     */
    void onPdfClick(int x, int y, int pdfWidth, int pdfHeight);

    /**
     * Called when scale gesture start
     *
     * @param detector detector that own by renderer, other gesture params can be got from detector
     */
    void onScaleBegin(ScaleGestureDetector detector);

    /**
     * Called when scale gesture end
     *
     * @param detector detector that own by renderer, other gesture params can be got from detector
     */
    void onScaleEnd(ScaleGestureDetector detector);

    /**
     * Called inside {@link android.view.SurfaceHolder.Callback2#surfaceCreated(SurfaceHolder)}
     */
    void onSurfaceCreated();

    /**
     * Called inside {@link SurfaceHolder.Callback2#surfaceChanged(SurfaceHolder, int, int, int)}
     * @param width surface holder width
     * @param height surface holder height
     */
    void onSurfaceChanged(int width, int height);

    /**
     * Called inside {@link SurfaceHolder.Callback2#surfaceDestroyed(SurfaceHolder)}
     */
    void onSurfaceDestroyed();
}

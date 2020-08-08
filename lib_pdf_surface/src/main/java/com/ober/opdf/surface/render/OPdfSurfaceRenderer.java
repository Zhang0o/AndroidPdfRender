package com.ober.opdf.surface.render;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.MainThread;

import com.ober.opdf.surface.OPdfCallback;
import com.ober.opdf.surface.OPdfRenderConfig;
import com.ober.opdf.surface.base.BitmapHolder;
import com.ober.opdf.surface.render.events.SPdfFrameRenderedEv;
import com.ober.opdf.surface.render.events.SPdfMsgDef;
import com.ober.opdf.surface.render.events.SPdfPageOpenEv;

import java.io.File;

/**
 * Core Renderer Logic
 *
 * Created by ober on 2020/7/31.
 */
public class OPdfSurfaceRenderer implements ViewGestureHelper.GestureHandler {
    private static final String TAG = "ORenderer";

    private final SurfaceView surfaceView;

    private final BitmapHolder previewTextureHolder;
    private final BitmapHolder frameTextureHolder;

    private final Handler mHandler;

    private boolean optimizeTransformMode;

    private int pdfWidth;
    private int pdfHeight;

    private boolean isDestroyed;
    private SDecoder sDecoder;

    protected final Matrix transform;

    protected final Paint mBmpPaint;

    private SDecoder.FrameDecodeCall mLastDecodeFrameTransform;

    private final Rect tempRect = new Rect();
    private final RectF tempRectF = new RectF();
    private final float[] tempF9 = new float[9];
    private SurfaceHolder.Callback2 innerSurfaceCallback;

    private OPdfCallback outerListener;

    public OPdfSurfaceRenderer(SurfaceView surfaceView, final File pdfFile, final int page) {
        this.surfaceView = surfaceView;
        this.mHandler = new InnerHandler();
        this.previewTextureHolder = new BitmapHolder();
        this.frameTextureHolder = new BitmapHolder();
        this.sDecoder = new SDecoder(mHandler);
        this.isDestroyed = false;
        this.mBmpPaint = new Paint();
        this.mBmpPaint.setAntiAlias(true);
        this.mBmpPaint.setDither(true);
        this.transform = new Matrix();
        innerSurfaceCallback = new SurfaceHolder.Callback2() {
            @Override
            public void surfaceRedrawNeeded(SurfaceHolder holder) {

            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if(outerListener != null) {
                    outerListener.onSurfaceCreated();
                }
                initialize(pdfFile, page);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if(outerListener != null) {
                    outerListener.onSurfaceChanged(width, height);
                }
                if(width != 0 && height != 0) {
                    createFrameBitmapIfNeed(width, height);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if(outerListener != null) {
                    outerListener.onSurfaceDestroyed();
                }
                destroy();
            }
        };
        surfaceView.getHolder().addCallback(innerSurfaceCallback);
    }

    /**
     * Set a callback to handle some events
     *
     * @param cb callback nullable
     */
    public void setOPdfCallback(OPdfCallback cb) {
        outerListener = cb;
    }

    /**
     * Set true to optimize drawing result when transforming.
     *
     * Effect function {@link #onDrawTransforming(Canvas)}.
     * If true is set, last frame-texture will be drawn on top of preview-texture,
     * so that view will be higher resolution in the last rendered rect.
     * It may lose some performance because one more bitmap may be drawn when transforming
     *
     * @param optimizeTransformMode whether optimize drawing when transforming
     */
    public void setOptimizeTransformDrawing(boolean optimizeTransformMode) {
        this.optimizeTransformMode = optimizeTransformMode;
    }

    /**
     * Can be invoke in main thread only
     * @return if it is destroyed
     */
    public final boolean isDestroyed() {
        return isDestroyed;
    }

    /**
     * Called when initialize decoding
     *
     * Allocate resources and start worker thread
     *
     * @param pdfFile target pdf file
     * @param page target pdf page
     */
    protected void initialize(File pdfFile, int page) {
        isDestroyed = false;
        sDecoder.init(pdfFile, page, this);
    }

    /**
     * Called when surface destroyed
     */
    protected void destroy() {
        isDestroyed = true;
        sDecoder.destroy();
    }

    /**
     * Draw after transform end
     * @param canvas surface holder locked canvas
     */
    protected void onDrawTransformEnd(Canvas canvas) {
        Bitmap bmp = frameTextureHolder.bitmap;
        if(bmp == null) {
            return;
        }

        canvas.drawColor(OPdfRenderConfig.getCanvasBackgroundColor());

        canvas.drawBitmap(bmp, 0, 0, mBmpPaint);
    }

    /**
     * Draw while transforming
     * @param canvas surface holder locked canvas
     */
    protected void onDrawTransforming(Canvas canvas) {
        Bitmap bmp = previewTextureHolder.bitmap;
        if(bmp == null) {
            return;
        }

        canvas.drawColor(OPdfRenderConfig.getCanvasBackgroundColor());

        canvas.setMatrix(transform);
        canvas.drawBitmap(bmp, 0, 0, mBmpPaint);

        //optimize drawing on scale, use frame texture to override preview texture;
        Bitmap frameTexture = frameTextureHolder.bitmap;
        if(optimizeTransformMode && frameTexture != null && mLastDecodeFrameTransform != null) {
            float x = mLastDecodeFrameTransform.transformX;
            float y = mLastDecodeFrameTransform.transformY;
            float scale = mLastDecodeFrameTransform.scale;
            int w = frameTexture.getWidth();
            int h = frameTexture.getHeight();

            Rect frameRect = tempRect;
            frameRect.set(0, 0, w, h);

            float[] ff = tempF9;

            transform.getValues(ff);
            float tranx = ff[Matrix.MTRANS_X];
            float trany = ff[Matrix.MTRANS_Y];
            float s = ff[Matrix.MSCALE_X];

            int framePdfLeft = (int) -x;
            int framePdfTop = (int) -y;
            int framePdfRight = (int) (-x + pdfWidth * scale);
            int framePdfBottom = (int) (-y + pdfHeight * scale);

            boolean intersect = frameRect.intersect(framePdfLeft, framePdfTop, framePdfRight, framePdfBottom);

            if(intersect) {
                canvas.setMatrix(null);

                final float ds = s / scale;

                final float l;
                final float t;

                if(x < 0) {
                    l = tranx;
                } else {
                    l = tranx + x * ds;
                }

                if(y < 0) {
                    t = trany;
                } else {
                    t = trany + y * ds;
                }

                float r = l + frameRect.width() * ds;
                float b = t + frameRect.height() * ds;

                RectF dstRect = tempRectF;
                dstRect.set(l, t, r, b);
                if(!frameRect.isEmpty() && !dstRect.isEmpty()) {
                    canvas.drawBitmap(frameTexture, frameRect, dstRect, mBmpPaint);
                }
            }
        }
    }

    /**
     * Init transform.
     * Current implementation is to make pdf center-inside
     */
    protected void initializeTransform() {
        //fit transform to center inside
        Rect rect = surfaceView.getHolder().getSurfaceFrame();
        int surfaceWidth = rect.width();
        int surfaceHeight = rect.height();
        float wRatio = (float) surfaceWidth / (float) pdfWidth;
        float hRatio = (float) surfaceHeight / (float) pdfHeight;

        if(wRatio >= 1.0f && hRatio >= 1.0f) {
            transform.setTranslate(-(surfaceWidth - pdfWidth), -(surfaceHeight - pdfHeight));
        } else {
            if(wRatio > hRatio) {
                //fit height
                float scale = hRatio;
                transform.setScale(scale, scale);
                transform.postTranslate((surfaceWidth -  pdfWidth * scale) / 2.0f, 0);
            } else {
                //fit width
                float scale = wRatio;
                transform.setScale(scale, scale);
                transform.postTranslate(0, (surfaceHeight -  pdfHeight * scale) / 2.0f);
            }
        }
    }

    @MainThread
    protected void onPdfInitCallback(int pageWidth, int pageHeight) {
        pdfWidth = pageWidth;
        pdfHeight = pageHeight;
        if(outerListener != null) {
            outerListener.onPdfPageOpen(pageWidth, pageHeight);
        }
    }

    @MainThread
    protected void onFullPageDecodeCallback() {
        if(isDestroyed) {
            return;
        }
        initializeTransform();
        dispatchDrawOnTransforming();
        if(outerListener != null) {
            outerListener.onPdfPreviewReady();
        }
    }

    @MainThread
    protected void onFrameDecodeCallback(SDecoder.FrameDecodeCall decodeCall) {
        if(isDestroyed) {
            return;
        }
        dispatchDrawOnTransformEnd(decodeCall);
    }

    private void dispatchDrawOnTransformEnd(SDecoder.FrameDecodeCall decodeCall) {
        Canvas canvas = lockCanvasCompat();

        onDrawTransformEnd(canvas);

        surfaceView.getHolder().unlockCanvasAndPost(canvas);
        mLastDecodeFrameTransform = decodeCall;
    }

    private void dispatchDrawOnTransforming() {
        Canvas canvas = lockCanvasCompat();

        onDrawTransforming(canvas);

        surfaceView.getHolder().unlockCanvasAndPost(canvas);
    }

    protected final BitmapHolder getPreviewTextureHolder() {
        synchronized (OPdfSurfaceRenderer.class) {
            return previewTextureHolder;
        }
    }

    protected final BitmapHolder getFrameTextureHolder() {
        synchronized (OPdfSurfaceRenderer.class) {
            return frameTextureHolder;
        }
    }

    @Override
    public void onSingleTapUp(MotionEvent event) {
        if(outerListener != null) {
            float[] ff = tempF9;
            transform.getValues(ff);
            float tranx = ff[Matrix.MTRANS_X];
            float trany = ff[Matrix.MTRANS_Y];
            float s = ff[Matrix.MSCALE_X];

            int x = (int) ((event.getX() - tranx) / s);
            int y = (int) ((event.getY() - trany) / s);

            outerListener.onPdfClick(x, y, pdfWidth, pdfHeight);
        }
    }

    @Override
    public void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        transform.postTranslate(-distanceX, -distanceY);
        dispatchDrawOnTransforming();
    }

    @Override
    public void onScale(ScaleGestureDetector detector) {
        float preSpan = detector.getPreviousSpan();
        float curSpan = detector.getCurrentSpan();
        float dscale = curSpan/ preSpan;

        float focusX = detector.getFocusX();
        float focusY = detector.getFocusY();

        transform.postScale(dscale, dscale, focusX, focusY);

        dispatchDrawOnTransforming();
    }

    @Override
    public void onScaleBegin(ScaleGestureDetector detector) {
        if(outerListener != null) {
            outerListener.onScaleBegin(detector);
        }
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        if(outerListener != null) {
            outerListener.onScaleEnd(detector);
        }
    }

    @Override
    public void onPointerUp(MotionEvent event) {
        sDecoder.clearTasks();

        float[] f9 = new float[9];
        transform.getValues(f9);
        SDecoder.FrameDecodeCall decodeCall = new SDecoder.FrameDecodeCall();
        decodeCall.transformX = -f9[2];
        decodeCall.transformY = -f9[5];
        decodeCall.scale = f9[0];
        sDecoder.queueTask(decodeCall);

    }

    private void createFrameBitmapIfNeed(int w, int h) {
        if(frameTextureHolder.bitmap == null
                || frameTextureHolder.bitmap.getWidth() != w
                || frameTextureHolder.bitmap.getHeight() != h) {
            frameTextureHolder.bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        }
    }

    private Canvas lockCanvasCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return surfaceView.getHolder().lockHardwareCanvas();
        } else {
            return surfaceView.getHolder().lockCanvas();
        }
    }

    @SuppressLint("HandlerLeak")
    private class InnerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(isDestroyed) {
                return;
            }

            switch (msg.what) {
                case SPdfMsgDef.RENDER_START:
                    Log.d(TAG, "renderer start");
                    break;
                case SPdfMsgDef.FULL_TEXTURE_RENDERED: {
                    Log.d(TAG, "full texture renderer");
                    onFullPageDecodeCallback();
                    break;
                }
                case SPdfMsgDef.FRAME_TEXTURE_RENDERED: {
                    SPdfFrameRenderedEv ev = (SPdfFrameRenderedEv) msg.obj;
                    SDecoder.FrameDecodeCall decodeCall = ev.decodeCall;
                    Log.d(TAG, "frame texture renderer " + decodeCall.toString());
                    onFrameDecodeCallback(decodeCall);
                    break;
                }
                case SPdfMsgDef.PAGE_OPEN: {
                    SPdfPageOpenEv ev = (SPdfPageOpenEv) msg.obj;
                    Log.d(TAG, "pdf page opened : " + ev.pageWidth + "x" + ev.pageHeight);
                    onPdfInitCallback(ev.pageWidth, ev.pageHeight);
                    break;
                }
                case SPdfMsgDef.RENDER_EXIT:
                    Log.d(TAG, "renderer exit");
                    break;
            }
        }
    }

}

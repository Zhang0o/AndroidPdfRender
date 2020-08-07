package com.ober.opdf.surface.render;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;

import com.ober.opdf.surface.OPdfRenderConfig;
import com.ober.opdf.surface.base.BitmapHolder;
import com.ober.opdf.surface.render.events.SPdfExitEv;
import com.ober.opdf.surface.render.events.SPdfFrameRenderedEv;
import com.ober.opdf.surface.render.events.SPdfMsgDef;
import com.ober.opdf.surface.render.events.SPdfPageOpenEv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by ober on 2020/7/31.
 */
@TargetApi(21)
public class SDecoder {

    public static class FrameDecodeCall {
        public float transformX;
        public float transformY;
        public float scale;

        @Override
        public String toString() {
            return "FrameDecodeCall{" +
                    "transformX=" + transformX +
                    ", transformY=" + transformY +
                    ", scale=" + scale +
                    '}';
        }
    }

    private final LinkedBlockingQueue<FrameDecodeCall> mTaskQueue;

    private final Handler handler;

    private Thread mThread;
    private DecodeLoop mDecodeLoop;

    SDecoder(Handler handler) {
        this.handler = handler;
        this.mTaskQueue = new LinkedBlockingQueue<>();
    }

    void init(File pdfFile, int page, OPdfSurfaceRenderer renderer) {
        mDecodeLoop = new DecodeLoop(pdfFile, page, renderer, mTaskQueue, handler);
        mThread = new Thread(mDecodeLoop);
        mThread.start();
    }

    void destroy() {
        mDecodeLoop.runningFlag = false;
        mThread.interrupt();
        handler.removeCallbacksAndMessages(null);
    }

    void clearTasks() {
        mTaskQueue.clear();
    }

    void queueTask(FrameDecodeCall decodeCall) {
        mTaskQueue.offer(decodeCall);
    }

    private static class DecodeLoop implements Runnable {

        private final File pdfFile;
        private final int pageIdx;
        private final LinkedBlockingQueue<FrameDecodeCall> taskQueue;
        private final WeakReference<OPdfSurfaceRenderer> surfaceRendererRef;
        private final Handler handler;

        private volatile boolean runningFlag;
        private int exitValue;
        private String errorMsg;

        DecodeLoop(File pdfFile,
                   int page,
                   OPdfSurfaceRenderer surfaceRenderer,
                   LinkedBlockingQueue<FrameDecodeCall> taskQueue,
                   Handler handler) {
            this.pdfFile = pdfFile;
            this.pageIdx = page;
            this.taskQueue = taskQueue;
            this.surfaceRendererRef = new WeakReference<>(surfaceRenderer);
            this.runningFlag = true;
            this.handler = handler;
            this.exitValue = 0;
        }

        public int getExitValue() {
            return exitValue;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        @Override
        public void run() {

            handler.sendEmptyMessage(SPdfMsgDef.RENDER_START);

            ParcelFileDescriptor pFd;
            try {
                pFd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                errorMsg = "open pdf file error";
                exitValue = -1;
                closeEveryThing(null, null, null);
                sendExitMsg();
                return;
            }
            PdfRenderer pdfRenderer;

            try {
                pdfRenderer = new PdfRenderer(pFd);
            } catch (IOException e) {
                e.printStackTrace();
                errorMsg = "create PdfRenderer error";
                exitValue = -2;
                closeEveryThing(pFd, null, null);
                sendExitMsg();
                return;
            }

            PdfRenderer.Page page = pdfRenderer.openPage(pageIdx);

            final int pageWidth = page.getWidth();
            final int pageHeight = page.getHeight();

            final Matrix matrix = new Matrix();

            {
                final OPdfSurfaceRenderer surfaceRenderer = surfaceRendererRef.get();
                if (surfaceRenderer == null) {
                    errorMsg = "SurfaceRenderer is died";
                    exitValue = -3;
                    closeEveryThing(pFd, page, pdfRenderer);
                    sendExitMsg();
                    return;
                }

                Message msg = handler.obtainMessage();
                msg.what = SPdfMsgDef.PAGE_OPEN;
                msg.obj = new SPdfPageOpenEv(pageWidth, pageHeight);
                handler.sendMessage(msg);

                BitmapHolder fullTextureHolder = surfaceRenderer.getPreviewTextureHolder();

                OPdfRenderConfig.getFullTextureBmpGenerator().generate(
                        fullTextureHolder,
                        pageWidth, pageHeight);

                Bitmap fullTexture = fullTextureHolder.bitmap;

                if(fullTexture == null) {
                    throw new RuntimeException("FullTextureBmpGenerator Bad Implementation");
                }

                float sx = (float) fullTexture.getWidth() / (float) pageWidth;
                float sy = (float) fullTexture.getHeight() / (float) pageHeight;

                matrix.reset();
                matrix.setScale(sx, sy);

                fullTexture.eraseColor(OPdfRenderConfig.getCanvasBackgroundColor());
                page.render(fullTexture, null,
                        matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                handler.sendEmptyMessage(SPdfMsgDef.FULL_TEXTURE_RENDERED);
            }

            FrameDecodeCall decodeCall;

            while (runningFlag) {
                try {
                    decodeCall = taskQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }

                OPdfSurfaceRenderer surfaceRenderer = surfaceRendererRef.get();
                if (surfaceRenderer == null) {
                    errorMsg = "SurfaceRenderer is died";
                    exitValue = -4;
                    closeEveryThing(pFd, page, pdfRenderer);
                    sendExitMsg();
                    return;
                }

                BitmapHolder frameBitmapHolder = surfaceRenderer.getFrameTextureHolder();

                int textureWidth = frameBitmapHolder.bitmap.getWidth();
                int textureHeight = frameBitmapHolder.bitmap.getHeight();

                matrix.reset();

                matrix.setScale(decodeCall.scale, decodeCall.scale);
                matrix.postTranslate(-decodeCall.transformX, -decodeCall.transformY);

                frameBitmapHolder.bitmap.eraseColor(OPdfRenderConfig.getCanvasBackgroundColor());

                page.render(frameBitmapHolder.bitmap, null,
                        matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                Message msg = handler.obtainMessage();
                msg.what = SPdfMsgDef.FRAME_TEXTURE_RENDERED;
                msg.obj = new SPdfFrameRenderedEv(decodeCall);
                handler.sendMessage(msg);
            }

            exitValue = 1;
            errorMsg = "ok";
            closeEveryThing(pFd, page, pdfRenderer);
            sendExitMsg();
        }

        private void closeEveryThing(ParcelFileDescriptor fd, PdfRenderer.Page page, PdfRenderer renderer) {
            if(fd != null) {
                try {
                    fd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(page != null) {
                try {
                    page.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(renderer != null) {
                try {
                    renderer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            runningFlag = false;
        }

        private void sendExitMsg() {
            Message msg = handler.obtainMessage();
            msg.obj = new SPdfExitEv(exitValue);
            msg.what = SPdfMsgDef.RENDER_EXIT;
            handler.sendMessage(msg);
        }
    }




}

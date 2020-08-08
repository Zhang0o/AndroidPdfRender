package com.ober.opdf.surface;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.ober.opdf.surface.base.BitmapHolder;
import com.ober.opdf.surface.render.FullTextureBmpGenerator;


/**
 * Global PDF Render Configuration Entry
 *
 * Created by ober on 2020/8/2.
 */
public class OPdfRenderConfig {

    private static FullTextureBmpGenerator fullTextureBmpGenerator;

    private static int canvasBackgroundColor = Color.WHITE;

    /**
     * Set custom Preview FullTextureBitmapGenerator, if not set, default{@link DefFullTextureBmpGenerator} will be supplied
     * @param fullTextureBmpGenerator custom fullTextureBmpGenerator to be set, null is ok
     */
    public static void setFullTextureBmpGenerator(FullTextureBmpGenerator fullTextureBmpGenerator) {
        OPdfRenderConfig.fullTextureBmpGenerator = fullTextureBmpGenerator;
    }

    /**
     * Get custom FullTextureBmpGenerator or default one , always not null.
     *
     * @return custom FullTextureBmpGenerator or default
     */
    public static FullTextureBmpGenerator getFullTextureBmpGenerator() {
        if(fullTextureBmpGenerator == null) {
            fullTextureBmpGenerator = new DefFullTextureBmpGenerator();
        }
        return fullTextureBmpGenerator;
    }

    /**
     * Set canvas background color that will be drawn under pdf page
     * @param canvasBackgroundColor ARGB int color
     */
    public static void setCanvasBackgroundColor(int canvasBackgroundColor) {
        OPdfRenderConfig.canvasBackgroundColor = canvasBackgroundColor;
    }

    /**
     * Get canvas background color
     * @return ARGB int color
     */
    public static int getCanvasBackgroundColor() {
        return canvasBackgroundColor;
    }

    private static class DefFullTextureBmpGenerator implements FullTextureBmpGenerator {

        @Override
        public void generate(BitmapHolder bitmapHolder, int pdfWidth, int pdfHeight) {
            int intScale = Math.max(pdfWidth, pdfHeight) / 2048;
            if(intScale < 0) {
                intScale = 1;
            }
            int bmpW = pdfWidth / intScale;
            int bmpH = pdfHeight / intScale;
            bitmapHolder.bitmap = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888);
        }
    }

}

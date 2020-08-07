package com.ober.opdf.surface;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.ober.opdf.surface.base.BitmapHolder;
import com.ober.opdf.surface.render.FullTextureBmpGenerator;


/**
 * Created by ober on 2020/8/2.
 */
public class OPdfRenderConfig {

    private static FullTextureBmpGenerator fullTextureBmpGenerator;

    public static void setFullTextureBmpGenerator(FullTextureBmpGenerator fullTextureBmpGenerator) {
        OPdfRenderConfig.fullTextureBmpGenerator = fullTextureBmpGenerator;
    }

    public static FullTextureBmpGenerator getFullTextureBmpGenerator() {
        if(fullTextureBmpGenerator == null) {
            fullTextureBmpGenerator = new DefFullTextureBmpGenerator();
        }
        return fullTextureBmpGenerator;
    }

    public static int getCanvasBackgroundColor() {
        return Color.WHITE;
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

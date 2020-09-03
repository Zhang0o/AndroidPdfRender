package com.ober.opdf.surface.render;


import com.ober.opdf.surface.base.BitmapHolder;

/**
 * An generator used when PDF renderer create bitmap for pdf page preview.<br/>
 * It will called only once if layout is stable. <br/>
 * It called in Pdf render thread so UI thread will not be block.<br/>
 *
 * Bitmap in bitmapHolder should be created with provided actual pdf width and height.
 * If bitmap is not created in generator, exception will be thrown by render logic.
 * Bitmap format ARGB_8888 and RGB_565 is supported now.
 * For example. Normally use a scale to create preview bitmap like:
 *      bitmapHolder.bitmap = Bitmap.createBitmap(pdfWidth * 0.5f, pdfHeight * 0.5f, Config.ARGB_8888)
 *
 * Created by ober on 2020/8/2.
 */
public interface FullTextureBmpGenerator {

    void generate(BitmapHolder bitmapHolder, int pdfWidth, int pdfHeight);

}

package com.ober.opdf.surface.render;


import com.ober.opdf.surface.base.BitmapHolder;

/**
 * Created by ober on 2020/8/2.
 */
public interface FullTextureBmpGenerator {

    void generate(BitmapHolder bitmapHolder, int pdfWidth, int pdfHeight);

}

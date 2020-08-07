package com.ober.opdf.surface.render.events;

/**
 * Created by ober on 2020/8/2.
 */
public class SPdfPageOpenEv {

    public final int pageWidth;
    public final int pageHeight;

    public SPdfPageOpenEv(int pageWidth, int pageHeight) {
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
    }

}

package com.ober.opdf.surface.render.events;


import com.ober.opdf.surface.render.SDecoder;

/**
 * Created by ober on 2020/8/2.
 */
public class SPdfFrameRenderedEv {

    public final SDecoder.FrameDecodeCall decodeCall;

    public SPdfFrameRenderedEv(SDecoder.FrameDecodeCall decodeCall) {
        this.decodeCall = decodeCall;
    }

}

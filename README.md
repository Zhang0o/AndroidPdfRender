# OPdf surface-renderer

Android PDF 查看器实现.

目录说明:
/app 是surface-renderer 演示 Demo, 依赖lib_pdf_surface
/lib_pdf_surface 模块下是"surface-renderer"的实现

"surface-renderer" 已经发布到jcenter 
最新版本
implementation 'com.ober.opdf:lib_pdf_surface:0.0.2'

基于SurfaceView的单页PDF查看，可自由缩放和平移。
使用方便，尤其适合展示复杂的PDF页面。

        //创建renderer 
        //create renderer object
        mRenderer = new OPdfSurfaceRenderer(surfaceView, pdfFile, 0);
        
        //在缩放和拖动时提高清晰度
        //improve drawing result when transforming, it will cause some performance lost
        mRenderer.setOptimizeTransformDrawing(true);
        
        //绑定surfaceView 和 renderer， 初始化完成
        //setup and it will initialize. every thing is ready.
        ViewGestureHelper.bindView(surfaceView, mRenderer);
        
        //可选，添加回调
        //optionally set callback
        mRenderer.setOPdfCallback(new OPdfCallbackImpl());
        
It is an single PDF page viewer implementation base on SurfaceView.
Free scale and translate is supported.
Easy to use and show complicated PDF very well.

*API level lower than 21 is not support now because of SDK-built-in class PdfRenderer.

*The GLSurfaceView implementation will coming later and API level limitation will be resolved.

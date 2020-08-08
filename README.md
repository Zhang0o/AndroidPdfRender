# Android Pdf surface-renderer

## Android PDF 查看器实现

<img src="https://github.com/Zhang0o/AndroidPdfRender/blob/master/demo-min.gif" width="221" height="480"/>

### 工程目录说明:

         /app 是surface-renderer演示Demo(依赖lib_pdf_surface)
         /lib_pdf_surface 是"surface-renderer"的实现
### include to project

"surface-renderer" 0.0.4 已经发布到jcenter 

        //use in gradle:
        implementation 'com.ober.opdf:lib_pdf_surface:0.0.4'

### 使用简介

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


ChangeLog
-----------
### v0.0.1
* Initialize surface-renderer and build a simple demo

### v0.0.2
* Write some java document
* Reformat code and add upload gradle plugin

### v0.0.3
* Initialize pdf center inside view
* Add some document
* Remove androidx.appcompat dependency 

### v0.0.4
* bug fix

Todo
-----
* API level lower than 21 is not support now because of SDK-built-in class PdfRenderer.

* The GLSurfaceView implementation will coming later and API level limitation will be resolved.

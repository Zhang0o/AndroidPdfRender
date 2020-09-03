# Android Pdf surface-renderer
### 简介
* 旨在高效稳定的展示单页PDF
* 良好的兼容性
* 一定的可扩展性

<img src="https://github.com/Zhang0o/AndroidPdfRender/blob/master/demo-min.gif" width="221" height="480"/>


### 工程目录说明:

         /app 是surface-renderer演示Demo(依赖lib_pdf_surface)
         /lib_pdf_surface 是"surface-renderer"的实现
### Get Started

"surface-renderer" 0.1.0 已经发布到jcenter 

        //use in gradle:
        implementation 'com.ober.opdf:lib_pdf_surface:0.1.0'

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

### 关联项目
https://github.com/Zhang0o/AndroidPdfiumCompat

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
* Bugfix

### v0.1.0
* Integrate pdfium library to support api version lower than 21.

Todo
-----
* API level lower than 21 is not support now because of SDK-built-in class PdfRenderer.

* The GLSurfaceView implementation will coming later and API level limitation will be resolved.

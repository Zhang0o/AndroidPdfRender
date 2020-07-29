#include <jni.h>
#include <string>
#include <sys/stat.h>
#include <unistd.h>
#include <android/bitmap.h>

#include "logger.h"

#include "fpdfview.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_ober_pdft_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


inline long getFileSize(int fd) {
    struct stat file_state;
    if(fstat(fd, &file_state) >= 0) {
        return (long) file_state.st_size;
    } else {
        LOGE("Error getting file size");
        return 0;
    }
}

static int getBlock(void* params, unsigned long position,
        unsigned char* outBuffer, unsigned long size) {
    const int fd = (intptr_t) params;
    const int readCount = pread(fd, outBuffer, size, position);
    if(readCount < 0) {
        LOGE("Cannot read from file descriptor, Error:%d", errno);
        return 0;
    }
    return 1;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_ober_pdft_PdfTest_decodePdf(JNIEnv *env, jclass clazz, jint fd, jobject bmp) {

    LOGD("decodePdf");

    FPDF_LIBRARY_CONFIG config;
    config.version = 2;
    config.m_pIsolate = 0;
    config.m_pPlatform = 0;
    config.m_v8EmbedderSlot = 0;
    config.m_pUserFontPaths = 0;

    FPDF_InitLibraryWithConfig(&config);
    LOGD("library initialized");


    long fileLength = getFileSize(fd);

    if(fileLength <= 0) {
        return -1;
    }

    FPDF_FILEACCESS loader;
    loader.m_FileLen = fileLength;
    loader.m_Param = (void*)((intptr_t)fd);
    loader.m_GetBlock = &getBlock;

    FPDF_DOCUMENT document = FPDF_LoadCustomDocument(&loader, NULL);
    if(!document) {
        const unsigned long errorNum = FPDF_GetLastError();
        LOGE("load document error %ld", errorNum);
        return -1;
    }
    LOGD("document loaded");


    FPDF_PAGE page = FPDF_LoadPage(document, 0);
    if(!page) {
        const unsigned long errorNum = FPDF_GetLastError();
        LOGE("load page error %ld", errorNum);
        FPDF_CloseDocument(document);
        return -1;
    }
    LOGD("page loaded");

    AndroidBitmapInfo bitmapInfo;
    AndroidBitmap_getInfo(env, bmp, &bitmapInfo);
    void* addr;
    AndroidBitmap_lockPixels(env, bmp, &addr);

    FPDF_BITMAP pdfBitmap = FPDFBitmap_CreateEx(bitmapInfo.width, bitmapInfo.height,
            FPDFBitmap_BGRA, addr, bitmapInfo.stride);
    LOGD("PdfBitmap created");

//    FPDFBitmap_FillRect(pdfBitmap, 0, 0,
//            bitmapInfo.width, bitmapInfo.height, 0x848484ff);
//    LOGD("PdfBitmap rect filled");

    int flags = FPDF_REVERSE_BYTE_ORDER;

//    FPDF_RenderPageBitmap(pdfBitmap, page,
//            0, 0,
//            bitmapInfo.width, bitmapInfo.height,
//            0, flags);

    FS_MATRIX transform;
    FS_RECTF clip;
    transform.a = 2.0f; transform.b = 0.0f;
    transform.c = 0.0f; transform.d = 2.0f;
    transform.e = 0.0f; transform.f = 0.0f;

    clip.left = 0;
    clip.top = 0;
    clip.bottom = 2048;
    clip.right = 2048;

    FPDF_RenderPageBitmapWithMatrix(pdfBitmap, page, &transform, &clip, flags);

    LOGD("RenderPageBitmap end");

    AndroidBitmap_unlockPixels(env, bmp);
    LOGD("AndroidBitmap unlocked");

    FPDFBitmap_Destroy(pdfBitmap);

    FPDF_ClosePage(page);
    FPDF_CloseDocument(document);

    FPDF_DestroyLibrary();

    LOGD("library destroyed");

    return 1;
}
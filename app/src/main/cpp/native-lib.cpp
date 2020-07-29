#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_ober_pdft_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_ober_pdft_PdfTest_decodePdf(JNIEnv *env, jclass clazz, jint fd, jobject bmp) {



    return 1;
}
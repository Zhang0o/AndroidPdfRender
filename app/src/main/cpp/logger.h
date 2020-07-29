//
// Created by ober on 20-7-29.
//

#ifndef PDFT_LOGGER_H
#define PDFT_LOGGER_H

#include <jni.h>
#include <android/log.h>

#define LOG_TAG "OPdf"

#define LOGI(...)     __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)     __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...)     __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)


#endif //PDFT_LOGGER_H

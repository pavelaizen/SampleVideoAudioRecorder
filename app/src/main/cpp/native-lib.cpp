#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring

JNICALL
Java_sayaaa_rpscience_com_sayaaaa_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

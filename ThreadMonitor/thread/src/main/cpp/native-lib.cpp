#include <jni.h>
#include <string>
#include <android/log.h>
#include "dlopen.h"
#include "inlineHook.h"

void *get_contented_monitor;
void *get_lock_owner_thread_id;
void *thread_create_call_back;

extern "C" JNIEXPORT jstring JNICALL
Java_com_dunn_instrument_thread_NativeThreadMonitor_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {

    // 主要要写个方法获取 thread 的锁对象，通过锁对象获取当前持有的 thread
    // mirror::Object* Monitor::GetContendedMonitor(Thread* thread)
    /*void *so_addr = ndk_dlopen("libart.so");
    // 系统源码，或者公司源码有这样写的
    void *get_contended_monitor_addr = ndk_dlsym(so_addr,"_ZN3art7Monitor19GetContendedMonitorEPNS_6ThreadE");
    long monitorObj = ((long(*)(long))get_contended_monitor_addr)(native_thread);*/

    // monitorObj 再去查被谁持有了
    // uint32_t Monitor::GetLockOwnerThreadId(mirror::Object* obj)
    // < 29
    // _ZN3art7Monitor20GetLockOwnerThreadIdEPNS_6mirror6ObjectE
    // > 29
    // 5/1 , 体谅，开始将线程监控，最终是想做到线程的生命周期监控，所占内存，所占 cpu
    // 已有的方案，微信张绍文，爱奇艺xhook，做的是监控 pthread_create
    // elf inline

    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());

}

const char *get_lock_owner_symbol_name(int level) {
    if (level < 29) {
        return "_ZN3art7Monitor20GetLockOwnerThreadIdEPNS_6mirror6ObjectE";
    } else {
        return "_ZN3art7Monitor20GetLockOwnerThreadIdENS_6ObjPtrINS_6mirror6ObjectEEE";
    }
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_dunn_instrument_thread_NativeThreadMonitor_getContentThreadId(JNIEnv *env, jclass clazz,
                                                                      jlong thread_native_address) {
    // GetContendedMonitor：获取当前线程在竞争的锁，回调函数来用
    if (get_contented_monitor != nullptr && get_lock_owner_thread_id != nullptr) {
        int monitorObj = ((int (*)(long)) get_contented_monitor)(thread_native_address);
        // GetLockOwnerThreadId：当前锁被哪个线程 id 持有了，只有这个方法可以间接的做到
        if (monitorObj != 0) {
            int monitorThreadId = ((int (*)(int)) get_lock_owner_thread_id)(monitorObj);
            return monitorThreadId;
        }
    }
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_dunn_instrument_thread_NativeThreadMonitor_nativeInit(JNIEnv *env, jclass clazz,
                                                              jint sdk_version) {
    // 初始化一下，后面会讲原理， ptl hook ，inline hook 都先不讲原理，先讲使用，先去预先或者复习，
    // elf 文件格式，编译四步骤, ptl got 表
    ndk_init(env);
    void *so_addr = ndk_dlopen("libart.so", RTLD_LAZY);
    if (so_addr == NULL) {
        // LOGD();
        return -1;
    }
    // GetContendedMonitor：获取当前线程在竞争的锁 nm xxx.so
    get_contented_monitor = ndk_dlsym(so_addr, "_ZN3art7Monitor19GetContendedMonitorEPNS_6ThreadE");
    if (get_contented_monitor == NULL) {
        return -2;
    }

    // GetLockOwnerThreadId：当前锁被哪个线程 id 持有了，只有这个方法可以间接的做到
    get_lock_owner_thread_id = ndk_dlsym(so_addr, get_lock_owner_symbol_name(sdk_version));
    if (get_lock_owner_thread_id == NULL) {
        return -3;
    }

    thread_create_call_back = ndk_dlsym(so_addr, "_ZN3art6Thread14CreateCallbackEPv");
    if (thread_create_call_back == NULL) {
        return -4;
    }

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_dunn_instrument_thread_NativeThreadMonitor_getCurrentThreadId(JNIEnv *env, jclass clazz,
                                                                      jlong thread_native_address,
                                                                      jint sdk_version) {
    if (thread_native_address != 0) {
        if (sdk_version > 20) { // 大于 5.0
            int *pInt = reinterpret_cast<int *>(thread_native_address);
            pInt = pInt + 3;
            return *pInt;
        }
    }

    return 0;
}

void* (*old_create_callback)(void*) = NULL;

void* new_create_callback(void* arg) {
    long startTime = time(NULL);
    void* result = old_create_callback(arg);
    long alive_time = time(NULL) - startTime;
    __android_log_print(ANDROID_LOG_ERROR, "TAG", "线程执行完毕，存活时间 -> %ldS",alive_time);
    // 获取 cpu 利用率，获取线程的名字，自己去折腾一下，网上也没有
    // 获取线程的名字？Native 崩溃再复习一下
    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_dunn_instrument_thread_NativeThreadMonitor_monitorThread(JNIEnv *env, jclass clazz) {
    // 第一个参数是 Thread::CreateCallback 的地址
    // 第二个参数是 hook 函数的新地址
    // 第三个参数其实也是 Thread::CreateCallback 的地址

    if (registerInlineHook((uint32_t) thread_create_call_back, (uint32_t) new_create_callback,
                           (uint32_t **) &old_create_callback) != ELE7EN_OK) {
        return -1;
    }
    if (inlineHook((uint32_t) thread_create_call_back) != ELE7EN_OK) {
        return -2;
    }

    return 0;
}
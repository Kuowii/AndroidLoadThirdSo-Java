#include <jni.h>
#include <string>
#include <dlfcn.h>
#include "FileHelper.h"

typedef char*(*fun)(char*,int,char*,int,int*);

fun tsetcfun;
const char* key = "v#key";
int keyLen = 5;
int gPoint = 9;

extern "C" JNIEXPORT jint JNICALL
Java_com_pgw_cppcalltest_MainActivity_SetKeyAndPoint(
        JNIEnv* env,jobject /* this */,
        jstring newKey,jint newPoint
        ) {
    const char* ccsNewKey;
    ccsNewKey = env->GetStringUTFChars(newKey,0);
    env->ReleaseStringUTFChars(newKey, ccsNewKey);

    key = ccsNewKey;
    gPoint = newPoint;
    keyLen = strlen(key);

    //__android_log_print(ANDROID_LOG_INFO,"Main","keyLen=%d",keyLen);

    return 0;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_pgw_cppcalltest_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jint JNICALL
Java_com_pgw_cppcalltest_MainActivity_decryFile(
        JNIEnv* env,jobject /* this */,
        jstring oldFile,jstring newFile,jint point) {

    const char* ccsOldFile;
    const char* ccsNewFile;

    ccsOldFile = env->GetStringUTFChars(oldFile,0);
    ccsNewFile = env->GetStringUTFChars(newFile, 0);

    env->ReleaseStringUTFChars(oldFile, ccsOldFile);
    env->ReleaseStringUTFChars(newFile, ccsNewFile);

    long len = 0;
    int nlen = 0;
    char* data;
    char* loaded = LoadFile(ccsOldFile,&data,&len);

    if(loaded != NULL)
    {
        char* nData = tsetcfun(data+point,len-point,(char*)key,keyLen,&nlen);
        if(nData && nlen>0)
        {
            int r = SaveFile(ccsNewFile,nData,nlen);
            return 0;
        }

    } else
    {
        return 3;
    }

    return 0;
}

extern "C" JNIEXPORT jstring
Java_com_pgw_cppcalltest_MainActivity_JNILoadTest(
        JNIEnv* env,jobject /* this */,
        jstring moduleName,jstring funName) {

    std::string re = "";
    char* chs = new char[4096];
    const char* ccsModuleName;
    const char* ccsFunName;

    ccsModuleName = env->GetStringUTFChars(moduleName,0);
    ccsFunName = env->GetStringUTFChars(funName, 0);

    std::string strModuleName(ccsModuleName);
    std::string strFunName(ccsFunName);

    sprintf(chs,"load %s from %s\n",ccsFunName,ccsModuleName);

    env->ReleaseStringUTFChars(moduleName, ccsModuleName);
    env->ReleaseStringUTFChars(moduleName, ccsFunName);

    re += chs;

    void* handle = dlopen( ccsModuleName, RTLD_LAZY );

    if(!handle)
    {
        re += "Load module fail.\n";
    } else
    {
        re = "Load module success.\n";
    }

    tsetcfun = (fun)dlsym(handle,ccsFunName);

    if(tsetcfun)
    {
        re += " load fun " + strFunName + " success.\n";
    } else
    {
        re += " load " + strFunName + " fail.\n";
    }

    return  env->NewStringUTF(re.c_str());
}

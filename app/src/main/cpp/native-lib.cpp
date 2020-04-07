#include <jni.h>
#include <string>
#include <dlfcn.h>
#include "FileHelper.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_pgw_cppcalltest_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
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

    typedef char*(*fun)(char*,int,char*,int,int*);

    fun tsetcfun = (fun)dlsym(handle,ccsFunName);

    if(tsetcfun)
    {
        re += " load fun " + strFunName + " success.\n";
        long len = 0;
        int nlen = 0;
        char* data;
        char* loaded = LoadFile("/storage/emulated/0/Test.txt",&data,&len);

        const  char* key = "v#key";

        if(loaded != NULL)
        {
            re+=" load file OK\n";
            sprintf(chs,"data[1] is %d len=%d\n",data[1],len);
            re += chs;

            char* nData = tsetcfun(data,len,(char*)key,5,&nlen);
            if(nData && nlen>0)
            {
                re+=" decry file OK\n";
                int r = SaveFile("/storage/emulated/0/Test_de.txt",nData,nlen);
                sprintf(chs,"save count =%d,r = %d\n nData[1] = %d",nlen,r,nData[1]);
                re += chs;
            }

        } else
        {
            re += "load file fail.\n";
        }

    } else
    {
        re += " load " + strFunName + " fail.\n";
    }

    return  env->NewStringUTF(re.c_str());
}

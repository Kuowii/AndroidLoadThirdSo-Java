//
// Created by pWX914214 on 2020/4/7.
//

#include <android/log.h>

#ifndef CPPCALLTEST_JAVA_FILEHELPER_H
#define CPPCALLTEST_JAVA_FILEHELPER_H

char* LoadFile(const char* filePath,char** load,long* len)
{
    FILE* pf = fopen(filePath,"rb");
    if(pf == NULL)
    {
        return NULL;
    }
    fseek(pf,0,SEEK_END);
    long lSize = ftell(pf);
    char* text = (char*)malloc(lSize);
    rewind(pf);
    fread(text,sizeof(char),lSize,pf);

    *len = lSize;
    *load = text;

    __android_log_print(ANDROID_LOG_INFO,"Main","lSize=%d",lSize);
    fclose(pf);

    return text;
}

int SaveFile(const char* filePath,char* source,int len)
{
    FILE* pf = fopen(filePath,"wb");
    if(pf == NULL)
    {
        return -3;
    }

    fwrite(source, sizeof(char),len,pf);
    fclose(pf);

    return 0;
}

#endif //CPPCALLTEST_JAVA_FILEHELPER_H
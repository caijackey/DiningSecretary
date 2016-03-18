
#include <string.h>
#include <jni.h>
#include <android/log.h>
#include "zip.h"

#define LOG_TAG		"chk"
#define LOGI(...)	__android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)	__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define KEY	"classes.dex\r\nSHA1-Digest: "
//#define A2 "mobileapi.95171.cn/javamobile.svc/"
//#define A2 "mainapp.xiaomishu.com/mainapp"
#define A2 "tmainapp.xiaomishu.com/mainapp"
//#define A2 "mobileapi.xiaomishu.com/javamobile.svc"
//#define A2 "tmobileapi.xiaomishu.com/javamobile.svc"
//#define A2 "mobileapi.xiaomishu.com:90/javamobile.svc"

//#define A2 "mapi.xiaomishu.com/javamobile.svc/"
#define SF "META-INF/CERT.SF"
#define SIGN "3gknfh/rP+nf5BK2+SD3kMqiZkQ="
#define A1 "mobile.95171.cn"


jstring
Java_com_fg114_main_service_http_A57HttpApiV3_get(JNIEnv* env,
		jobject thiz, jstring assetpath) {
	// Open zip
		jboolean iscopy;
		const char *mpath = (*env)->GetStringUTFChars(env, assetpath, &iscopy);
		struct zip* apkArchive = zip_open(mpath, 0, NULL);
		(*env)->ReleaseStringUTFChars(env, assetpath, mpath);

		struct zip_stat fstat;
		zip_stat_init(&fstat);

		int numFiles = zip_get_num_files(apkArchive);

		// Read .SF file
		const char *fname = SF;
		struct zip_file* file = zip_fopen(apkArchive, fname, 0);
		if (!file) {
			LOGE("Error opening file", fname);
			return;
		}
		zip_stat(apkArchive, fname, 0, &fstat);
		char *buffer = (char *) malloc(fstat.size + 1);
		buffer[fstat.size] = 0;
		int numBytesRead = zip_fread(file, buffer, fstat.size);

		// Deal with key
		char *key = KEY;
		char *subp = strstr(buffer, key);
		int len = strlen(key);
		char *sub = (char *) malloc(29);
		sub[28] = 0;
		memcpy(sub, subp + len, 28);
		int result = strcmp(sub, SIGN);
		jstring ret;
		if (result == 0) {
			ret = (*env)->NewStringUTF(env, A2);
		} else {
			ret = (*env)->NewStringUTF(env, "");
		}
		free(sub);
		free(buffer);
		zip_fclose(file);
		zip_close(apkArchive);

		//debug, toggle comment when release
		//ret = (*env)->NewStringUTF(env, A2);

		return ret;
}

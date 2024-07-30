#include <jni.h>

int main() {}

JNIEXPORT jstring JNICALL
Java_tech_turso_libsql_Libsql_stringFromJNI(JNIEnv *env, jclass clazz, jstring db_file);

JNIEXPORT jlong JNICALL
Java_tech_turso_libsql_Libsql_nativeOpenEmbeddedReplica(JNIEnv *env, jclass clazz, jstring db_file,
                                                        jstring url, jstring auth_token);

JNIEXPORT jlong JNICALL
Java_tech_turso_libsql_Libsql_nativeOpenRemote(JNIEnv *env, jclass clazz, jstring url,
                                               jstring auth_token);

JNIEXPORT jlong JNICALL
Java_tech_turso_libsql_Libsql_nativeOpenLocal(JNIEnv *env, jclass clazz, jstring db_file);



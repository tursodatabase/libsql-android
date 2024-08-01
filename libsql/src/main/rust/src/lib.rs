#[cfg(target_os = "android")]
#[allow(non_snake_case)]
pub mod android {
    use jni::objects::JString;
    use jni::objects::{JClass, JObject, JObjectArray, JValue};
    use jni::sys::jobjectArray;
    use jni::sys::jstring;
    use jni::sys::{jdouble, jlong};
    use jni::JNIEnv;
    use jni_fn::jni_fn;
    use libsql::Builder;
    use libsql::Connection;
    use libsql::Database;
    use libsql::Rows;
    use std::ptr;

    #[jni_fn("tech.turso.libsql.Libsql")]
    pub fn nativeOpenLocal(env: JNIEnv, _: JClass, db_file: jstring) -> jlong {
        let db_file = unsafe { JString::from_raw(db_file) };
        let db_file: String = unsafe { env.get_string_unchecked(&db_file) }
            .unwrap()
            .into();
        let rt = tokio::runtime::Runtime::new().unwrap();
        let db = rt.block_on(async { Builder::new_local(db_file).build().await });
        (match db {
            Ok(db) => Box::into_raw(Box::new(db)),
            Err(_) => ptr::null(),
        }) as jlong
    }

    #[jni_fn("tech.turso.libsql.Libsql")]
    pub fn nativeOpenRemote(env: JNIEnv, _: JClass, url: jstring, auth_token: jstring) -> jlong {
        let url = unsafe { JString::from_raw(url) };
        let url: String = unsafe { env.get_string_unchecked(&url) }.unwrap().into();
        let auth_token = unsafe { JString::from_raw(auth_token) };
        let auth_token: String = unsafe { env.get_string_unchecked(&auth_token) }
            .unwrap()
            .into();
        let rt = tokio::runtime::Runtime::new().unwrap();
        let db = rt.block_on(async {
            let https = hyper_rustls::HttpsConnectorBuilder::new()
                .with_webpki_roots()
                .https_or_http()
                .enable_http1()
                .build();
            Builder::new_remote(url, auth_token)
                .connector(https)
                .build()
                .await
        });
        (match db {
            Ok(db) => Box::into_raw(Box::new(db)),
            Err(_) => ptr::null(),
        }) as jlong
    }

    #[jni_fn("tech.turso.libsql.Libsql")]
    pub fn nativeOpenEmbeddedReplica(
        env: JNIEnv,
        _: JClass,
        db_file: jstring,
        url: jstring,
        auth_token: jstring,
    ) -> jlong {
        let db_file = unsafe { JString::from_raw(db_file) };
        let db_file: String = unsafe { env.get_string_unchecked(&db_file).unwrap().into() };
        let url = unsafe { JString::from_raw(url) };
        let url: String = unsafe { env.get_string_unchecked(&url) }.unwrap().into();
        let auth_token = unsafe { JString::from_raw(auth_token) };
        let auth_token: String = unsafe { env.get_string_unchecked(&auth_token) }
            .unwrap()
            .into();
        let rt = tokio::runtime::Runtime::new().unwrap();
        let db = rt.block_on(async {
            let https = hyper_rustls::HttpsConnectorBuilder::new()
                .with_webpki_roots()
                .https_or_http()
                .enable_http1()
                .build();
            Builder::new_remote_replica(db_file, url, auth_token)
                .connector(https)
                .build()
                .await
        });
        (match db {
            Ok(db) => Box::into_raw(Box::new(db)),
            Err(_) => ptr::null(),
        }) as jlong
    }

    #[jni_fn("tech.turso.libsql.Database")]
    pub fn nativeConnect(_: JNIEnv, _: JClass, ptr: jlong) -> jlong {
        let db = unsafe { Box::from_raw(ptr as *mut Database) };
        let res = (match db.connect() {
            Ok(conn) => Box::into_raw(Box::new(conn)),
            Err(_) => ptr::null(),
        }) as jlong;
        Box::leak(db);
        res
    }

    #[jni_fn("tech.turso.libsql.Database")]
    pub fn nativeClose(_: JNIEnv, _: JClass, ptr: jlong) {
        drop(unsafe { Box::from_raw(ptr as *mut Database) });
    }

    #[jni_fn("tech.turso.libsql.EmbeddedReplicaDatabase")]
    pub fn nativeSync(_: JNIEnv, _: JClass, ptr: jlong) {
        let db = unsafe { Box::from_raw(ptr as *mut Database) };
        let rt = tokio::runtime::Runtime::new().unwrap();
        let _ = rt.block_on(db.sync()).unwrap();
        Box::leak(db);
    }

    #[jni_fn("tech.turso.libsql.Connection")]
    pub fn nativeExecute(env: JNIEnv, _: JClass, ptr: jlong, sql: jstring) {
        let sql = unsafe { JString::from_raw(sql) };
        let sql: String = unsafe { env.get_string_unchecked(&sql) }.unwrap().into();
        let db = unsafe { Box::from_raw(ptr as *mut Connection) };
        let rt = tokio::runtime::Runtime::new().unwrap();
        rt.block_on(async {
            let _ = db.execute(&sql, ()).await.unwrap();
        });
        Box::leak(db);
    }

    #[jni_fn("tech.turso.libsql.Connection")]
    pub fn nativeQuery(env: JNIEnv, _: JClass, ptr: jlong, sql: jstring) -> jlong {
        let sql = unsafe { JString::from_raw(sql) };
        let sql: String = unsafe { env.get_string_unchecked(&sql) }.unwrap().into();
        let db = unsafe { Box::from_raw(ptr as *mut Connection) };
        let rt = tokio::runtime::Runtime::new().unwrap();
        let res = match rt.block_on(db.query(&sql, ())) {
            Ok(rows) => Box::into_raw(Box::new(rows)),
            Err(_) => ptr::null(),
        } as jlong;
        Box::leak(db);
        res
    }

    #[jni_fn("tech.turso.libsql.Connection")]
    pub fn nativeClose(_: JNIEnv, _: JClass, ptr: jlong) {
        drop(unsafe { Box::from_raw(ptr as *mut Connection) });
    }

    #[jni_fn("tech.turso.libsql.Rows")]
    pub fn nativeNextRow(mut env: JNIEnv, _: JClass, ptr: jlong) -> jobjectArray {
        let mut rows = unsafe { Box::from_raw(ptr as *mut Rows) };
        let rt = tokio::runtime::Runtime::new().unwrap();
        let res = rt
            .block_on(async {
                match rows.next().await {
                    Ok(Some(row)) => {
                        let count = rows.column_count();
                        match env.new_object_array(count, "java/lang/Object", JObject::null()) {
                            Ok(arr) => {
                                for i in 0..count {
                                    let val = row.get_value(i).unwrap();
                                    let obj = match val {
                                        libsql::Value::Null => JObject::null(),
                                        libsql::Value::Integer(v) => env
                                            .new_object(
                                                "java/lang/Long",
                                                "(J)V",
                                                &[JValue::from(v as jlong)],
                                            )
                                            .unwrap(),
                                        libsql::Value::Real(v) => env
                                            .new_object(
                                                "java/lang/Double",
                                                "(D)V",
                                                &[JValue::from(v as jdouble)],
                                            )
                                            .unwrap(),
                                        libsql::Value::Text(v) => env.new_string(v).unwrap().into(),
                                        libsql::Value::Blob(v) => {
                                            env.byte_array_from_slice(&v).unwrap().into()
                                        }
                                    };
                                    env.set_object_array_element(&arr, i, obj).unwrap();
                                }
                                arr
                            }
                            _ => JObjectArray::default(),
                        }
                    }
                    _ => JObjectArray::default(),
                }
            })
            .into_raw();
        Box::leak(rows);
        res
    }

    #[jni_fn("tech.turso.libsql.Rows")]
    pub fn nativeClose(_: JNIEnv, _: JClass, ptr: jlong) {
        drop(unsafe { Box::from_raw(ptr as *mut Rows) });
    }
}

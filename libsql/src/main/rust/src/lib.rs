#![allow(non_snake_case)]

use jni::{
    objects::{JByteArray, JClass, JString},
    sys::{jbyteArray, jlong},
    JNIEnv,
};
use jni_fn::jni_fn;
use libsql::{Builder, Connection, Database, Rows, Transaction};
use std::{mem::ManuallyDrop, ptr};

use lazy_static::lazy_static;
use prost::Message;
use tokio::runtime::Runtime;

pub mod proto {
    include!(concat!(env!("OUT_DIR"), "/proto.rs"));
}

impl From<proto::Value> for libsql::Value {
    fn from(value: proto::Value) -> Self {
        use proto::value::Value as V;

        match value.value {
            Some(V::Integer(i)) => libsql::Value::Integer(i),
            Some(V::Real(r)) => libsql::Value::Real(r),
            Some(V::Text(s)) => libsql::Value::Text(s),
            Some(V::Blob(b)) => libsql::Value::Blob(b),
            Some(V::Null(_)) => libsql::Value::Null,
            None => libsql::Value::Null,
        }
    }
}

impl From<libsql::Value> for proto::Value {
    fn from(value: libsql::Value) -> Self {
        use proto::value::Value as V;

        match value {
            libsql::Value::Integer(i) => proto::Value {
                value: Some(V::Integer(i)),
            },
            libsql::Value::Real(r) => proto::Value {
                value: Some(V::Real(r)),
            },
            libsql::Value::Text(s) => proto::Value {
                value: Some(V::Text(s)),
            },
            libsql::Value::Blob(b) => proto::Value {
                value: Some(V::Blob(b)),
            },
            libsql::Value::Null => proto::Value {
                value: Some(V::Null(proto::value::Null {})),
            },
        }
    }
}

lazy_static! {
    static ref RT: Runtime = Runtime::new().unwrap();
}

fn execute(conn: &Connection, sql: impl AsRef<str>, buf: impl AsRef<[u8]>) -> anyhow::Result<u64> {
    let proto::Parameters { parameters } = proto::Parameters::decode(buf.as_ref())?;

    use proto::{parameters::Parameters, NamedParameters, PositionalParameters};

    Ok(match parameters {
        Some(Parameters::Named(NamedParameters { parameters })) => {
            let parameters: Vec<(String, libsql::Value)> =
                parameters.into_iter().map(|(k, v)| (k, v.into())).collect();

            RT.block_on(conn.execute(sql.as_ref(), parameters))?
        }
        Some(Parameters::Positional(PositionalParameters { parameters })) => {
            let parameters: Vec<libsql::Value> = parameters.into_iter().map(|v| v.into()).collect();

            RT.block_on(conn.execute(sql.as_ref(), parameters))?
        }
        None => RT.block_on(conn.execute(sql.as_ref(), ()))?,
    })
}

fn query(conn: &Connection, sql: impl AsRef<str>, buf: impl AsRef<[u8]>) -> anyhow::Result<Rows> {
    let proto::Parameters { parameters } = proto::Parameters::decode(buf.as_ref())?;

    use proto::{parameters::Parameters, NamedParameters, PositionalParameters};

    Ok(match parameters {
        Some(Parameters::Named(NamedParameters { parameters })) => {
            let parameters: Vec<(String, libsql::Value)> =
                parameters.into_iter().map(|(k, v)| (k, v.into())).collect();

            RT.block_on(conn.query(sql.as_ref(), parameters))?
        }
        Some(Parameters::Positional(PositionalParameters { parameters })) => {
            let parameters: Vec<libsql::Value> = parameters.into_iter().map(|v| v.into()).collect();

            RT.block_on(conn.query(sql.as_ref(), parameters))?
        }
        None => RT.block_on(conn.query(sql.as_ref(), ()))?,
    })
}

#[jni_fn("tech.turso.libsql.Libsql")]
pub fn nativeOpenLocal(mut env: JNIEnv, _: JClass, path: JString) -> jlong {
    match (|| -> anyhow::Result<Database> {
        let path = env.get_string(&path)?;
        Ok(RT.block_on(Builder::new_local(&*path.to_string_lossy()).build())?)
    })() {
        Ok(db) => Box::into_raw(Box::new(db)) as jlong,
        Err(err) => {
            env.throw(err.to_string()).unwrap();
            ptr::null_mut::<Database>() as jlong
        }
    }
}

#[jni_fn("tech.turso.libsql.Libsql")]
pub fn nativeOpenRemote(mut env: JNIEnv, _: JClass, url: JString, auth_token: JString) -> jlong {
    match (|| -> anyhow::Result<Database> {
        let url = env.get_string(&url)?;
        let auth_token = env.get_string(&auth_token)?;

        let connector = hyper_rustls::HttpsConnectorBuilder::new()
            .with_webpki_roots()
            .https_or_http()
            .enable_http1()
            .build();

        Ok(RT.block_on(
            Builder::new_remote(url.into(), auth_token.into())
                .connector(connector)
                .build(),
        )?)
    })() {
        Ok(db) => Box::into_raw(Box::new(db)) as jlong,
        Err(err) => {
            env.throw(err.to_string()).unwrap();
            ptr::null_mut::<Database>() as jlong
        }
    }
}

#[jni_fn("tech.turso.libsql.Libsql")]
pub fn nativeOpenEmbeddedReplica(
    mut env: JNIEnv,
    _: JClass,
    path: JString,
    url: JString,
    auth_token: JString,
) -> jlong {
    match (|| -> anyhow::Result<Database> {
        let path = env.get_string(&path)?;
        let url = env.get_string(&url)?;
        let auth_token = env.get_string(&auth_token)?;

        let connector = hyper_rustls::HttpsConnectorBuilder::new()
            .with_webpki_roots()
            .https_or_http()
            .enable_http1()
            .build();

        let db = RT.block_on(
            Builder::new_remote_replica(&*path.to_string_lossy(), url.into(), auth_token.into())
                .connector(connector)
                .build(),
        );

        Ok(db?)
    })() {
        Ok(db) => Box::into_raw(Box::new(db)) as jlong,
        Err(err) => {
            env.throw(err.to_string()).unwrap();
            ptr::null_mut::<Database>() as jlong
        }
    }
}

#[jni_fn("tech.turso.libsql.Database")]
pub fn nativeConnect(mut env: JNIEnv, _: JClass, db: jlong) -> jlong {
    let db = ManuallyDrop::new(unsafe { Box::from_raw(db as *mut Database) });
    match db.connect() {
        Ok(conn) => Box::into_raw(Box::new(conn)) as jlong,
        Err(err) => {
            env.throw(err.to_string()).unwrap();
            ptr::null_mut::<Connection>() as jlong
        }
    }
}

#[jni_fn("tech.turso.libsql.Database")]
pub fn nativeClose(_: JNIEnv, _: JClass, db: jlong) {
    drop(unsafe { Box::from_raw(db as *mut Database) });
}

#[jni_fn("tech.turso.libsql.EmbeddedReplicaDatabase")]
pub fn nativeSync(mut env: JNIEnv, _: JClass, db: jlong) {
    let db = ManuallyDrop::new(unsafe { Box::from_raw(db as *mut Database) });
    match RT.block_on(db.sync()) {
        Ok(_) => (),
        Err(err) => env.throw(err.to_string()).unwrap(),
    }
}

#[jni_fn("tech.turso.libsql.ConnectionImpl")]
pub fn nativeExecute(mut env: JNIEnv, _: JClass, conn: jlong, sql: JString, buf: JByteArray) {
    match (|| -> anyhow::Result<u64> {
        let conn = ManuallyDrop::new(unsafe { Box::from_raw(conn as *mut Connection) });
        let sql = env.get_string(&sql)?;
        let buf = env.convert_byte_array(buf)?;
        Ok(execute(&conn, &sql.to_string_lossy(), buf)?)
    })() {
        Ok(_) => (),
        Err(err) => env.throw(err.to_string()).unwrap(),
    }
}

#[jni_fn("tech.turso.libsql.ConnectionImpl")]
pub fn nativeExecuteBatch(mut env: JNIEnv, _: JClass, tx: jlong, sql: JString) {
    // TODO: Support BatchRows
    match (|| -> anyhow::Result<_> {
        let tx = ManuallyDrop::new(unsafe { Box::from_raw(tx as *mut Connection) });
        let sql = env.get_string(&sql)?;
        Ok(RT.block_on(tx.execute_batch(&sql.to_string_lossy()))?)
    })() {
        Ok(_) => (),
        Err(err) => env.throw(err.to_string()).unwrap(),
    }
}

#[jni_fn("tech.turso.libsql.ConnectionImpl")]
pub fn nativeQuery(
    mut env: JNIEnv,
    _: JClass,
    conn: jlong,
    sql: JString,
    buf: JByteArray,
) -> jlong {
    match (|| -> anyhow::Result<Rows> {
        let conn = ManuallyDrop::new(unsafe { Box::from_raw(conn as *mut Connection) });
        let sql = env.get_string(&sql)?;
        let buf = env.convert_byte_array(buf)?;
        Ok(query(&conn, &sql.to_string_lossy(), buf)?)
    })() {
        Ok(row) => Box::into_raw(Box::new(row)) as jlong,
        Err(err) => {
            env.throw(err.to_string()).unwrap();
            return ptr::null_mut::<Rows>() as jlong;
        }
    }
}

#[jni_fn("tech.turso.libsql.ConnectionImpl")]
pub fn nativeTransaction(mut env: JNIEnv, _: JClass, conn: jlong) -> jlong {
    let conn = ManuallyDrop::new(unsafe { Box::from_raw(conn as *mut Connection) });

    match RT.block_on(conn.transaction()) {
        Ok(t) => Box::into_raw(Box::new(t)) as jlong,
        Err(err) => {
            env.throw(err.to_string()).unwrap();
            return ptr::null_mut::<Transaction>() as jlong;
        }
    }
}

#[jni_fn("tech.turso.libsql.ConnectionImpl")]
pub fn nativeClose(_: JNIEnv, _: JClass, conn: jlong) {
    drop(unsafe { Box::from_raw(conn as *mut Connection) });
}

#[jni_fn("tech.turso.libsql.Rows")]
pub fn nativeNext(mut env: JNIEnv, _: JClass, rows: jlong) -> jbyteArray {
    match (|| -> anyhow::Result<JByteArray> {
        let mut rows = ManuallyDrop::new(unsafe { Box::from_raw(rows as *mut Rows) });
        let mut values = Vec::<proto::Value>::new();

        if let Some(row) = RT.block_on(rows.next())? {
            for i in 0..rows.column_count() {
                let value = row.get_value(i)?;
                values.push(value.into());
            }
        }

        Ok(env.byte_array_from_slice(proto::Row { values }.encode_to_vec().as_slice())?)
    })() {
        Ok(byte_array) => byte_array.into_raw(),
        Err(err) => {
            env.throw(err.to_string()).unwrap();
            return JByteArray::default().into_raw();
        }
    }
}

#[jni_fn("tech.turso.libsql.Rows")]
pub fn nativeClose(_: JNIEnv, _: JClass, rows: jlong) {
    drop(unsafe { Box::from_raw(rows as *mut Rows) });
}

#[jni_fn("tech.turso.libsql.Transaction")]
pub fn nativeExecute(mut env: JNIEnv, _: JClass, tx: jlong, sql: JString, buf: JByteArray) {
    match (|| -> anyhow::Result<u64> {
        let tx = ManuallyDrop::new(unsafe { Box::from_raw(tx as *mut Transaction) });
        let sql = env.get_string(&sql)?;
        let buf = env.convert_byte_array(buf)?;
        Ok(execute(&tx, &sql.to_string_lossy(), buf)?)
    })() {
        Ok(_) => (),
        Err(err) => env.throw(err.to_string()).unwrap(),
    }
}

#[jni_fn("tech.turso.libsql.Transaction")]
pub fn nativeExecuteBatch(mut env: JNIEnv, _: JClass, tx: jlong, sql: JString) {
    // TODO: Support BatchRows
    match (|| -> anyhow::Result<_> {
        let tx = ManuallyDrop::new(unsafe { Box::from_raw(tx as *mut Transaction) });
        let sql = env.get_string(&sql)?;
        Ok(RT.block_on(tx.execute_batch(&sql.to_string_lossy()))?)
    })() {
        Ok(_) => (),
        Err(err) => env.throw(err.to_string()).unwrap(),
    }
}

#[jni_fn("tech.turso.libsql.Transaction")]
pub fn nativeQuery(mut env: JNIEnv, _: JClass, tx: jlong, sql: JString, buf: JByteArray) -> jlong {
    match (|| -> anyhow::Result<Rows> {
        let tx = ManuallyDrop::new(unsafe { Box::from_raw(tx as *mut Transaction) });
        let sql = env.get_string(&sql)?;
        let buf = env.convert_byte_array(buf)?;
        Ok(query(&tx, &sql.to_string_lossy(), buf)?)
    })() {
        Ok(row) => Box::into_raw(Box::new(row)) as jlong,
        Err(err) => {
            env.throw(err.to_string()).unwrap();
            return ptr::null_mut::<Rows>() as jlong;
        }
    }
}

#[jni_fn("tech.turso.libsql.Transaction")]
pub fn nativeTransaction(mut env: JNIEnv, _: JClass, tx: jlong) -> jlong {
    let tx = ManuallyDrop::new(unsafe { Box::from_raw(tx as *mut Transaction) });

    match RT.block_on(tx.transaction()) {
        Ok(t) => Box::into_raw(Box::new(t)) as jlong,
        Err(err) => {
            env.throw(err.to_string()).unwrap();
            return ptr::null_mut::<Transaction>() as jlong;
        }
    }
}

#[jni_fn("tech.turso.libsql.Transaction")]
pub fn nativeCommit(mut env: JNIEnv, _: JClass, tx: jlong) {
    let tx = unsafe { Box::from_raw(tx as *mut Transaction) };

    match RT.block_on(tx.commit()) {
        Ok(_) => (),
        Err(err) => {
            env.throw(err.to_string()).unwrap();
        }
    }
}

#[jni_fn("tech.turso.libsql.Transaction")]
pub fn nativeRollback(mut env: JNIEnv, _: JClass, tx: jlong) {
    let tx = unsafe { Box::from_raw(tx as *mut Transaction) };

    match RT.block_on(tx.rollback()) {
        Ok(_) => (),
        Err(err) => {
            env.throw(err.to_string()).unwrap();
        }
    }
}

#[jni_fn("tech.turso.libsql.Transaction")]
pub fn nativeClose(_: JNIEnv, _: JClass, tx: jlong) {
    drop(unsafe { Box::from_raw(tx as *mut Transaction) });
}

/*
 * Copyright (c) 2012-2013, Judison Oliveira Gil Filho <judison@gmail.com>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.judison.mongodm;

import java.util.List;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

public class MCollection<T> {

	final MDB mdb;
	private final DBCollection coll;
	private final TypeInfo typeInfo;
	final Class<T> cls;

	public MCollection(MDB mdb, Class<T> cls) throws MException {
		this(mdb, cls, null);
	}

	public MCollection(MDB mdb, Class<T> cls, String entityName) throws MException {
		this.mdb = mdb;
		this.cls = cls;
		if (cls == MObject.class)
			this.typeInfo = null;
		else
			try {
				this.typeInfo = Mapper.getTypeInfo(cls);
			} catch (Throwable e) {
				throw new MException(e);
			}
		if (entityName == null)
			entityName = typeInfo.entityName;
		this.coll = mdb.getMongoDB().getCollection(entityName);
		this.coll.setDBDecoderFactory(MDecoder.FACTORY);

		if (typeInfo != null)
			for (IndexInfo idx: typeInfo.indexes)
				try {
					coll.ensureIndex(idx.keys, idx.options);
				} catch (MongoException e) {
					throw new MException(e);
				}
	}

	public T load(Object id) throws MException {
		try {
			MObject data = (MObject)coll.findOne(id);
			return mapLoad(data);
		} catch (MongoException e) {
			throw new MException(e);
		}
	}

	public T findOne() throws MException {
		try {
			MObject data = (MObject)coll.findOne();
			return mapLoad(data);
		} catch (MongoException e) {
			throw new MException(e);
		}
	}

	public T findOne(DBObject query) throws MException {
		try {
			MObject data = (MObject)coll.findOne(query);
			return mapLoad(data);
		} catch (MongoException e) {
			throw new MException(e);
		}
	}

	public T findOne(Query query) throws MException {
		try {
			MObject data = (MObject)coll.findOne(query.toMObject());
			return mapLoad(data);
		} catch (MongoException e) {
			throw new MException(e);
		}
	}

	public MCursor<T> find() throws MException {
		try {
			DBCursor cursor = coll.find();
			return new MCursor<T>(this, cls, cursor, false);
		} catch (MongoException e) {
			throw new MException(e);
		}
	}

	public MCursor<T> find(MObject query) throws MException {
		try {
			DBCursor cursor = coll.find(query);
			return new MCursor<T>(this, cls, cursor, false);
		} catch (MongoException e) {
			throw new MException(e);
		}
	}

	public MCursor<T> find(Query query) throws MException {
		try {
			DBCursor cursor = coll.find(query.toMObject());
			return new MCursor<T>(this, cls, cursor, false);
		} catch (MongoException e) {
			throw new MException(e);
		}
	}

	public MCursor<MObject> find(MObject query, MObject projection) throws MException {
		try {
			DBCursor cursor = coll.find(query, projection);
			return new MCursor<MObject>(this, MObject.class, cursor, true);
		} catch (MongoException e) {
			throw new MException(e);
		}
	}

	public MCursor<MObject> find(Query query, Projection projection) throws MException {
		try {
			DBCursor cursor = coll.find(query.toMObject(), projection.toMObject());
			return new MCursor<MObject>(this, MObject.class, cursor, true);
		} catch (MongoException e) {
			throw new MException(e);
		}
	}

	/*
	@SuppressWarnings("unchecked")
	public List<MObject> aggregate(Pipeline pipeline) throws MException {
		try {
			MObject cmd = new MObject("aggregate", coll.getName());
			cmd.put("pipeline", pipeline.getOperators());
			
			
			
			CommandResult res = mdb.getMongoDB().command(cmd);
			MongoException e = res.getException();
			if (e != null)
				throw new MException(e);

			Object result = res.get("result");
			return (List<MObject>)result;
		} catch (MongoException e) {
			throw new MException(e);
		}
	}
	*/

	@SuppressWarnings("unchecked")
	public List<MObject> aggregate(Pipeline pipeline) throws MException {
		MObject cmd = new MObject("aggregate", coll.getName());
		cmd.put("pipeline", pipeline.getOperators());
		return (List<MObject>)mdb.command(cmd);
	}

	private void checkResult(WriteResult res) throws MException {
		String error = res.getError();
		if (error != null)
			throw new MException(error);
	}

	public void save(T object) throws MException {
		try {
			if (cls == MObject.class) {
				MObject data = (MObject)object;
				WriteResult res = coll.save(data);
				String error = res.getError();
				if (error != null)
					throw new MException(error);
			} else {
				MObject data = mdb.getLoadedData(object);
				if (data == null)
					data = new MObject();

				Mapper.saveEntity(object, data);

				WriteResult res = coll.save(data);

				checkResult(res);

				Mapper.load(object, data);

				mdb.putLoadedData(object, data);
			}
		} catch (MongoException e) {
			throw new MException(e);
		}
	}

	public int update(Query query, Update update) throws MException {
		return update(query.toMObject(), update.toMObject(), false, false);
	}

	public int update(Query query, Update update, boolean upsert, boolean multi) throws MException {
		return update(query.toMObject(), update.toMObject(), upsert, multi);
	}

	public int update(MObject query, MObject update) throws MException {
		return update(query, update, false, false);
	}

	public int update(MObject query, MObject update, boolean upsert, boolean multi) throws MException {
		WriteResult res = coll.update(query, update, upsert, multi);
		checkResult(res);
		return res.getN();
	}

	public void remove(T object) throws MException {
		MObject data = new MObject();
		Mapper.saveEntity(object, data);
		WriteResult res = coll.remove(new MObject("_id", data.get("_id")));
		checkResult(res);
	}

	public void removeById(Object id) throws MException {
		WriteResult res = coll.remove(new MObject("_id", id));
		checkResult(res);
	}

	public long count() {
		return coll.count();
	}

	public long count(Query query) {
		return coll.count(query.toMObject());
	}

	@SuppressWarnings("unchecked")
	T mapLoad(MObject data) {
		if (cls == MObject.class)
			return (T)data;
		if (data == null)
			return null;
		else
			try {
				T object = (T)typeInfo.constructor.newInstance();
				Mapper.load(object, data);
				mdb.putLoadedData(object, data);
				return object;
			} catch (Throwable e) {
				throw new MRuntimeException(e);
			}
	}

	public MDB getMDB() {
		return mdb;
	}

}
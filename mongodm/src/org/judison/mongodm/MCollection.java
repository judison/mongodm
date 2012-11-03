/*
 * Copyright (c) 2012, Judison Oliveira Gil Filho <judison@gmail.com>
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
 */
package org.judison.mongodm;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

public class MCollection<T> {

	final MDB mdb;
	private final Mapper mapper;
	private final DBCollection coll;
	private final TypeInfo typeInfo;
	final Class<T> cls;

	public MCollection(MDB mdb, Class<T> cls) throws MException {
		this.mdb = mdb;
		this.mapper = mdb.getMapper();
		this.cls = cls;
		try {
			this.typeInfo = mapper.getTypeInfo(cls);
		} catch (Throwable e) {
			throw new MException(e);
		}
		this.coll = mdb.getMongoDB().getCollection(typeInfo.entityName);

		for (IndexInfo idx: typeInfo.indexes)
			try {
				coll.ensureIndex(idx.keys, idx.options);
			} catch (MongoException e) {
				throw new MException(e);
			}
	}

	public T load(Object id) throws MException {
		try {
			DBObject data = coll.findOne(id);
			return mapLoad(data);
		} catch (MongoException e) {
			throw new MException(e);
		}
	}

	public T findOne() throws MException {
		try {
			DBObject data = coll.findOne();
			return mapLoad(data);
		} catch (MongoException e) {
			throw new MException(e);
		}
	}

	public T findOne(DBObject query) throws MException {
		try {
			DBObject data = coll.findOne(query);
			return mapLoad(data);
		} catch (MongoException e) {
			throw new MException(e);
		}
	}

	public T findOne(Query query) throws MException {
		try {
			DBObject data = coll.findOne(query.toDBObject());
			return mapLoad(data);
		} catch (MongoException e) {
			throw new MException(e);
		}
	}

	public MCursor<T> find() throws MException {
		try {
			DBCursor cursor = coll.find();
			return new MCursor<T>(this, cursor);
		} catch (MongoException e) {
			throw new MException(e);
		}
	}

	public MCursor<T> find(DBObject query) throws MException {
		try {
			DBCursor cursor = coll.find(query);
			return new MCursor<T>(this, cursor);
		} catch (MongoException e) {
			throw new MException(e);
		}
	}

	public MCursor<T> find(Query query) throws MException {
		try {
			DBCursor cursor = coll.find(query.toDBObject());
			return new MCursor<T>(this, cursor);
		} catch (MongoException e) {
			throw new MException(e);
		}
	}

	public void save(T object) throws MException {
		try {
			DBObject data = mdb.getLoadedData(object);
			if (data == null)
				data = new BasicDBObject();

			mapper.save(object, data);

			WriteResult res = coll.save(data);

			String error = res.getError();
			if (error != null)
				throw new MException(error);

			mapper.load(object, data);

			mdb.putLoadedData(object, data);
		} catch (MongoException e) {
			throw new MException(e);
		}
	}

	T mapLoad(DBObject data) {
		if (data == null)
			return null;
		else
			try {
				@SuppressWarnings("unchecked")
				T object = (T)typeInfo.constructor.newInstance();
				mapper.load(object, data);
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

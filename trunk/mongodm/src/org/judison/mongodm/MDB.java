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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MDB {

	private final DB database;
	private final Mapper mapper;
	private Map<Object, MCollection<?>> collections = new HashMap<Object, MCollection<?>>();

	public MDB(String url) throws UnknownHostException {
		this(Mongo.connect(new DBAddress(url)), new Mapper());
	}

	public MDB(DBAddress url) {
		this(Mongo.connect(url), new Mapper());
	}

	public MDB(DB database) {
		this(database, new Mapper());
	}

	public MDB(DB database, Mapper mapper) {
		this.database = database;
		this.mapper = mapper;
	}

	public Mapper getMapper() {
		return mapper;
	}

	public <T> void putCollection(Class<T> cls, MCollection<T> coll) throws MException {
		if (coll.mdb != this)
			throw new IllegalArgumentException();
		if (!coll.cls.equals(cls))
			throw new IllegalArgumentException();
		synchronized (collections) {
			collections.put(cls, coll);
		}
	}

	public <T> MCollection<T> getCollection(Class<T> cls) throws MException {
		synchronized (collections) {
			@SuppressWarnings("unchecked")
			MCollection<T> coll = (MCollection<T>)collections.get(cls);
			if (coll == null) {
				coll = new MCollection<T>(this, cls);
				collections.put(cls, coll);
			}
			return coll;
		}
	}

	public MCollection<DBObject> getCollection(String name) throws MException {
		synchronized (collections) {
			@SuppressWarnings("unchecked")
			MCollection<DBObject> coll = (MCollection<DBObject>)collections.get(name);
			if (coll == null) {
				coll = new MCollection<DBObject>(this, DBObject.class, name);
				collections.put(name, coll);
			}
			return coll;
		}
	}

	public DB getMongoDB() {
		return database;
	}

	private WeakHashMap<Object, DBObject> loadedDatas = new WeakHashMap<Object, DBObject>();

	void putLoadedData(Object key, DBObject value) {
		synchronized (loadedDatas) {
			loadedDatas.put(key, value);
		}
	}

	DBObject getLoadedData(Object key) {
		synchronized (loadedDatas) {
			return loadedDatas.get(key);
		}
	}

	private List<MCursor<?>> cursors = new ArrayList<MCursor<?>>();

	protected <T> void onCursorCreated(MCursor<T> cursor, Class<T> cls) {
		synchronized (cursors) {
			cursors.add(cursor);
		}
	}

	protected <T> void onCursorClosed(MCursor<T> cursor) {
		synchronized (cursors) {
			cursors.remove(cursor);
		}
	}

	public void closeCursors() {
		synchronized (cursors) {
			for (MCursor<?> cursor: cursors.toArray(new MCursor<?>[cursors.size()])) {
				cursor.close();
			}
		}
	}
}

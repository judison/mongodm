/*
 * Copyright (c) 2013, Judison Oliveira Gil Filho <judison@gmail.com>
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bson.BSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONCallback;

public class MObject implements BSONObject, DBObject {

	public static MObject parseJSON(String str) {
		return (MObject)JSON.parse(str, new JSONCallback() {

			@Override
			public BSONObject create() {
				return new MObject();
			}

			@Override
			public BSONObject create(boolean array, List<String> path) {
				if (array)
					return new BasicDBList();
				else
					return new MObject();
			}
		});
	}

	private LinkedHashMap<String, Object> map;

	public MObject() {
		this(true);
	}

	public MObject(String name, Object value) {
		this(true);
		put(name, value);
	}

	public MObject(BSONObject other) {
		this(true);
		putAll(other);
	}

	/*
	MObject(TypeInfo typeInfo, Object obj) {
		this(true);
		this.obj = obj;
		for (PropertyInfo pi: typeInfo.properties)
			map.put(pi.name, pi);
	}
	*/

	MObject(boolean isMObject) {
		if (isMObject)
			map = new LinkedHashMap<String, Object>();
	}

	/*
	void mapToObject(TypeInfo typeInfo, Object obj) {
		if (this.obj != null)
			throw new IllegalStateException("MObject already mapped");
		this.obj = obj;

		for (PropertyInfo pi: typeInfo.properties) {
			Object bsonValue = map.put(pi.name, pi);
			setField(pi, obj, bsonValue);
		}
	}

	public boolean isBacked() {
		return obj != null;
	}

	public Object getBackendObject() {
		return obj;
	}
	*/

	@Override
	public String toString() {
		return JSON.serialize(this);
	}

	@Override
	public Object put(String name, Object value) {
		synchronized (map) {
			/*
			Object old = map.get(name);
			if (old instanceof PropertyInfo)
				try {
					PropertyInfo pi = (PropertyInfo)old;
					old = getField(pi, obj);
					setField(pi, obj, value);

					return old;
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			*/
			return map.put(name, value);
		}
	}

	@Override
	public void putAll(BSONObject o) {
		for (String name: o.keySet())
			put(name, o.get(name));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void putAll(Map m) {
		for (Object o: m.entrySet()) {
			Entry e = (Entry)o;
			put(String.valueOf(e.getKey()), e.getValue());
		}
	}

	@Override
	public Object get(String name) {
		synchronized (map) {
			Object value = map.get(name);
			/*
			if (value instanceof PropertyInfo)
				try {
					PropertyInfo pi = (PropertyInfo)value;
					value = getField(pi, obj);
					return value;
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			*/
			return value;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map toMap() {
		synchronized (map) {
			Map<String, Object> resp = new LinkedHashMap<String, Object>();
			for (Entry<String, Object> e: map.entrySet()) {
				String name = e.getKey();
				Object value = map.get(name);
				/*
				if (value instanceof PropertyInfo)
					try {
						PropertyInfo pi = (PropertyInfo)value;
						value = getField(pi, obj);
					} catch (Throwable ex) {
						throw new RuntimeException(ex);
					}
				*/
				resp.put(name, value);
			}
			return resp;
		}
	}

	@Override
	public Object removeField(String name) {
		synchronized (map) {
			/*
			Object value = map.get(name);
			if (value instanceof PropertyInfo)
				try {
					PropertyInfo pi = (PropertyInfo)value;
					value = getField(pi, obj);
					setField(pi, obj, null);
					return value;
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			*/
			return map.remove(name);
		}
	}

	@Override
	@Deprecated
	public boolean containsKey(String name) {
		return containsField(name);
	}

	@Override
	public boolean containsField(String name) {
		synchronized (map) {
			return map.containsKey(name);
		}
	}

	@Override
	public Set<String> keySet() {
		synchronized (map) {
			return map.keySet();
		}
	}

	private boolean partialObject;

	@Override
	public void markAsPartialObject() {
		partialObject = true;
	}

	@Override
	public boolean isPartialObject() {
		return partialObject;
	}

	/*
	private static void setField(PropertyInfo pi, Object object, Object bsonValue) {
		try {
			pi.field.set(object, Mapper.bsonToJava(pi.cls, pi.itemCls, bsonValue));
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static Object getField(PropertyInfo pi, Object object) {
		try {
			return Mapper.javaToBson(pi.field.get(object), pi);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	*/

	/*
	public int getInt(String name) {
		Object value = get(name);
		return ((Number)value).intValue();
	}

	public int getInt(String name, int def) {
		Object value = get(name);
		if (value == null)
			return def;
		return ((Number)value).intValue();
	}

	public long getLong(String name) {
		Object value = get(name);
		return ((Number)value).longValue();
	}

	public long getLong(String name, long def) {
		Object value = get(name);
		if (value == null)
			return def;
		return ((Number)value).longValue();
	}
	*/
}

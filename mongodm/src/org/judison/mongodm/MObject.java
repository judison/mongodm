/*
 * Copyright (c) 2013-2015, Judison Oliveira Gil Filho <judison@gmail.com>
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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.Set;

import org.bson.BSONObject;
import org.bson.types.BSONTimestamp;
import org.bson.types.Code;
import org.bson.types.CodeWScope;
import org.bson.types.MaxKey;
import org.bson.types.MinKey;
import org.bson.types.ObjectId;
import org.bson.types.Symbol;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class MObject implements DBObject, BSONObject {

	public static MObject parseJSON(String str) {
		return (MObject) JSON.parse(str, MDecoder.FACTORY.create()
				.getDBCallback(null));
	}

	private Mapper mapper;
	private Object obj;

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

	MObject(TypeInfo typeInfo, Mapper mapper, Object obj) {
		this(true);
		this.obj = obj;
		this.mapper = mapper;
		for (PropertyInfo pi : typeInfo.properties)
			map.put(pi.name, pi);
	}

	MObject(boolean isMObject) {
		if (isMObject)
			map = new LinkedHashMap<String, Object>();
	}

	void mapToObject(TypeInfo typeInfo, Mapper mapper, Object obj) {
		if (this.obj != null)
			throw new IllegalStateException("MObject already mapped");
		this.obj = obj;
		this.mapper = mapper;

		for (PropertyInfo pi : typeInfo.properties) {
			Object bsonValue = map.put(pi.name, pi);
			setField(pi, obj, mapper, bsonValue);
		}
	}

	void unmap() {
		if (this.obj == null)
			return;
		synchronized (map) {
			List<String> props = new ArrayList<String>(map.keySet());
			for (String name : props) {
				Object value = map.get(name);
				
				if (value instanceof PropertyInfo) 
					try {
						PropertyInfo pi = (PropertyInfo) value;
						value = getField(pi, obj, mapper);
						map.put(name, value);
					} catch (Throwable e) {
						e.printStackTrace(); //TODO melhorar
						map.remove(name);
					}
			}
			this.obj = null;
			this.mapper = null;
		}
	}

	public boolean isBacked() {
		return obj != null;
	}

	public Object getBackendObject() {
		return obj;
	}

	@Override
	public String toString() {
		return JSON.serialize(this);
	}

	protected void checkValue(Object value) {
		if (value instanceof MObject)
			return;

		if (value == null)
			return;
		if (value instanceof Number)
			return;
		if (value instanceof String)
			return;
		if (value instanceof Boolean)
			return;
		if (value instanceof Character)
			return;
		if (value instanceof ObjectId)
			return;
		if (value instanceof Date)
			return;
		if (value instanceof Pattern)
			return;
		if (value instanceof UUID)
			return;
		if (value instanceof MaxKey || value instanceof MinKey)
			return;
		if (value instanceof byte[])
			return;
		if (value instanceof BSONTimestamp || value instanceof Symbol
				|| value instanceof Code || value instanceof CodeWScope)
			return;

		throw new IllegalArgumentException(getClass().getSimpleName()
				+ " can't store a " + value.getClass().getName());
	}

	@Override
	public Object put(String name, Object value) {
		checkValue(value);
		synchronized (map) {
			Object old = map.get(name);
			if (old instanceof PropertyInfo)
				try {
					PropertyInfo pi = (PropertyInfo) old;
					old = getField(pi, obj, mapper);
					setField(pi, obj, mapper, value);

					return old;
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}

			return map.put(name, value);
		}
	}

	@Override
	public void putAll(BSONObject o) {
		for (String name : o.keySet())
			put(name, o.get(name));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void putAll(Map m) {
		for (Object o : m.entrySet()) {
			Entry e = (Entry) o;
			put(String.valueOf(e.getKey()), e.getValue());
		}
	}

	@Override
	public Object get(String name) {
		synchronized (map) {
			Object value = map.get(name);
			if (value instanceof PropertyInfo)
				try {
					PropertyInfo pi = (PropertyInfo) value;
					value = getField(pi, obj, mapper);
					return value;
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}

			return value;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map toMap() {
		synchronized (map) {
			Map<String, Object> resp = new LinkedHashMap<String, Object>();
			for (Entry<String, Object> e : map.entrySet()) {
				String name = e.getKey();
				Object value = map.get(name);
				if (value instanceof PropertyInfo)
					try {
						PropertyInfo pi = (PropertyInfo) value;
						value = getField(pi, obj, mapper);
					} catch (Throwable ex) {
						throw new RuntimeException(ex);
					}
				resp.put(name, value);
			}
			return resp;
		}
	}

	@Override
	public Object removeField(String name) {
		synchronized (map) {
			Object value = map.get(name);
			if (value instanceof PropertyInfo)
				try {
					PropertyInfo pi = (PropertyInfo) value;
					value = getField(pi, obj, mapper);
					setField(pi, obj, mapper, null);
					return value;
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}

			map.remove(name);
			return value;
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
			// TODO: ver se eh null, caso seja um PropertyInfo
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

	private static void setField(PropertyInfo pi, Object object, Mapper mapper,
			Object bsonValue) {
		try {
			pi.field.set(object,
					mapper.bsonToJava(pi.cls, pi.itemCls, bsonValue));
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static Object getField(PropertyInfo pi, Object object, Mapper mapper) {
		try {
			return mapper.javaToBson(pi.field.get(object), pi);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	// ==========================
	// deep (Dot Notation) Stuff
	// ==========================

	public Object deepGet(String name) {
		int dotIdx = name.indexOf('.');
		if (dotIdx >= 0) {
			String p0 = name.substring(0, dotIdx);
			String p1 = name.substring(dotIdx + 1);
			Object aux = get(p0); // otimizar depois...
			if (aux instanceof MObject)
				return ((MObject) aux).deepGet(p1);
			else
				return null; // ou aux eh null, ou nao eh um { }, entao...
		} else
			return get(name); // otimizar depois...
	}

	public void deepPut(String name, Object value) {
		checkValue(value);
		int dotIdx = name.indexOf('.');
		if (dotIdx >= 0) {
			String p0 = name.substring(0, dotIdx);
			String p1 = name.substring(dotIdx + 1);
			Object aux = get(p0); // otimizar depois...
			if (!(aux instanceof MObject)) {
				aux = new MObject();
				put(p0, aux); // otimizar depois...
			}
			((MObject) aux).deepPut(p1, value);
		} else
			put(name, value); // otimizar depois...
	}

	public boolean deepContainsField(String name) {
		int dotIdx = name.indexOf('.');
		if (dotIdx >= 0) {
			String p0 = name.substring(0, dotIdx);
			String p1 = name.substring(dotIdx + 1);
			Object aux = get(p0); // otimizar depois...
			if (aux instanceof MObject)
				return ((MObject) aux).deepContainsField(p1);
			else
				return false; // ou aux eh null, ou nao eh um { }, entao...
		} else
			return containsField(name); // otimizar depois...
	}

	public Object deepRemove(String name) {
		int dotIdx = name.indexOf('.');
		if (dotIdx >= 0) {
			String p0 = name.substring(0, dotIdx);
			String p1 = name.substring(dotIdx + 1);
			Object aux = get(p0); // otimizar depois...
			if (aux instanceof MObject)
				return ((MObject) aux).deepRemove(p1);
			else
				return null; // ou aux eh null, ou nao eh um { }, entao...
		} else
			return removeField(name); // otimizar depois...
	}

}

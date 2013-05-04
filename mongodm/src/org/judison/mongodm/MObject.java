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

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bson.BSONObject;
import org.bson.types.BSONTimestamp;
import org.bson.types.Code;
import org.bson.types.CodeWScope;
import org.bson.types.MaxKey;
import org.bson.types.MinKey;
import org.bson.types.ObjectId;
import org.bson.types.Symbol;

import com.mongodb.DBCallback;
import com.mongodb.DBCollection;
import com.mongodb.DBDecoder;
import com.mongodb.DBDecoderFactory;
import com.mongodb.DBObject;
import com.mongodb.DefaultDBCallback;
import com.mongodb.DefaultDBDecoder;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONCallback;

public class MObject implements BSONObject, DBObject {

	public static class MDBCallback extends DefaultDBCallback {

		public MDBCallback(DBCollection coll) {
			super(coll);
		}

		@Override
		public BSONObject create() {
			return new MObject();
		}

		@Override
		public BSONObject create(boolean array, List<String> path) {
			if (array)
				return new MList();
			else
				return new MObject();
		}
	}

	public static class MJSONCallback extends JSONCallback {

		@Override
		public BSONObject create() {
			return new MObject();
		}

		@Override
		public BSONObject create(boolean array, List<String> path) {
			if (array)
				return new MList();
			else
				return new MObject();
		}
	}

	public static final DBDecoderFactory DB_DECODER_FACTORY = new DBDecoderFactory() {

		@Override
		public DBDecoder create() {
			return new DefaultDBDecoder() {

				@Override
				public DBCallback getDBCallback(DBCollection collection) {
					return new MDBCallback(collection);
				}
			};
		}
	};

	public static MObject parseJSON(String str) {
		return (MObject)JSON.parse(str, new MJSONCallback());
	}

	public static MObject deepCopy(BSONObject src) {
		if (src == null)
			return null;
		if (src instanceof List) {
			MList dst = new MList();
			for (Object item: (List<?>)src)
				if (item instanceof BSONObject)
					dst.add(deepCopy((BSONObject)item));
				else
					dst.add(item);
			return dst;
		}

		MObject dst = new MObject();
		for (String name: src.keySet()) {
			Object value = src.get(name);
			if (value instanceof BSONObject)
				value = deepCopy((BSONObject)value);
			dst.set(name, value);
		}
		return dst;
	}

	private LinkedHashMap<String, Object> map;

	public MObject() {
		this(true);
	}

	public MObject(String name, Object value) {
		this(true);
		set(name, value);
	}

	public MObject(BSONObject other) {
		this(true);
		putAll(other);
	}

	/** Usado pelo MList **/
	MObject(boolean isMObject) {
		if (isMObject)
			map = new LinkedHashMap<String, Object>();
	}

	@Override
	public String toString() {
		return JSON.serialize(this);
	}

	//==========================
	// checkValue()
	//==========================

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
		if (value instanceof BSONTimestamp || value instanceof Symbol || value instanceof Code || value instanceof CodeWScope)
			return;

		throw new IllegalArgumentException(getClass().getSimpleName() + " can't store a " + value.getClass().getName());
	}

	//==========================
	// set(name, value)
	//==========================

	public Object set(String name, Object value) {
		checkValue(value);
		synchronized (map) {
			return map.put(name, value);
		}
	}

	@Override
	@Deprecated
	public Object put(String name, Object value) {
		checkValue(value);
		synchronized (map) {
			return map.put(name, value);
		}
	}

	@Override
	public void putAll(BSONObject o) {
		synchronized (map) {
			for (String name: o.keySet()) {
				Object value = o.get(name);
				checkValue(value);
				map.put(name, value);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void putAll(Map m) {
		synchronized (map) {
			for (Object o: m.entrySet()) {
				Entry e = (Entry)o;
				Object value = e.getValue();
				checkValue(value);
				map.put(String.valueOf(e.getKey()), value);
			}
		}
	}

	//==========================
	// get(name)
	//==========================

	@Override
	public Object get(String name) {
		synchronized (map) {
			return map.get(name);
		}
	}

	//==========================
	// remove(name)
	//==========================

	public Object remove(String name) {
		synchronized (map) {
			return map.remove(name);
		}
	}

	@Override
	@Deprecated
	public Object removeField(String name) {
		return remove(name);
	}

	//==========================
	// contains(name)
	//==========================

	public boolean contains(String name) {
		synchronized (map) {
			return map.containsKey(name);
		}
	}

	@Override
	@Deprecated
	public boolean containsKey(String name) {
		return contains(name);
	}

	@Override
	@Deprecated
	public boolean containsField(String name) {
		return contains(name);
	}

	//==========================
	// isEmpty()
	//==========================

	public boolean isEmpty() {
		synchronized (map) {
			return map.size() == 0;
		}
	}

	//==========================
	// BSONObject Stuff
	//==========================

	@SuppressWarnings("rawtypes")
	@Override
	public Map toMap() {
		synchronized (map) {
			Map<String, Object> resp = new LinkedHashMap<String, Object>();
			for (Entry<String, Object> e: map.entrySet()) {
				String name = e.getKey();
				Object value = map.get(name);
				resp.put(name, value);
			}
			return resp;
		}
	}

	@Override
	public Set<String> keySet() {
		synchronized (map) {
			return map.keySet();
		}
	}

	//==========================
	// DBObject Stuff
	//==========================

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

	//==========================
	// deep (Dot Notation) Stuff
	//==========================

	public Object deepGet(String name) {
		int dotIdx = name.indexOf('.');
		if (dotIdx >= 0) {
			String p0 = name.substring(0, dotIdx);
			String p1 = name.substring(dotIdx + 1);
			Object aux = get(p0); // otimizar depois...
			if (aux instanceof MObject)
				return ((MObject)aux).deepGet(p1);
			else
				return null; // ou aux eh null, ou nao eh um { }, entao...
		} else
			return get(name); // otimizar depois...
	}

	public void deepSet(String name, Object value) {
		checkValue(value);
		int dotIdx = name.indexOf('.');
		if (dotIdx >= 0) {
			String p0 = name.substring(0, dotIdx);
			String p1 = name.substring(dotIdx + 1);
			Object aux = get(p0); // otimizar depois...
			if (!(aux instanceof MObject)) {
				aux = new MObject();
				set(p0, aux); // otimizar depois...
			}
			((MObject)aux).deepSet(p1, value);
		} else
			set(name, value); // otimizar depois...
	}

	public boolean deepContains(String name) {
		int dotIdx = name.indexOf('.');
		if (dotIdx >= 0) {
			String p0 = name.substring(0, dotIdx);
			String p1 = name.substring(dotIdx + 1);
			Object aux = get(p0); // otimizar depois...
			if (aux instanceof MObject)
				return ((MObject)aux).deepContains(p1);
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
				return ((MObject)aux).deepRemove(p1);
			else
				return null; // ou aux eh null, ou nao eh um { }, entao...
		} else
			return removeField(name); // otimizar depois...
	}

}

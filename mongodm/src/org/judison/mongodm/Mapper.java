/*
 * Copyright (c) 2012-2015, Judison Oliveira Gil Filho <judison@gmail.com>
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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.bson.BSONObject;
import org.judison.mongodm.converter.LatLngConverter;
import org.judison.mongodm.converter.NumberConverter;
import org.judison.mongodm.converter.PassThruConverter;
import org.judison.mongodm.converter.TypeConverter;

public final class Mapper {

	private static Map<Class<?>, TypeConverter<?>> typeConverters = new HashMap<Class<?>, TypeConverter<?>>();

	public static <T> void registerTypeConverter(Class<T> cls, TypeConverter<T> converter) {
		typeConverters.put(cls, converter);
	}

	public static boolean hasTypeConverterFor(Class<?> cls) {
		return typeConverters.containsKey(cls);
	}

	static {
		typeConverters.put(Byte.class, NumberConverter.BYTE);
		typeConverters.put(Byte.TYPE, NumberConverter.BYTE);
		typeConverters.put(Short.class, NumberConverter.SHORT);
		typeConverters.put(Short.TYPE, NumberConverter.SHORT);
		typeConverters.put(Integer.class, NumberConverter.INTEGER);
		typeConverters.put(Integer.TYPE, NumberConverter.INTEGER);
		typeConverters.put(Long.class, NumberConverter.LONG);
		typeConverters.put(Long.TYPE, NumberConverter.LONG);
		typeConverters.put(Float.class, NumberConverter.FLOAT);
		typeConverters.put(Float.TYPE, NumberConverter.FLOAT);
		typeConverters.put(Double.class, NumberConverter.DOUBLE);
		typeConverters.put(Double.TYPE, NumberConverter.DOUBLE);

		typeConverters.put(BigInteger.class, NumberConverter.BIG_INTEGER);
		typeConverters.put(BigDecimal.class, NumberConverter.BIG_DECIMAL);

		typeConverters.put(LatLng.class, LatLngConverter.INSTANCE);

		for (Class<?> cls: PassThruConverter.CLASSES)
			typeConverters.put(cls, PassThruConverter.INSTANCE);
	}

	//===================================

	private static Map<Class<?>, TypeInfo> typeInfos = new HashMap<Class<?>, TypeInfo>();

	static TypeInfo getTypeInfo(Class<?> cls) {
		return getTypeInfo(cls, true);
	}

	static TypeInfo getTypeInfo(Class<?> cls, boolean canThrow) {
		try {
			TypeInfo info = typeInfos.get(cls);
			if (info == null) {
				info = new TypeInfo(cls);
				typeInfos.put(cls, info);
			}
			return info;
		} catch (RuntimeException e) {
			if (canThrow)
				throw e;
			else
				return null;
		}
	}

	public static void addClass(Class<?> cls) {
		TypeInfo info = typeInfos.get(cls);
		if (info == null) {
			info = new TypeInfo(cls);
			typeInfos.put(cls, info);
		}
	}

	//========================================================================

	private WeakHashMap<Object, MObject> mobjects = new WeakHashMap<Object, MObject>();

	Mapper() {}

	public <T> T bsonToJava(Class<T> cls, Object bsonValue) {
		return bsonToJava(cls, null, bsonValue);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T bsonToJava(Class<T> cls, Class<?> itemCls, Object bsonValue) {

		if (bsonValue == null)
			if (cls == Integer.TYPE)
				return (T)Integer.valueOf(0);
			else if (cls == Long.TYPE)
				return (T)Long.valueOf(0);
			else if (cls == Byte.TYPE)
				return (T)Byte.valueOf((byte)0);
			else if (cls == Short.TYPE)
				return (T)Short.valueOf((short)0);
			else if (cls == Double.TYPE)
				return (T)Double.valueOf(0);
			else if (cls == Float.TYPE)
				return (T)Float.valueOf(0);
			else if (cls == Boolean.TYPE)
				return (T)Boolean.FALSE;
			else if (cls == Character.TYPE)
				return (T)Character.valueOf((char)0);
			else
				return null;

		TypeConverter<T> tc = (TypeConverter<T>)typeConverters.get(cls);
		if (tc != null)
			return tc.bsonToJava(bsonValue);

		if (cls.isEnum())
			return (T)Enum.valueOf((Class<? extends Enum>)cls, bsonValue.toString());

		if (cls.isArray() || List.class.isAssignableFrom(cls)) {

			if (bsonValue instanceof MList) {
				MList mlist = (MList)bsonValue;
				Object javaObj = mlist.getBackendObject();
				if (javaObj != null)
					if (cls.isAssignableFrom(javaObj.getClass()))
						return (T)javaObj;
					else
						throw new IllegalArgumentException("Can't map to " + cls.getName() + ", already mapped to " + javaObj.getClass().getName());

				if (itemCls == null)
					throw new IllegalArgumentException("Can't map to " + cls.getName() + ", item class unknow");

				// precisa criar o obj...
				if (cls.isArray())
					javaObj = Array.newInstance(itemCls, mlist.size());
				else
					try {
						javaObj = cls.newInstance();
					} catch (Throwable e) {
						throw new IllegalArgumentException("Can't map to " + bsonValue.getClass().getName(), e);
					}

				mlist.mapToObject(cls.isArray(), this, javaObj, itemCls);

				synchronized (mobjects) {
					mobjects.put(javaObj, mlist);
				}

				return (T)javaObj;
			}

			throw new IllegalArgumentException("Can't map to " + cls.getName() + ", " + bsonValue.getClass() + " is not a MList");

		}

		if (bsonValue instanceof BSONObject) {

			MObject mobj = null;
			Object javaObj = null;
			if (bsonValue instanceof MObject) {
				mobj = (MObject)bsonValue;
				javaObj = mobj.getBackendObject();
				if (javaObj != null)
					if (cls.isAssignableFrom(javaObj.getClass()))
						return (T)javaObj;
					else
						throw new IllegalArgumentException("Can't map to " + cls.getName() + ", already mapped to " + javaObj.getClass().getName());
			}
			BSONObject bsonObject = (BSONObject)bsonValue;

			TypeInfo typeInfo = typeInfos.get(cls);
			if (typeInfo == null)
				try {
					typeInfo = new TypeInfo(cls);
					typeInfos.put(cls, typeInfo);
				} catch (Throwable e) {
					throw new IllegalArgumentException("Can't map to " + cls.getName(), e);
				}

			try {
				javaObj = cls.newInstance();
			} catch (Throwable e) {
				throw new IllegalArgumentException("Can't map to " + bsonValue.getClass().getName(), e);
			}

			if (mobj == null) {
				// to criando um novo MObject, mas o BSONObject q tava la, vai continuar
				mobj = new MObject(typeInfo, this, javaObj);
				mobj.putAll(bsonObject);
			} else {
				// faço meu MObject ficar mapeado
				mobj.mapToObject(typeInfo, this, javaObj);
			}
			synchronized (mobjects) {
				mobjects.put(javaObj, mobj);
			}

			return (T)javaObj;

			//--
		}

		throw new IllegalArgumentException("Can't map " + bsonValue.getClass() + " to " + cls.getName());
	}

	public Object javaToBson(Object javaValue) {
		return javaToBson(javaValue, null);
	}

	public Object javaToBson(Object javaValue, PropertyInfo pi) {
		if (javaValue == null)
			return null;

		Class<?> cls = javaValue.getClass();

		@SuppressWarnings("unchecked")
		TypeConverter<Object> tc = (TypeConverter<Object>)typeConverters.get(cls);
		if (tc != null)
			return tc.javaToBson(javaValue);

		if (cls.isEnum())
			return ((Enum<?>)javaValue).name();

		if ((javaValue instanceof List) || cls.isArray())
			synchronized (mobjects) {
				MList mlist = (MList)mobjects.get(javaValue);
				if (mlist == null) {
					mlist = new MList(cls.isArray(), javaValue, this, pi == null ? null : pi.itemCls);//TODO nao usar null, se pi == null
					mobjects.put(javaValue, mlist);
				}
				return mlist;
			}

		if (javaValue instanceof BSONObject)
			return javaValue;

		TypeInfo typeInfo = typeInfos.get(cls);
		if (typeInfo == null)
			try {
				typeInfo = new TypeInfo(cls);
				typeInfos.put(cls, typeInfo);
			} catch (Throwable e) {
				throw new IllegalArgumentException("Can't map " + cls.getName() + " to MObject", e);
			}

		synchronized (mobjects) {
			MObject mobj = mobjects.get(javaValue);
			if (mobj == null) {
				mobj = new MObject(typeInfo, this, javaValue);
				mobjects.put(javaValue, mobj);
			}
			return mobj;
		}
	}

	public void unmapAll() {
		synchronized (mobjects) {
			// avoid concurrent mod
			MObject[] all = mobjects.values().toArray(new MObject[mobjects.size()]);
			for (MObject mobj: all)
				mobj.unmap();
			mobjects.clear();
		}
	}

}

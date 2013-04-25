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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.bson.types.BasicBSONList;
import org.judison.mongodm.PropertyInfo.Type;
import org.judison.mongodm.converter.DBObjectConverter;
import org.judison.mongodm.converter.DateConverter;
import org.judison.mongodm.converter.NumberConverter;
import org.judison.mongodm.converter.PassThruConverter;
import org.judison.mongodm.converter.TypeConverter;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public final class Mapper {

	private Mapper() {}

	private static Map<Class<?>, TypeConverter> typeConverters = new HashMap<Class<?>, TypeConverter>();

	public static <T> void registerTypeConverter(Class<T> cls, TypeConverter converter) {
		typeConverters.put(cls, converter);
	}

	public static boolean hasTypeConverterFor(Class<?> cls) {
		return typeConverters.containsKey(cls);
	}

	static {
		DateConverter dateConverter = DateConverter.INSTANCE;
		registerTypeConverter(Date.class, dateConverter);
		registerTypeConverter(java.sql.Date.class, dateConverter);
		registerTypeConverter(java.sql.Time.class, dateConverter);
		registerTypeConverter(java.sql.Timestamp.class, dateConverter);

		NumberConverter numberConverter = NumberConverter.INSTANCE;
		registerTypeConverter(Byte.class, numberConverter);
		registerTypeConverter(Short.class, numberConverter);
		registerTypeConverter(Integer.class, numberConverter);
		registerTypeConverter(Long.class, numberConverter);
		registerTypeConverter(Float.class, numberConverter);
		registerTypeConverter(Double.class, numberConverter);
		registerTypeConverter(BigInteger.class, numberConverter);
		registerTypeConverter(BigDecimal.class, numberConverter);

		for (Class<?> cls: PassThruConverter.CLASSES)
			registerTypeConverter(cls, PassThruConverter.INSTANCE);

		DBObjectConverter dbObjectConverter = DBObjectConverter.INSTANCE;
		registerTypeConverter(DBObject.class, dbObjectConverter);
		registerTypeConverter(BasicDBObject.class, dbObjectConverter);
		registerTypeConverter(BasicDBList.class, dbObjectConverter);
		registerTypeConverter(BasicBSONList.class, dbObjectConverter);
	}

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

	public static void load(Object object, DBObject data) {
		loadEntity(object, data);
	}

	private static void loadEntity(Object object, DBObject data) {
		Class<?> cls = object.getClass();
		TypeInfo typeInfo = getTypeInfo(cls);
		if (!typeInfo.isEntity)
			throw new IllegalArgumentException("Class " + cls.getName() + " is not an @Entity");

		for (PropertyInfo fi: typeInfo.properties) {
			setField(fi, object, data.get(fi.name));
		}
	}

	private static void loadEmbedded(Object object, DBObject data) {
		Class<?> cls = object.getClass();
		TypeInfo typeInfo = getTypeInfo(cls);
		if (!typeInfo.isEmbedded)
			throw new IllegalArgumentException("Class " + cls.getName() + " can't be @Embedded");

		for (PropertyInfo fi: typeInfo.properties) {
			setField(fi, object, data.get(fi.name));
		}
	}

	public static void save(Object object, DBObject data) {
		Class<?> cls = object.getClass();
		TypeInfo typeInfo = getTypeInfo(cls);

		if (typeInfo.isEntity)
			saveEntity(object, data);
		else if (typeInfo.isEmbedded)
			saveEmbedded(object, data);
		else
			throw new IllegalArgumentException("Class " + cls.getName() + " is not an @Entity or @Embedded");
	}

	static void saveEntity(Object object, DBObject data) {
		Class<?> cls = object.getClass();
		TypeInfo typeInfo = getTypeInfo(cls);

		if (!typeInfo.isEntity)
			throw new IllegalArgumentException("Class " + cls.getName() + " is not an @Entity");

		for (PropertyInfo fi: typeInfo.properties) {
			getField(fi, object, data);
		}

	}

	static void saveEmbedded(Object object, DBObject data) {
		Class<?> cls = object.getClass();
		TypeInfo typeInfo = getTypeInfo(cls);

		if (!typeInfo.isEmbedded)
			throw new IllegalArgumentException("Class " + cls.getName() + " is not an @Embedded");

		for (PropertyInfo fi: typeInfo.properties) {
			getField(fi, object, data);
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static Object bsonToJava(Class<?> fieldClass, Object value, Object previous) throws IllegalAccessException {
		if (fieldClass == String.class)
			return value.toString();

		// Tem TypeConverter registrado ???
		TypeConverter converter = typeConverters.get(fieldClass);
		if (converter != null)
			return converter.bsonToJava(fieldClass, value);

		// É um enum ??
		if (fieldClass.isEnum())
			return Enum.valueOf((Class<? extends Enum>)fieldClass, value.toString());

		// Tem um TypeInfo pra classe esperada?
		TypeInfo typeInfo = getTypeInfo(fieldClass, false);
		if (typeInfo != null) {
			if (value instanceof DBObject) {
				DBObject subData = (DBObject)value;
				Object subObj = previous;
				if (subObj == null)
					try {
						subObj = typeInfo.constructor.newInstance();
					} catch (InstantiationException e) {
						throw new RuntimeException(e);
					} catch (InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				loadEmbedded(subObj, subData);
				return subObj;
			} else
				throw new IllegalArgumentException("Invalid value for " + fieldClass.getName());
		}

		throw new IllegalArgumentException("Cant convert " + value.getClass() + " to " + fieldClass);

	}

	static void setField(PropertyInfo fi, Object object, Object value) {
		try {
			Field f = fi.field;

			if (value != null) {
				Class<?> fieldClass = f.getType();

				if (fieldClass == Integer.TYPE)
					f.setInt(object, ((Number)NumberConverter.INSTANCE.bsonToJava(Integer.class, value)).intValue());
				else if (fieldClass == Long.TYPE)
					f.setLong(object, ((Number)NumberConverter.INSTANCE.bsonToJava(Long.class, value)).longValue());
				else if (fieldClass == Byte.TYPE)
					f.setByte(object, ((Number)NumberConverter.INSTANCE.bsonToJava(Byte.class, value)).byteValue());
				else if (fieldClass == Short.TYPE)
					f.setShort(object, ((Number)NumberConverter.INSTANCE.bsonToJava(Short.class, value)).shortValue());
				else if (fieldClass == Double.TYPE)
					f.setDouble(object, ((Number)NumberConverter.INSTANCE.bsonToJava(Double.class, value)).doubleValue());
				else if (fieldClass == Float.TYPE)
					f.setFloat(object, ((Number)NumberConverter.INSTANCE.bsonToJava(Float.class, value)).byteValue());
				else if (fieldClass == Boolean.TYPE)
					f.setBoolean(object, ((Boolean)PassThruConverter.INSTANCE.bsonToJava(Boolean.class, value)).booleanValue());
				else if (fieldClass == Character.TYPE)
					f.setChar(object, value.toString().charAt(0)); // certo?
				else if (fi.type == Type.ARRAY) {
					f.set(object, arrayBsonToJava(fi.cls, fi.itemCls, value));
				} else
					f.set(object, bsonToJava(fieldClass, value, f.get(object)));

			} else {
				setNull(f, object);
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static void setNull(Field f, Object object) throws IllegalArgumentException, IllegalAccessException {
		Class<?> fieldClass = f.getType();
		if (fieldClass == Integer.TYPE)
			f.setInt(object, 0);
		else if (fieldClass == Long.TYPE)
			f.setLong(object, 0l);
		else if (fieldClass == Byte.TYPE)
			f.setByte(object, (byte)0);
		else if (fieldClass == Short.TYPE)
			f.setShort(object, (short)0);
		else if (fieldClass == Double.TYPE)
			f.setDouble(object, 0);
		else if (fieldClass == Float.TYPE)
			f.setFloat(object, 0);
		else if (fieldClass == Boolean.TYPE)
			f.setBoolean(object, false);
		else if (fieldClass == Character.TYPE)
			f.setChar(object, (char)0);
		else
			f.set(object, null);
	}

	static Object javaToBson(Class<?> fieldClass, Object value, Object previous) {

		if (fieldClass == Integer.TYPE)
			return NumberConverter.INSTANCE.javaToBson(Integer.class, value);
		if (fieldClass == Long.TYPE)
			return NumberConverter.INSTANCE.javaToBson(Long.class, value);
		if (fieldClass == Byte.TYPE)
			return NumberConverter.INSTANCE.javaToBson(Byte.class, value);
		if (fieldClass == Short.TYPE)
			return NumberConverter.INSTANCE.javaToBson(Short.class, value);
		if (fieldClass == Double.TYPE)
			return NumberConverter.INSTANCE.javaToBson(Double.class, value);
		if (fieldClass == Float.TYPE)
			return NumberConverter.INSTANCE.javaToBson(Float.class, value);
		if (fieldClass == Boolean.TYPE)
			return PassThruConverter.INSTANCE.javaToBson(Boolean.class, value);
		if (fieldClass == Character.TYPE)
			return value.toString().charAt(0); // certo?
		if (fieldClass == String.class)
			return value.toString();

		TypeConverter converter = typeConverters.get(fieldClass);
		if (converter != null) // Tem TypeConverter registrado
			return converter.javaToBson(fieldClass, value);

		if (fieldClass.isEnum()) // É um enum
			return ((Enum<?>)value).name();

		TypeInfo typeInfo = getTypeInfo(fieldClass, false);
		if (typeInfo != null) {
			DBObject subData;
			if (previous instanceof DBObject)
				subData = (DBObject)previous;
			else
				subData = new BasicDBObject();

			Object subObj = value;

			saveEmbedded(subObj, subData);

			return subData;
		}

		throw new IllegalArgumentException("Cant convert " + value.getClass() + " to BSON");
	}

	private static void getField(PropertyInfo prop, Object object, DBObject data) {
		try {
			Field f = prop.field;
			String name = prop.name;

			//System.out.println("getField " + f.getType().getSimpleName() + " " + f.getDeclaringClass().getSimpleName() + "." + f.getName());

			Object value = f.get(object);

			if (value != null) {
				Class<?> fieldClass = f.getType();

				if (prop.type == Type.ARRAY)
					data.put(name, arrayJavaToBson(prop.cls, prop.itemCls, value));
				else
					data.put(name, javaToBson(fieldClass, value, data.get(name)));

			} else {
				data.removeField(name);
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static Object arrayBsonToJava(Class<?> cls, Class<?> itemCls, Object value) throws IllegalAccessException {
		if (!(value instanceof BasicBSONList))
			throw new IllegalArgumentException();

		BasicBSONList blist = (BasicBSONList)value;

		// Trata Array's
		if (cls.isArray()) {

			if (itemCls == int.class) {
				int[] array = new int[blist.size()];
				for (int i = 0; i < array.length; i++)
					array[i] = ((Integer)bsonToJava(Integer.class, blist.get(i), null)).intValue();
				return array;
			} else if (itemCls == short.class) {
				short[] array = new short[blist.size()];
				for (int i = 0; i < array.length; i++)
					array[i] = ((Short)bsonToJava(Short.class, blist.get(i), null)).shortValue();
				return array;
			} else if (itemCls == long.class) {
				long[] array = new long[blist.size()];
				for (int i = 0; i < array.length; i++)
					array[i] = ((Long)bsonToJava(Long.class, blist.get(i), null)).longValue();
				return array;
			} else if (itemCls == float.class) {
				float[] array = new float[blist.size()];
				for (int i = 0; i < array.length; i++)
					array[i] = ((Float)bsonToJava(Float.class, blist.get(i), null)).floatValue();
				return array;
			} else if (itemCls == double.class) {
				double[] array = new double[blist.size()];
				for (int i = 0; i < array.length; i++)
					array[i] = ((Double)bsonToJava(Double.class, blist.get(i), null)).doubleValue();
				return array;
			} else if (itemCls == boolean.class) {
				boolean[] array = new boolean[blist.size()];
				for (int i = 0; i < array.length; i++)
					array[i] = ((Boolean)bsonToJava(Boolean.class, blist.get(i), null)).booleanValue();
				return array;
			} else if (itemCls == char.class) {
				char[] array = new char[blist.size()];
				for (int i = 0; i < array.length; i++)
					array[i] = ((Character)bsonToJava(Character.class, blist.get(i), null)).charValue();
				return array;
			}

			Object[] array = (Object[])Array.newInstance(itemCls, blist.size());
			for (int i = 0; i < array.length; i++)
				array[i] = bsonToJava(itemCls, blist.get(i), null);
			return array;
		}

		// Trata List 
		List<Object> list;

		if (cls == ArrayList.class)
			list = new ArrayList<Object>(blist.size());
		else if (cls == LinkedList.class)
			list = new LinkedList<Object>();
		else if (cls == Vector.class)
			list = new Vector<Object>();
		else
			throw new IllegalArgumentException();

		for (Object obj: blist)
			list.add(bsonToJava(itemCls, obj, null));

		return list;
	}

	private static Object arrayJavaToBson(Class<?> cls, Class<?> itemCls, Object value) {
		if (value.getClass().isArray()) {
			BasicBSONList blist = new BasicBSONList();

			if (itemCls == int.class) {
				int[] array = (int[])value;
				for (int item: array)
					blist.add(item);
			} else if (itemCls == long.class) {
				long[] array = (long[])value;
				for (long item: array)
					blist.add(item);
			} else {
				Object[] array = (Object[])value;
				for (Object item: array)
					blist.add(javaToBson(itemCls, item, null));
			}

			return blist;
		}

		if (!(value instanceof Collection<?>))
			throw new IllegalArgumentException();

		BasicBSONList blist = new BasicBSONList();

		Collection<?> coll = (Collection<?>)value;
		for (Object item: coll)
			blist.add(javaToBson(itemCls, item, null));

		return blist;
	}

}

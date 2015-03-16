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
package org.judison.mongodm.converter;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class NumberConverter<T extends Number> extends TypeConverter<T> {

	public static final NumberConverter<Integer> INTEGER = new NumberConverter<Integer>(Integer.class);
	public static final NumberConverter<Long> LONG = new NumberConverter<Long>(Long.class);
	public static final NumberConverter<Byte> BYTE = new NumberConverter<Byte>(Byte.class);
	public static final NumberConverter<Short> SHORT = new NumberConverter<Short>(Short.class);
	public static final NumberConverter<Float> FLOAT = new NumberConverter<Float>(Float.class);
	public static final NumberConverter<Double> DOUBLE = new NumberConverter<Double>(Double.class);
	public static final NumberConverter<BigInteger> BIG_INTEGER = new NumberConverter<BigInteger>(BigInteger.class);
	public static final NumberConverter<BigDecimal> BIG_DECIMAL = new NumberConverter<BigDecimal>(BigDecimal.class);

	private final Class<? extends Number> cls;

	private NumberConverter(Class<T> cls) {
		this.cls = cls;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T bsonToJava(Object bsonValue) {
		if (cls == Integer.class) {
			if (bsonValue instanceof Integer)
				return (T)bsonValue;
			if (bsonValue instanceof Number)
				return (T)Integer.valueOf(((Number)bsonValue).intValue());
			return (T)Integer.valueOf(bsonValue.toString());
		} else if (cls == Long.class) {
			if (bsonValue instanceof Long)
				return (T)bsonValue;
			if (bsonValue instanceof Number)
				return (T)Long.valueOf(((Number)bsonValue).longValue());
			return (T)Long.valueOf(bsonValue.toString());
		} else if (cls == Byte.class) {
			if (bsonValue instanceof Byte)
				return (T)bsonValue;
			if (bsonValue instanceof Number)
				return (T)Byte.valueOf(((Number)bsonValue).byteValue());
			return (T)Byte.valueOf(bsonValue.toString());
		} else if (cls == Short.class) {
			if (bsonValue instanceof Short)
				return (T)bsonValue;
			if (bsonValue instanceof Number)
				return (T)Short.valueOf(((Number)bsonValue).shortValue());
			return (T)Short.valueOf(bsonValue.toString());
		} else if (cls == Float.class) {
			if (bsonValue instanceof Float)
				return (T)bsonValue;
			if (bsonValue instanceof Number)
				return (T)Float.valueOf(((Number)bsonValue).floatValue());
			return (T)Float.valueOf(bsonValue.toString());
		} else if (cls == Double.class) {
			if (bsonValue instanceof Double)
				return (T)bsonValue;
			if (bsonValue instanceof Number)
				return (T)Double.valueOf(((Number)bsonValue).doubleValue());
			return (T)Double.valueOf(bsonValue.toString());
		} else if (cls == BigInteger.class) {
			if (bsonValue instanceof BigInteger)
				return (T)bsonValue;
			if (bsonValue instanceof BigDecimal)
				return (T)((BigDecimal)bsonValue).toBigInteger();
			if (bsonValue instanceof Number)
				return (T)BigInteger.valueOf(((Number)bsonValue).longValue());
			return (T)new BigInteger(bsonValue.toString());
		} else if (cls == BigDecimal.class) {
			if (bsonValue instanceof BigDecimal)
				return (T)bsonValue;
			if (bsonValue instanceof BigInteger)
				return (T)new BigDecimal((BigInteger)bsonValue);
			if (bsonValue instanceof Number)
				return (T)BigDecimal.valueOf(((Number)bsonValue).doubleValue());
			return (T)new BigDecimal(bsonValue.toString());
		}
		throw new IllegalArgumentException();
	}

	@Override
	public Object javaToBson(T javaValue) {
		if (cls == Integer.class || cls == Short.class || cls == Byte.class) {
			if (javaValue instanceof Integer)
				return javaValue;
			if (javaValue instanceof Number)
				return Integer.valueOf(((Number)javaValue).intValue());
			return Integer.valueOf(javaValue.toString());
		} else if (cls == Long.class) {
			if (javaValue instanceof Long)
				return javaValue;
			if (javaValue instanceof Number)
				return Long.valueOf(((Number)javaValue).longValue());
			return Long.valueOf(javaValue.toString());
		} else if (cls == Double.class || cls == Float.class) {
			if (javaValue instanceof Double)
				return javaValue;
			if (javaValue instanceof Number)
				return Double.valueOf(((Number)javaValue).doubleValue());
			return Double.valueOf(javaValue.toString());
		} else if (cls == BigInteger.class) {
			if (javaValue instanceof BigInteger)
				return javaValue.toString();
			if (javaValue instanceof BigDecimal)
				return ((BigDecimal)javaValue).toBigInteger().toString();
			if (javaValue instanceof Number)
				return Long.toString(((Number)javaValue).longValue());
			return new BigInteger(javaValue.toString()).toString();
		} else if (cls == BigDecimal.class) {
			if (javaValue instanceof BigDecimal)
				return javaValue.toString();
			if (javaValue instanceof BigInteger)
				return new BigDecimal((BigInteger)javaValue).toString();
			if (javaValue instanceof Number)
				return BigDecimal.valueOf(((Number)javaValue).doubleValue()).toString();
			return new BigDecimal(javaValue.toString()).toString();
		}
		throw new IllegalArgumentException();
	}

}

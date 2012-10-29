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
package org.judison.mongodm.converter;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class NumberConverter extends TypeConverter {

	public static final NumberConverter INSTANCE = new NumberConverter();

	private NumberConverter() {}

	@Override
	public Object bsonToJava(java.lang.Class<?> cls, Object bsonValue) {
		if (cls == Integer.class) {
			if (bsonValue instanceof Integer)
				return bsonValue;
			if (bsonValue instanceof Number)
				return Integer.valueOf(((Number)bsonValue).intValue());
			return Integer.valueOf(bsonValue.toString());
		} else if (cls == Long.class) {
			if (bsonValue instanceof Long)
				return bsonValue;
			if (bsonValue instanceof Number)
				return Long.valueOf(((Number)bsonValue).longValue());
			return Long.valueOf(bsonValue.toString());
		} else if (cls == Byte.class) {
			if (bsonValue instanceof Byte)
				return bsonValue;
			if (bsonValue instanceof Number)
				return Byte.valueOf(((Number)bsonValue).byteValue());
			return Byte.valueOf(bsonValue.toString());
		} else if (cls == Short.class) {
			if (bsonValue instanceof Short)
				return bsonValue;
			if (bsonValue instanceof Number)
				return Short.valueOf(((Number)bsonValue).shortValue());
			return Short.valueOf(bsonValue.toString());
		} else if (cls == Float.class) {
			if (bsonValue instanceof Float)
				return bsonValue;
			if (bsonValue instanceof Number)
				return Float.valueOf(((Number)bsonValue).floatValue());
			return Float.valueOf(bsonValue.toString());
		} else if (cls == Double.class) {
			if (bsonValue instanceof Double)
				return bsonValue;
			if (bsonValue instanceof Number)
				return Double.valueOf(((Number)bsonValue).doubleValue());
			return Double.valueOf(bsonValue.toString());
		} else if (cls == BigInteger.class) {
			if (bsonValue instanceof BigInteger)
				return bsonValue;
			if (bsonValue instanceof BigDecimal)
				return ((BigDecimal)bsonValue).toBigInteger();
			if (bsonValue instanceof Number)
				return BigInteger.valueOf(((Number)bsonValue).longValue());
			return new BigInteger(bsonValue.toString());
		} else if (cls == BigDecimal.class) {
			if (bsonValue instanceof BigDecimal)
				return bsonValue;
			if (bsonValue instanceof BigInteger)
				return new BigDecimal((BigInteger)bsonValue);
			if (bsonValue instanceof Number)
				return BigDecimal.valueOf(((Number)bsonValue).doubleValue());
			return new BigDecimal(bsonValue.toString());
		}
		throw new IllegalArgumentException();
	}

	@Override
	public Object javaToBson(Class<?> cls, Object javaValue) {
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

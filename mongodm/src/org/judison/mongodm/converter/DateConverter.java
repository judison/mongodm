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

import java.util.Date;

public class DateConverter extends TypeConverter {

	public static final DateConverter INSTANCE = new DateConverter();

	private DateConverter() {}

	@Override
	public Object bsonToJava(Class<?> javaClass, Object bsonValue) {
		if (javaClass == Date.class) {
			if (bsonValue.getClass() == Date.class)
				return bsonValue;
			if (bsonValue instanceof Date)
				return new Date(((Date)bsonValue).getTime());
			if (bsonValue instanceof Number)
				return new Date(((Number)bsonValue).longValue());
			throw new IllegalArgumentException();
		} else if (javaClass == java.sql.Date.class) {
			if (bsonValue.getClass() == java.sql.Date.class)
				return bsonValue;
			if (bsonValue instanceof Date)
				return new java.sql.Date(((Date)bsonValue).getTime());
			if (bsonValue instanceof Number)
				return new java.sql.Date(((Number)bsonValue).longValue());
			throw new IllegalArgumentException();
		} else if (javaClass == java.sql.Time.class) {
			if (bsonValue.getClass() == java.sql.Time.class)
				return bsonValue;
			if (bsonValue instanceof Date)
				return new java.sql.Time(((Date)bsonValue).getTime());
			if (bsonValue instanceof Number)
				return new java.sql.Time(((Number)bsonValue).longValue());
			throw new IllegalArgumentException();
		} else if (javaClass == java.sql.Timestamp.class) {
			if (bsonValue.getClass() == java.sql.Timestamp.class)
				return bsonValue;
			if (bsonValue instanceof Date)
				return new java.sql.Timestamp(((Date)bsonValue).getTime());
			if (bsonValue instanceof Number)
				return new java.sql.Timestamp(((Number)bsonValue).longValue());
			throw new IllegalArgumentException();
		}

		throw new IllegalArgumentException();
	}

	@Override
	public Object javaToBson(Class<?> javaClass, Object javaValue) {
		if (javaClass == Date.class || javaClass == java.sql.Date.class || javaClass == java.sql.Time.class || javaClass == java.sql.Timestamp.class) {
			if (javaValue.getClass() == Date.class)
				return javaValue;
			if (javaValue instanceof Date)
				return new Date(((Date)javaValue).getTime());
			if (javaValue instanceof Number)
				return new Date(((Number)javaValue).longValue());
			throw new IllegalArgumentException();
		}
		throw new IllegalArgumentException();
	}

}

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

import org.bson.BSONObject;
import org.bson.types.BasicBSONList;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public final class DBObjectConverter extends TypeConverter {

	public static final DBObjectConverter INSTANCE = new DBObjectConverter();

	private DBObjectConverter() {}

	@Override
	public Object bsonToJava(java.lang.Class<?> cls, Object bsonValue) {
		if (cls == BasicDBObject.class || cls == DBObject.class) {
			if (bsonValue instanceof BasicDBObject)
				return bsonValue;
			if (bsonValue instanceof BSONObject) {
				BasicDBObject dbo = new BasicDBObject();
				dbo.putAll((BSONObject)bsonValue);
				return dbo;
			}
			throw new IllegalArgumentException();
		} else if (cls == BasicDBList.class || cls == BasicBSONList.class) {
			if (bsonValue instanceof BasicDBList)
				return bsonValue;
			if (bsonValue instanceof BasicBSONList) {
				BasicDBList dbl = new BasicDBList();
				dbl.addAll((BasicBSONList)bsonValue);
				return dbl;
			}
			throw new IllegalArgumentException();
		}
		throw new IllegalArgumentException();
	}

	@Override
	public Object javaToBson(Class<?> cls, Object javaValue) {
		if (cls == BasicDBObject.class || cls == DBObject.class) {
			if (javaValue instanceof BasicDBObject)
				return javaValue;
			if (javaValue instanceof DBObject)
				return javaValue;
			throw new IllegalArgumentException();
		} else if (cls == BasicDBList.class || cls == BasicBSONList.class) {
			if (javaValue instanceof BasicDBList)
				return javaValue;
			if (javaValue instanceof BasicBSONList)
				return javaValue;
			throw new IllegalArgumentException();
		}
		throw new IllegalArgumentException();
	}

}

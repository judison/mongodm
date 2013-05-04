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
package org.judison.mongodm.converter;

import org.bson.BSONObject;
import org.judison.mongodm.MList;
import org.judison.mongodm.MObject;

import com.mongodb.BasicDBList;

public final class MObjectConverter extends TypeConverter {

	public static final MObjectConverter INSTANCE = new MObjectConverter();

	private MObjectConverter() {}

	@Override
	public Object bsonToJava(java.lang.Class<?> cls, Object bsonValue) {
		if (cls == MObject.class) {
			if (bsonValue instanceof MObject)
				return bsonValue;
			if (bsonValue instanceof BSONObject)
				return MObject.deepCopy((BSONObject)bsonValue);
			throw new IllegalArgumentException();
		}
		if (cls == MList.class) {
			if (bsonValue instanceof MObject)
				return bsonValue;
			if (bsonValue instanceof BasicDBList)
				return MObject.deepCopy((BasicDBList)bsonValue);
			throw new IllegalArgumentException();
		}
		throw new IllegalArgumentException();
	}

	@Override
	public Object javaToBson(Class<?> cls, Object javaValue) {
		if (cls == MObject.class)
			return javaValue;
		if (cls == MList.class)
			return javaValue;
		throw new IllegalArgumentException();
	}

}

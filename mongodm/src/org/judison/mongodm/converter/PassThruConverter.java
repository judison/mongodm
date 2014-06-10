/*
 * Copyright (c) 2012-2014, Judison Oliveira Gil Filho <judison@gmail.com>
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

import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.judison.mongodm.MObject;

public final class PassThruConverter<T> extends TypeConverter<T> {

	public static final PassThruConverter<Object> INSTANCE = new PassThruConverter<Object>();

	public static final Class<?>[] CLASSES = {
		String.class,
		ObjectId.class,
		Character.class,
		Character.TYPE,
		Boolean.class,
		Boolean.TYPE,
		UUID.class,
		byte[].class,
		Pattern.class,
		Date.class,
		MObject.class,
	};

	private PassThruConverter() {}

	@SuppressWarnings("unchecked")
	@Override
	public T bsonToJava(Object bsonValue) {
		return (T)bsonValue;
	}

	@Override
	public Object javaToBson(T javaValue) {
		return javaValue;
	}

}

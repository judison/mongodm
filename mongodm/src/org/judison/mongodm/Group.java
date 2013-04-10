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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Represents an $group aggregate operator.<br>
 * 
 * @author judison
 * @see {@link Pipeline#group(Group)}
 */
public class Group {

	private BasicDBObject group = new BasicDBObject();

	/**
	 * Constructs an {@link Group} with { "_id": {<b>name</b>: <b>value</b>} }
	 */
	public Group(String name, Object value) {
		compositeId(name, value);
	}

	/**
	 * Constructs an {@link Group} with { "_id": <b>id</b> }
	 */
	public Group(Object id) {
		put("_id", id);
	}

	public Group put(String name, Object value) {
		group.put(name, value);
		return this;
	}

	public Group compositeId(String name, Object value) {
		DBObject id = null;
		Object _id = group.get("_id");
		if (_id instanceof DBObject)
			id = (DBObject)_id;
		else
			id = new BasicDBObject();

		id.put(name, value);

		return put("_id", id);
	}

	public Group count(String name) {
		return put(name, new BasicDBObject("$sum", 1));
	}

	public Group sum(String name, Object value) {
		return put(name, new BasicDBObject("$sum", value));
	}

	public Group push(String name, Object value) {
		return put(name, new BasicDBObject("$push", value));
	}

	public Group avg(String name, Object value) {
		return put(name, new BasicDBObject("$avg", value));
	}

	public Group min(String name, Object value) {
		return put(name, new BasicDBObject("$min", value));
	}

	public Group max(String name, Object value) {
		return put(name, new BasicDBObject("$max", value));
	}

	public Group last(String name, Object value) {
		return put(name, new BasicDBObject("$last", value));
	}

	public Group first(String name, Object value) {
		return put(name, new BasicDBObject("$first", value));
	}

	public Group addToSet(String name, Object value) {
		return put(name, new BasicDBObject("$addToSet", value));
	}

	public DBObject toDBObject() {
		return group;
	}

	@Override
	public String toString() {
		return group.toString();
	}

}

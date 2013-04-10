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

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Pipeline {

	ArrayList<DBObject> operators = new ArrayList<DBObject>();

	public Pipeline() {}

	public Pipeline project(String... fields) {
		operators.add(new BasicDBObject("$project", IndexInfo.parseFields(fields)));
		return this;
	}

	public Pipeline project(Projection projection) {
		operators.add(new BasicDBObject("$project", projection.toDBObject()));
		return this;
	}

	public Pipeline project(DBObject projection) {
		operators.add(new BasicDBObject("$project", projection));
		return this;
	}

	public Pipeline match(Query query) {
		operators.add(new BasicDBObject("$match", query.toDBObject()));
		return this;
	}

	public Pipeline match(DBObject query) {
		operators.add(new BasicDBObject("$match", query));
		return this;
	}

	public Pipeline limit(int limit) {
		if (limit <= 0)
			throw new IllegalArgumentException();
		operators.add(new BasicDBObject("$limit", limit));
		return this;
	}

	public Pipeline skip(int skip) {
		if (skip < 0)
			throw new IllegalArgumentException();
		if (skip == 0)
			return this;
		operators.add(new BasicDBObject("$skip", skip));
		return this;
	}

	public Pipeline unwind(String unwind) {
		if (unwind == null || unwind.isEmpty())
			throw new IllegalArgumentException();
		if (!unwind.startsWith("$"))
			unwind = '$' + unwind;
		operators.add(new BasicDBObject("$unwind", unwind));
		return this;
	}

	public Pipeline group(Group group) {
		operators.add(new BasicDBObject("$group", group.toDBObject()));
		return this;
	}

	public Pipeline group(DBObject group) {
		operators.add(new BasicDBObject("$group", group));
		return this;
	}

	public Pipeline sort(String... fields) {
		if (fields == null || fields.length == 0)
			throw new IllegalArgumentException();
		operators.add(new BasicDBObject("$sort", IndexInfo.parseFields(fields)));
		return this;
	}

	public Pipeline sort(DBObject sort) {
		operators.add(new BasicDBObject("$sort", sort));
		return this;
	}

	public List<DBObject> getOperators() {
		return operators;
	}

}

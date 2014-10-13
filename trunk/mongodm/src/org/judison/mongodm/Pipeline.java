/*
 * Copyright (c) 2013-2014, Judison Oliveira Gil Filho <judison@gmail.com>
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

public class Pipeline {

	MList operators = new MList();

	public Pipeline() {}

	public Pipeline project(String... fields) {
		operators.add(new MObject("$project", IndexInfo.parseFields(fields)));
		return this;
	}

	public Pipeline project(Projection projection) {
		operators.add(new MObject("$project", projection.toMObject()));
		return this;
	}

	public Pipeline project(MObject projection) {
		operators.add(new MObject("$project", projection));
		return this;
	}

	public Pipeline match(Query query) {
		operators.add(new MObject("$match", query.toMObject()));
		return this;
	}

	public Pipeline match(MObject query) {
		operators.add(new MObject("$match", query));
		return this;
	}

	public Pipeline limit(int limit) {
		if (limit <= 0)
			throw new IllegalArgumentException();
		operators.add(new MObject("$limit", limit));
		return this;
	}

	public Pipeline skip(int skip) {
		if (skip < 0)
			throw new IllegalArgumentException();
		if (skip == 0)
			return this;
		operators.add(new MObject("$skip", skip));
		return this;
	}

	public Pipeline unwind(String unwind) {
		if (unwind == null || unwind.isEmpty())
			throw new IllegalArgumentException();
		if (!unwind.startsWith("$"))
			unwind = '$' + unwind;
		operators.add(new MObject("$unwind", unwind));
		return this;
	}

	public Pipeline group(Group group) {
		operators.add(new MObject("$group", group.toMObject()));
		return this;
	}

	public Pipeline group(MObject group) {
		operators.add(new MObject("$group", group));
		return this;
	}

	public Pipeline sort(String... fields) {
		if (fields == null || fields.length == 0)
			throw new IllegalArgumentException();
		operators.add(new MObject("$sort", IndexInfo.parseFields(fields)));
		return this;
	}

	public Pipeline sort(MObject sort) {
		operators.add(new MObject("$sort", sort));
		return this;
	}

	public MList getOperators() {
		return operators;
	}
	
	@Override
	public String toString() {
		return operators.toString();
	}
	

}

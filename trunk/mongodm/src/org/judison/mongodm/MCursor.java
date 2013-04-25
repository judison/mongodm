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

import java.io.Closeable;
import java.util.Iterator;

import com.mongodb.DBCursor;

public class MCursor<T> implements Iterable<T>, Iterator<T>, Closeable {

	private final MCollection<?> coll;
	private final DBCursor dbCursor;
	private final boolean dbObj;
	private final Class<T> cls;
	private T last = null;

	MCursor(MCollection<?> coll, Class<T> cls, DBCursor dbCursor, boolean dbObj) {
		this.coll = coll;
		this.dbCursor = dbCursor;
		this.cls = cls;
		coll.mdb.onCursorCreated(this, cls);
		this.dbObj = dbObj;
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	@Override
	public void close() {
		dbCursor.close();
		coll.mdb.onCursorClosed(this);
	}

	@Override
	public boolean hasNext() {
		return dbCursor.hasNext();
	}

	@Override
	@SuppressWarnings("unchecked")
	public T next() {
		MObject data = (MObject)dbCursor.next();
		if (dbObj)
			last = (T)data;
		else
			last = (T)coll.mapLoad(data);
		return last;
	}

	public T getLast() {
		return last;
	}

	@Override
	public Iterator<T> iterator() {
		return copy();
	}

	private MCursor<T> copy() {
		MCursor<T> copy = new MCursor<T>(coll, cls, dbCursor.copy(), dbObj);
		return copy;
	}

	public MCursor<T> sort(String... fields) {
		MObject orderBy = IndexInfo.parseFields(fields);
		dbCursor.sort(orderBy);
		return this;
	}

	public MCursor<T> limit(int n) {
		dbCursor.limit(n);
		return this;
	}

	public MCursor<T> skip(int n) {
		dbCursor.skip(n);
		return this;
	}

	public MCursor<T> skipAndLimit(int skip, int limit) {
		dbCursor.skip(skip);
		dbCursor.limit(limit);
		return this;
	}

	/**
	 * @return The number of objects matching the query <b>NOT TAKING</b> limit/skip into consideration
	 */
	public int count() {
		return dbCursor.count();
	}

	/**
	 * @return The number of objects matching the query <b>taking</b> limit/skip into consideration
	 */
	public int size() {
		return dbCursor.size();
	}

	@Override
	@Deprecated
	public void remove() {
		throw new UnsupportedOperationException("Can't remove from a MCursor");
	}

}

/*
 * Copyright (c) 2013-2015, Judison Oliveira Gil Filho <judison@gmail.com>
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

public class Update {

	private MObject update = new MObject();

	public Update() {}

	public Update put(String name, Object value) {
		update.put(name, value);
		return this;
	}

	private void putIn(String operator, String field, Object value) {
		MObject sub = (MObject)update.get(operator);
		if (sub == null) {
			sub = new MObject();
			update.put(operator, sub);
		}
		sub.put(field, value);
	}

	private Object getIn(String operator, String field) {
		MObject sub = (MObject)update.get(operator);
		if (sub == null)
			return null;
		return sub.get(field);
	}

	public Update set(String field, Object value) {
		putIn("$set", field, value);
		return this;
	}

	/**
	 * The $setOnInsert operator assigns values to fields during an upsert only when using the upsert option to the update() operation performs an insert.
	 */
	public Update setOnInsert(String field, Object value) {
		putIn("$setOnInsert", field, value);
		return this;
	}

	public Update unset(String... fields) {
		for (String field: fields)
			putIn("$unset", field, 1);
		return this;
	}

	public Update push(String array, Object... values) {
		for (Object value: values)
			_each("$push", array, value);
		return this;
	}

	public Update pull(String array, Object... values) {
		for (Object value: values)
			_each("$pull", array, value);
		return this;
	}

	public Update addToSet(String array, Object... values) {
		for (Object value: values)
			_each("$addToSet", array, value);
		return this;
	}

	private void _each(String operator, String array, Object value) {
		Object obj = getIn(operator, array);
		if (obj == null)
			putIn(operator, array, value);
		else if (obj instanceof MObject && ((MObject)obj).containsField("$each")) {
			MObject eh = (MObject)obj;
			MList each = (MList)eh.get("$each");
			each.add(value);
		} else {
			MObject eh = new MObject();
			MList each = new MList();
			each.add(obj);
			each.add(value);
			eh.put("$each", each);
			putIn(operator, array, eh);
		}
	}

	public Update pop(String field) {
		putIn("$pop", field, 1);
		return this;
	}

	public Update popFirst(String field) {
		putIn("$pop", field, -1);
		return this;
	}

	public Update rename(String oldName, String newName) {
		putIn("$rename", oldName, newName);
		return this;
	}

	public MObject toMObject() {
		return update;
	}

	@Override
	public String toString() {
		return update.toString();
	}

}

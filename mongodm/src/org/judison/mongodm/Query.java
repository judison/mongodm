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

import java.util.HashMap;
import java.util.Map;

public class Query {

	public enum Operator {
		EQUAL("$eq"),
		GREATER_THAN("$gt"),
		GREATER_THAN_OR_EQUAL("$gte"),
		LESS_THAN("$lt"),
		LESS_THAN_OR_EQUAL("$lte"),
		EXISTS("$exists"),
		TYPE("$type"),
		NOT("$not"),
		MOD("$mod"),
		SIZE("$size"),
		IN("$in"),
		NOT_IN("$nin"),
		ALL("$all"),
		ELEMENT_MATCH("$elemMatch"),
		NOT_EQUAL("$ne"),
		WHERE("$where"),
		NEAR("$near"),
		NEAR_SPHERE("$nearSphere"),
		WITHIN("$within"),
		WITHIN_CIRCLE("$center"),
		WITHIN_CIRCLE_SPHERE("$centerSphere"),
		WITHIN_BOX("$box");

		private String value;

		private Operator(String val) {
			value = val;
		}

		private boolean equals(String val) {
			return value.equals(val);
		}

		public static Operator fromString(String val) {
			for (int i = 0; i < values().length; i++) {
				Operator fo = values()[i];
				if (fo.equals(val))
					return fo;
			}
			return null;
		}
	}

	private MObject conds = new MObject();

	public Query() {}

	public Query(String condition, Object value) {
		filter(condition, value);
	}

	public Query(String prop, Operator oper, Object value) {
		filter(prop, oper, value);
	}

	protected Operator fop(String operator) {
		operator = operator.trim();

		if (operator.equals("=") || operator.equals("=="))
			return Operator.EQUAL;
		else if (operator.equals(">"))
			return Operator.GREATER_THAN;
		else if (operator.equals(">="))
			return Operator.GREATER_THAN_OR_EQUAL;
		else if (operator.equals("<"))
			return Operator.LESS_THAN;
		else if (operator.equals("<="))
			return Operator.LESS_THAN_OR_EQUAL;
		else if (operator.equals("!=") || operator.equals("<>"))
			return Operator.NOT_EQUAL;
		else if (operator.toLowerCase().equals("in"))
			return Operator.IN;
		else if (operator.toLowerCase().equals("nin"))
			return Operator.NOT_IN;
		else if (operator.toLowerCase().equals("all"))
			return Operator.ALL;
		else if (operator.toLowerCase().equals("exists"))
			return Operator.EXISTS;
		else if (operator.toLowerCase().equals("elem"))
			return Operator.ELEMENT_MATCH;
		else if (operator.toLowerCase().equals("size"))
			return Operator.SIZE;
		else if (operator.toLowerCase().equals("within"))
			return Operator.WITHIN;
		else if (operator.toLowerCase().equals("near"))
			return Operator.NEAR;
		else
			throw new IllegalArgumentException("Unknown operator '" + operator + "'");
	}

	public Query filter(String condition, Object value) {
		String[] parts = condition.trim().split(" ");
		if (parts.length < 1 || parts.length > 2)
			throw new IllegalArgumentException("'" + condition + "' is not a legal filter condition");

		String prop = parts[0].trim();
		Operator oper = (parts.length == 2) ? fop(parts[1]) : Operator.EQUAL;

		return filter(prop, oper, value);
	}

	@SuppressWarnings("unchecked")
	public Query filter(String prop, Operator oper, Object value) {
		if (oper == Operator.EQUAL)
			conds.set(prop, value);
		else {
			Object inner = conds.get(prop); // operator within inner object
			if (!(inner instanceof Map<?, ?>)) {
				inner = new HashMap<String, Object>();
				conds.set(prop, inner);
			}
			((Map<String, Object>)inner).put(oper.value, value);
		}
		return this;
	}

	public Query equal(String prop, Object value) {
		return filter(prop, Operator.EQUAL, value);
	}

	public Query notEqual(String prop, Object value) {
		return filter(prop, Operator.NOT_EQUAL, value);
	}

	public Query greater(String prop, Object value) {
		return filter(prop, Operator.GREATER_THAN, value);
	}

	public Query greaterOrEqual(String prop, Object value) {
		return filter(prop, Operator.GREATER_THAN_OR_EQUAL, value);
	}

	public Query less(String prop, Object value) {
		return filter(prop, Operator.LESS_THAN, value);
	}

	public Query lessOrEqual(String prop, Object value) {
		return filter(prop, Operator.LESS_THAN_OR_EQUAL, value);
	}

	public Query exists(String prop) {
		return filter(prop, Operator.EXISTS, Boolean.TRUE);
	}

	public Query notExists(String prop) {
		return filter(prop, Operator.EXISTS, Boolean.FALSE);
	}

	public MObject toMObject() {
		return conds;
	}

	@Override
	public String toString() {
		return conds.toString();
	}

}

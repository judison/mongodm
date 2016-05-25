/*
 * Copyright (c) 2012-2015, Judison Oliveira Gil Filho <judison@gmail.com>
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.judison.mongodm.annotations.Embedded;
import org.judison.mongodm.annotations.Entity;
import org.judison.mongodm.annotations.Id;
import org.judison.mongodm.annotations.Index;
import org.judison.mongodm.annotations.Indexed;
import org.judison.mongodm.annotations.Indexed.Order;
import org.judison.mongodm.annotations.Indexes;
import org.judison.mongodm.annotations.Overflow;
import org.judison.mongodm.annotations.Property;
import org.judison.mongodm.annotations.TextIndex;
import org.judison.mongodm.annotations.Transient;

final class TypeInfo {

	final String entityName;
	final Constructor<?> constructor;
	final LinkedHashMap<String, PropertyInfo> properties;
	final PropertyInfo idField;
	final PropertyInfo overflowField;
	final boolean isEntity;
	final boolean isEmbedded;
	final IndexInfo[] indexes;

	public TypeInfo(Class<?> cls) {
		try {
			Entity entity = cls.getAnnotation(Entity.class);
			if (entity != null) {
				isEntity = true;
				String name = entity.value();
				if (name.isEmpty())
					name = cls.getSimpleName();
				entityName = name;
			} else {
				isEntity = false;
				entityName = cls.getSimpleName();
			}

			Embedded embedded = cls.getAnnotation(Embedded.class);
			isEmbedded = embedded != null;

			if (!isEntity && !isEmbedded)
				throw new IllegalStateException(cls.getName() + " is not an @Entity or @Embedded class");

			constructor = cls.getConstructor();
			constructor.setAccessible(true);

			PropertyInfo idField = null;
			PropertyInfo overflowField = null;

			properties = new LinkedHashMap<String, PropertyInfo>();

			List<IndexInfo> indexes = new ArrayList<IndexInfo>();

			while (cls != null && cls != Object.class) {

				//== Indexes
				Indexes idxs = cls.getAnnotation(Indexes.class);
				if (idxs != null)
					for (Index index: idxs.value())
						indexes.add(new IndexInfo(index));
				Index index = cls.getAnnotation(Index.class);
				if (index != null)
					indexes.add(new IndexInfo(index));
				TextIndex textIndex = cls.getAnnotation(TextIndex.class);
				if (textIndex != null)
					indexes.add(new IndexInfo(textIndex));
				//===================

				for (Field f: cls.getDeclaredFields()) {
					if (f.isAnnotationPresent(Transient.class))
						continue;

					String name = f.getName();
					int fieldMods = f.getModifiers();
					Property prop = f.getAnnotation(Property.class);
					if (prop == null) {
						if ((fieldMods & Modifier.TRANSIENT) != 0)
							continue;
						if ((fieldMods & Modifier.STATIC) != 0)
							continue;
						if ((fieldMods & Modifier.FINAL) != 0)
							continue;
					} else {
						if ((fieldMods & Modifier.STATIC) != 0)
							throw new IllegalStateException("Static field " + f.getDeclaringClass().getName() + "." + f.getName() + " can't be a @Property");
						if ((fieldMods & Modifier.FINAL) != 0)
							throw new IllegalStateException("Final field " + f.getDeclaringClass().getName() + "." + f.getName() + " can't be a @Property");
					}

					if (prop != null) {
						if (!prop.value().isEmpty())
							name = prop.value();
					}
					if (f.isAnnotationPresent(Id.class)) {
						name = "_id";
						if (prop != null && !prop.value().isEmpty() && !prop.value().equals("_id"))
							throw new IllegalStateException("@Id and @Property with a different name other than '_id' at " + f.getDeclaringClass().getName() + "." + f.getName());
					}

					if (f.isAnnotationPresent(Overflow.class)) {

						if (prop != null)
							throw new IllegalStateException("@Overflow field " + f.getDeclaringClass().getName() + "." + f.getName() + " can't be a @Property");
						if (overflowField != null)
							throw new IllegalStateException("Duplicated @Overflow field at " + f.getDeclaringClass().getName() + "." + f.getName());
						if (!MObject.class.isAssignableFrom(f.getType()))
							throw new IllegalStateException("@Overflow field " + f.getDeclaringClass().getName() + "." + f.getName() + " must be a MObject");

						overflowField = new PropertyInfo(f, name, prop);
					}

					if (properties.containsKey(name))
						throw new IllegalStateException("Duplicated field '" + name + "' at " + f.getDeclaringClass().getName() + "." + f.getName());

					PropertyInfo info = new PropertyInfo(f, name, prop);
					properties.put(name, info);

					if (name.equals("_id"))
						idField = info;

					Indexed indexed = f.getAnnotation(Indexed.class);
					if (indexed != null) {
						String[] flds = new String[] { name };
						if (indexed.order() == Order.DESCENDING)
							flds[0] = '-' + name;

						IndexInfo idx = new IndexInfo(indexed.name(), flds, indexed.unique(), indexed.sparse());
						indexes.add(idx);
					}

				}
				cls = cls.getSuperclass();
			}

			this.idField = idField;
			this.overflowField = overflowField;
			this.indexes = indexes.toArray(new IndexInfo[indexes.size()]);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}

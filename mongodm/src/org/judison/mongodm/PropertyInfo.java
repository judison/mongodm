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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.judison.mongodm.annotations.Embedded;
import org.judison.mongodm.annotations.Entity;
import org.judison.mongodm.annotations.Property;

final class PropertyInfo {

	public static enum Type {
		ARRAY,
		ENUM,
		SUB,
		SIMPLE
	}

	public final Type type;
	public final Class<?> cls;
	public final Class<?> itemCls;
	public final Field field;
	public final String name;
	final TypeInfo subType;

	PropertyInfo(Field field, String name, Property prop) {
		this.field = field;
		this.name = name;

		Type type = Type.SIMPLE;

		if (prop != null && prop.concreteClass() != void.class)
			cls = prop.concreteClass();
		else
			cls = field.getType();

		Class<?> itemCls = null;
		if (isListOrArrayClass(field.getType())) {
			type = Type.ARRAY;
			if (cls.isArray())
				itemCls = cls.getComponentType();
			else if (prop != null && prop.itemClass() != void.class)
				itemCls = prop.itemClass();
			else
				throw new RuntimeException("You must specify an itemClass for '" + name + "' at " + field.getDeclaringClass().getName() + "." + field.getName());

		} else {
			if (prop != null && prop.itemClass() != void.class)
				throw new RuntimeException("You must NOT specify an itemClass for '" + name + "' at " + field.getDeclaringClass().getName() + "." + field.getName());

			//== nao sou array....

			if (cls.isEnum())
				type = Type.ENUM;
		}
		this.itemCls = itemCls;

		if (!field.getType().isAssignableFrom(cls))
			throw new RuntimeException("Invalid concrete class for '" + name + "' at " + field.getDeclaringClass().getName() + "." + field.getName());

		if (!isClassConcrete(cls))
			throw new RuntimeException("You must specify a valid concrete class for '" + name + "' at " + field.getDeclaringClass().getName() + "." + field.getName());

		if (type == Type.SIMPLE) {
			// eh simple, ou pode ser sub?
			if (cls.getAnnotation(Entity.class) != null)
				throw new RuntimeException("You cant embedd an @Entity class for '" + name + "' at " + field.getDeclaringClass().getName() + "." + field.getName());

			if (cls.getAnnotation(Embedded.class) != null)
				type = Type.SUB;
			else if (!cls.isPrimitive() && !Mapper.hasTypeConverterFor(cls))
				throw new RuntimeException("There's no TypeConverter registered for class " + cls.getName() + " used at " + field.getDeclaringClass().getName() + "." + field.getName());
		}

		if (type == Type.SUB)
			subType = Mapper.getTypeInfo(cls); //Gera problema ciclico
		else
			subType = null;

		field.setAccessible(true);

		this.type = type;
	}

	private static boolean isClassConcrete(Class<?> cls) {
		// Permite MObject
		return cls.isPrimitive() || cls == MObject.class || (!cls.isInterface() && (cls.isArray() || !Modifier.isAbstract(cls.getModifiers())));
	}

	private static boolean isListOrArrayClass(Class<?> cls) {
		if (cls.isArray())
			return true;
		if (cls == List.class || cls == ArrayList.class || cls == LinkedList.class || cls == Vector.class)
			return true;
		return false;
	}

}
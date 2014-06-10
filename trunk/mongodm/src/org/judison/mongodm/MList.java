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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.bson.util.StringRangeSet;

public class MList extends MObject implements List<Object> {

	private boolean backed;
	private List<Object> list;
	private Object array;
	private Class<?> itemCls;
	private int modCount;

	@SuppressWarnings("unchecked")
	MList(boolean isArray, Object obj, Class<?> itemCls) {
		super(false);
		backed = true;
		array = isArray ? obj : null;
		list = isArray ? null : (List<Object>)obj;
		this.itemCls = itemCls;
	}

	public MList() {
		super(false);
		backed = false;
		list = new ArrayList<Object>();
		array = null;
	}

	@Override
	void mapToObject(TypeInfo typeInfo, Object obj) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	void mapToObject(boolean isArray, Object obj, Class<?> itemCls) {
		if (backed)
			throw new IllegalStateException("MList already mapped");

		List<Object> oldList = list;

		backed = true;
		array = isArray ? obj : null;
		list = isArray ? null : (List<Object>)obj;
		this.itemCls = itemCls;

		if (isArray)
			for (int i = 0; i < oldList.size(); i++)
				set(i, oldList.get(i)); // o get pega certo, sem converter, e o set, vai converter pq backed = true
		else
			for (int i = 0; i < oldList.size(); i++)
				add(oldList.get(i)); // o get pega certo, sem converter, e o set, vai converter pq backed = true	
	}

	@Override
	public boolean isBacked() {
		return backed;
	}

	@Override
	public Object getBackendObject() {
		if (!backed)
			return null;
		if (list != null)
			return list;
		else
			return array;
	}

	@Override
	public int size() {
		if (list != null)
			return list.size();
		else
			return Array.getLength(array);
	}

	@Override
	public boolean add(Object v) {
		if (list != null)
			if (backed)
				list.add(Mapper.bsonToJava(itemCls, null, v)); //ISSUE 1: Allows List<List<ItemClass>>
			else
				list.add(v);
		else
			throw new IllegalStateException("Can't add items to a MList backed by an array");
		modCount++;
		return true;
	}

	@Override
	public Object set(int index, Object v) {
		try {
			if (list != null) {
				if (index >= size()) { // estou adicionando, e nao alterando...
					// adiciona null's se precisar preenxer
					while (index > size())
						list.add(null);
					// adiciona o meu item
					if (backed)
						list.add(index, Mapper.bsonToJava(itemCls, null, v)); //ISSUE 1: Allows List<List<ItemClass>>
					else
						list.add(index, v);
					// retorn null, pq antes nao tinha nada lá
					return null;
				} else { // estou alterando um valor ja existente
					if (backed)
						return Mapper.javaToBson(list.set(index, Mapper.bsonToJava(itemCls, v)));
					else
						return list.set(index, v);
				}
			} else {
				// se meu index eh >= size, da IndexOutOfBounds, pq eh um array por de baixo
				Object old = Array.get(array, index);
				Array.set(array, index, Mapper.bsonToJava(itemCls, v));
				return Mapper.javaToBson(old);
			}
		} finally {
			modCount++;
		}
	}

	@Override
	public Object get(int index) {
		if (list != null)
			if (backed)
				return Mapper.javaToBson(list.get(index));
			else
				return list.get(index);
		else
			return Mapper.javaToBson(Array.get(array, index));
	}

	@Override
	public Object remove(int index) {
		if (list != null)
			try {
				if (backed)
					return Mapper.javaToBson(list.remove(index));
				else
					return list.remove(index);
			} finally {
				modCount++;
			}
		else
			throw new IllegalStateException("Can't remove items from a MList backed by an array");
	}

	@Override
	public void clear() {
		if (list != null)
			try {
				list.clear();
			} finally {
				modCount++;
			}
		else
			throw new IllegalStateException("Can't remove items from a MList backed by an array");
	}

	@Override
	public Object put(String key, Object v) {
		return set(_getInt(key), v);
	}

	// precisa?
	public Object put(int key, Object v) {
		return set(key, v);
	}

	@Override
	public Object get(String key) {
		int i = _getInt(key);
		if (i < 0)
			return null;
		if (i >= size())
			return null;
		return get(i);
	}

	@Override
	public Object removeField(String key) {
		int i = _getInt(key);
		if (i < 0)
			return null;
		if (i >= size())
			return null;
		return remove(i);
	}

	@Override
	public boolean containsField(String key) {
		int i = _getInt(key, false);
		return i >= 0 && i < size();
	}

	@Override
	public Set<String> keySet() {
		//TODO criar um set real, q tipo, permite remover, e etc...
		return new StringRangeSet(size());
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map toMap() {
		// Versao otimizada, a do MObject funciona mas conta com o keySet()
		Map m = new HashMap();
		for (int i = 0; i < size(); i++)
			m.put(String.valueOf(i), get(i));
		return m;
	}

	private boolean partialObject;

	@Override
	public void markAsPartialObject() {
		partialObject = true;
	}

	@Override
	public boolean isPartialObject() {
		return partialObject;
	}

	int _getInt(String s) {
		return _getInt(s, true);
	}

	int _getInt(String s, boolean err) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			if (err)
				throw new IllegalArgumentException("MList can only work with numeric keys, not: [" + s + "]");
			return -1;
		}
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean contains(Object o) {
		if (o == null) {
			for (Object item: this)
				if (item == null)
					return true;
		} else {
			for (Object item: this)
				if (o.equals(item))
					return true;
		}
		return false;
	}

	private class Itr implements Iterator<Object> {

		int cursor = 0;
		int lastRet = -1;

		int expectedModCount = modCount;

		@Override
		public boolean hasNext() {
			return cursor != size();
		}

		@Override
		public Object next() {
			checkForComodification();
			try {
				Object next = get(cursor);
				lastRet = cursor++;
				return next;
			} catch (IndexOutOfBoundsException e) {
				checkForComodification();
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			if (lastRet == -1)
				throw new IllegalStateException();
			checkForComodification();

			try {
				MList.this.remove(lastRet);
				if (lastRet < cursor)
					cursor--;
				lastRet = -1;
				expectedModCount = modCount;
			} catch (IndexOutOfBoundsException e) {
				throw new ConcurrentModificationException();
			}
		}

		final void checkForComodification() {
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
		}
	}

	@Override
	public Iterator<Object> iterator() {
		return new Itr();
	}

	@Override
	public Object[] toArray() {
		int size = size();
		Object[] res = new Object[size];
		for (int i = 0; i < size; i++)
			res[i] = get(i);
		return res;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		int size = size();
		Object[] array = toArray();

		if (a.length < size)
			return (T[])Arrays.copyOf(array, size, a.getClass());

		System.arraycopy(array, 0, a, 0, size);
		if (a.length > size)
			a[size] = null;
		return a;
	}

	@Override
	public boolean remove(Object o) {
		int idx = indexOf(o);
		if (idx != -1) {
			remove(idx);
			return true;
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o: c)
			if (indexOf(o) == -1)
				return false;
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Object> c) {
		boolean res = false;
		for (Object o: c)
			res = add(o);
		return res;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Object> c) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean res = false;
		for (Object o: c)
			res = remove(o) || res;
		return res;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, Object element) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o) {
		if (o == null) {
			for (int i = 0; i < size(); i++)
				if (get(i) == null)
					return i;
		} else {
			for (int i = 0; i < size(); i++)
				if (o.equals(get(i)))
					return i;
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		if (o == null) {
			for (int i = size() - 1; i >= 0; i--)
				if (get(i) == null)
					return i;
		} else {
			for (int i = size() - 1; i >= 0; i--)
				if (o.equals(get(i)))
					return i;
		}
		return -1;
	}

	private class ListItr extends Itr implements ListIterator<Object> {

		ListItr(int index) {
			cursor = index;
		}

		@Override
		public boolean hasPrevious() {
			return cursor != 0;
		}

		@Override
		public Object previous() {
			checkForComodification();
			try {
				int i = cursor - 1;
				Object previous = get(i);
				lastRet = cursor = i;
				return previous;
			} catch (IndexOutOfBoundsException e) {
				checkForComodification();
				throw new NoSuchElementException();
			}
		}

		@Override
		public int nextIndex() {
			return cursor;
		}

		@Override
		public int previousIndex() {
			return cursor - 1;
		}

		@Override
		public void set(Object e) {
			if (lastRet == -1)
				throw new IllegalStateException();
			checkForComodification();

			try {
				MList.this.set(lastRet, e);
				expectedModCount = modCount;
			} catch (IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}

		@Override
		public void add(Object e) {
			checkForComodification();

			try {
				MList.this.add(cursor++, e);
				lastRet = -1;
				expectedModCount = modCount;
			} catch (IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}
	}

	@Override
	public ListIterator<Object> listIterator() {
		return new ListItr(0);
	}

	@Override
	public ListIterator<Object> listIterator(int index) {
		return new ListItr(index);
	}

	@Override
	public List<Object> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

}

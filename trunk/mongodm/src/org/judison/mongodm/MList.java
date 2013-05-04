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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.bson.util.StringRangeSet;

public class MList extends MObject implements List<Object> {

	private List<Object> list;

	public MList() {
		super(false);
		list = new ArrayList<Object>();
	}

	@Override
	public int size() {
		synchronized (list) {
			return list.size();
		}
	}

	@Override
	public boolean add(Object value) {
		synchronized (list) {
			return list.add(value);
		}
	}

	@Override
	public Object set(int index, Object value) {
		synchronized (list) {
			if (index >= size()) { // estou adicionando, e nao alterando...
				// adiciona null's se precisar preenxer
				while (index > size())
					list.add(null);
				list.add(index, value);
				// return null, pq antes nao tinha nada lá
				return null;
			} else { // estou alterando um valor ja existente
				return list.set(index, value);
			}
		}
	}

	@Override
	public Object get(int index) {
		synchronized (list) {
			return list.get(index);
		}
	}

	@Override
	public Object remove(int index) {
		synchronized (list) {
			return list.remove(index);
		}
	}

	@Override
	public void clear() {
		synchronized (list) {
			list.clear();
		}
	}

	@Override
	public Object put(String name, Object value) {
		return set(_index(name), value);
	}

	// precisa?
	public Object put(int index, Object value) {
		return set(index, value);
	}

	@Override
	public Object get(String name) {
		synchronized (list) {
			int index = _indexNoEx(name);
			if (index < 0 || index >= size())
				return null;

			//return get(i);
			return list.get(index); //Otimizacao
		}
	}

	@Override
	public Object removeField(String name) {
		synchronized (list) {
			int index = _indexNoEx(name);
			if (index < 0 || index >= size())
				return null;
			return remove(index);
		}
	}

	@Override
	public boolean containsField(String name) {
		synchronized (list) {
			int index = _indexNoEx(name);
			return index >= 0 && index < list.size(); // Otimizacao
		}
	}

	@Override
	public Set<String> keySet() {
		//TODO criar um set real, q tipo, permite remover, e etc... ??
		return new StringRangeSet(size());
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map toMap() {
		// Versao otimizada, a do MObject funciona mas conta com o keySet()
		synchronized (list) {
			Map m = new HashMap();
			for (int i = 0; i < size(); i++)
				m.put(String.valueOf(i), get(i));
			return m;
		}

	}

	private final int _index(String name) {
		try {
			return Integer.parseInt(name);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("MList can only work with numeric keys, not: [" + name + "]");
		}
	}

	private final int _indexNoEx(String name) {
		try {
			return Integer.parseInt(name);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	@Override
	public boolean isEmpty() {
		synchronized (list) {
			return list.size() == 0;
		}
	}

	@Override
	public boolean contains(Object o) {
		synchronized (list) {
			if (o == null) {
				for (Object item: list)
					if (item == null)
						return true;
			} else {
				for (Object item: list)
					if (o.equals(item))
						return true;
			}
			return false;
		}
	}

	@Override
	public Iterator<Object> iterator() {
		synchronized (list) {
			return list.iterator();
		}
	}

	@Override
	public Object[] toArray() {
		synchronized (list) {
			return list.toArray();
		}
	}

	@Override
	public <T> T[] toArray(T[] a) {
		synchronized (list) {
			return list.toArray(a);
		}
	}

	@Override
	public boolean remove(Object o) {
		synchronized (list) {
			return list.remove(o);
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		synchronized (list) {
			return list.containsAll(c);
		}
	}

	@Override
	public boolean addAll(Collection<? extends Object> c) {
		synchronized (list) {
			return list.addAll(c);
		}
	}

	@Override
	public boolean addAll(int index, Collection<? extends Object> c) {
		synchronized (list) {
			return list.addAll(index, c);
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		synchronized (list) {
			return list.removeAll(c);
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		synchronized (list) {
			return list.retainAll(c);
		}
	}

	@Override
	public void add(int index, Object element) {
		synchronized (list) {
			list.add(index, element);
		}
	}

	@Override
	public int indexOf(Object o) {
		synchronized (list) {
			return list.indexOf(o);
		}
	}

	@Override
	public int lastIndexOf(Object o) {
		synchronized (list) {
			return list.lastIndexOf(o);
		}
	}

	@Override
	public ListIterator<Object> listIterator() {
		synchronized (list) {
			return list.listIterator();
		}
	}

	@Override
	public ListIterator<Object> listIterator(int index) {
		synchronized (list) {
			return list.listIterator(index);
		}
	}

	@Override
	public List<Object> subList(int fromIndex, int toIndex) {
		synchronized (list) {
			return list.subList(fromIndex, toIndex);
		}
	}

}

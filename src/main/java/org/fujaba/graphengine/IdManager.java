package org.fujaba.graphengine;

import java.util.HashMap;

public class IdManager {
	
	private HashMap<Object, Long> idMap;
	private HashMap<Long, Object> objectMap;
	private long nextId = 0;
	
	public IdManager resetIds() {
		idMap = null;
		objectMap = null;
		nextId = 0;
		return this;
	}
	
	public IdManager tellId(Long id, Object o) {
		if (idMap == null) {
			idMap = new HashMap<Object, Long>();
		}
		if (objectMap == null) {
			objectMap = new HashMap<Long, Object>();
		}
		idMap.put(o, id);
		objectMap.put(id, o);
		nextId = id + 1;
		while (objectMap.containsKey(nextId)) {
			++nextId;
		}
		return this;
	}
	
	public long getId(Object o) {
		if (idMap == null) {
			idMap = new HashMap<Object, Long>();
		}
		if (objectMap == null) {
			objectMap = new HashMap<Long, Object>();
		}
		if (!idMap.containsKey(o)) {
			idMap.put(o, nextId);
			objectMap.put(nextId, o);
			while (objectMap.containsKey(nextId)) {
				++nextId;
			}
		}
		return idMap.get(o);
	}
	
	public Object getObject(long id) {
		if (objectMap == null) {
			return null;
		}
		return objectMap.get(id);
	}

}

package org.bitmarkets.bitnash;

import java.lang.reflect.InvocationTargetException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BNObjectSerializer {
	Object objectToSerialize;
	
	public void setObjectToSerialize(Object objectToSerialize) {
		this.objectToSerialize = objectToSerialize;
	}
	
	public Object serialize() throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if (objectToSerialize instanceof BNObject) {
			return serializeBnObject();
		} else if (objectToSerialize instanceof JSONObject) {
			return serializeJSONObject();
		} else if (objectToSerialize instanceof JSONArray) {
			return serializeJSONArray();
		} else {
			return objectToSerialize;
		}
		
	}
	
	public static void willSerializeObject(Object value) {
		if (value instanceof BNObject) {
			BNObject bnObj = (BNObject) value;
			bnObj.willSerialize();
		} else if (value instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) value;
			for (Object obj : jsonArray) {
				willSerializeObject(obj);
			}
		} else if (value instanceof JSONObject) {
			JSONObject jsonObject = (JSONObject) value;
			for (Object key : jsonObject.keySet()) {
				willSerializeObject(jsonObject.get(key));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	Object serializeBnObject() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		BNObject bnObject = (BNObject) objectToSerialize;
		
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put("type", bnObject.getClass().getSimpleName());
		
		for (BNObjectSlot slot: bnObject.bnSlots()) {
			BNObjectSerializer s = new BNObjectSerializer();
			s.setObjectToSerialize(slot.getValue());
			jsonObject.put(slot.getName(), s.serialize());
		}
		return jsonObject;
	}
	
	@SuppressWarnings("unchecked")
	Object serializeJSONObject() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		JSONObject jsonObject = (JSONObject) objectToSerialize;
		
		JSONObject serialized = new JSONObject();
		for (Object key : jsonObject.keySet()) {
			BNObjectSerializer s = new BNObjectSerializer();
			s.setObjectToSerialize(jsonObject.get(key));
			serialized.put(key, s.serialize());
		}
		return serialized;
	}
	
	@SuppressWarnings("unchecked")
	Object serializeJSONArray() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		JSONArray jsonArray = (JSONArray) objectToSerialize;
		
		JSONArray serialized = new JSONArray();
		for (Object element : jsonArray) {
			BNObjectSerializer s = new BNObjectSerializer();
			s.setObjectToSerialize(element);
			serialized.add(s.serialize());
		}
		return serialized;
	}
}

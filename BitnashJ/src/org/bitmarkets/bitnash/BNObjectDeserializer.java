package org.bitmarkets.bitnash;

import java.lang.reflect.Method;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BNObjectDeserializer {
	private Object serialization;
	private BNObject bnParent;
	
	public void setSerialization(Object serialization) {
		this.serialization = serialization;
	}
	
	public void setBnParent(BNObject bnParent) {
		this.bnParent = bnParent;
	}
	
	public Object deserialize() {
		if (serialization instanceof JSONObject) {
			return deserializeJSONObject();
		} else if (serialization instanceof JSONArray) {
			return bnDeserializeJSONArray();
		} else {
			return serialization;
		}
	}
	
	public static void didDeserializeObject(Object value) {
		if (value instanceof BNObject) {
			BNObject bnObj = (BNObject) value;
			bnObj.didDeserialize();
		} else if (value instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) value;
			for (Object obj : jsonArray) {
				didDeserializeObject(obj);
			}
		} else if (value instanceof JSONObject) {
			JSONObject jsonObject = (JSONObject) value;
			for (Object key : jsonObject.keySet()) {
				didDeserializeObject(jsonObject.get(key));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private Object deserializeWithoutType() {
		JSONObject jsonObject = (JSONObject) serialization;
		JSONObject converted = new JSONObject();
		for (Object key : jsonObject.keySet()) {
			BNObjectDeserializer d = new BNObjectDeserializer();
			d.setBnParent(bnParent);
			d.setSerialization(jsonObject.get(key));
			converted.put(key, d.deserialize());
		}
		return converted;
	}
	
	public Object deserializeJSONObject() {
		JSONObject jsonObject = (JSONObject) serialization;
		String type = (String) jsonObject.get("type");

		if (type == null) {
			return deserializeWithoutType();
		} else {
			String fullyQualifiedName = this.getClass().getPackage().getName() + "." + type;
			BNObject obj;
			try {
				Class<?> objClass = Class.forName(fullyQualifiedName);
				
				try {
					Method constructor = objClass.getMethod("bnDeserializerInstance", (Class<?>[])null);
					obj = (BNObject) constructor.invoke(objClass);
				}
				catch (NoSuchMethodException e) {
					obj = (BNObject) objClass.newInstance();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			obj.setBnParent(bnParent);
			obj.deserialzeFromJSONObject(jsonObject);
			return obj;
		}
	}
	
	@SuppressWarnings("unchecked")
	public Object bnDeserializeJSONArray() {
		JSONArray jsonArray = (JSONArray) serialization;
		JSONArray converted = new JSONArray();
		for (Object obj : jsonArray) {
			BNObjectDeserializer d = new BNObjectDeserializer();
			d.setBnParent(bnParent);
			d.setSerialization(obj);
			converted.add(d.deserialize());
		}
		return converted;
	}
}

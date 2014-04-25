package org.bitmarkets.bitnash;

import java.lang.reflect.Method;

public class BNObjectSlot {
	BNObject owner;
	String name;
	
	public void setOwner(BNObject owner) {
		this.owner = owner;
	}
	
	public BNObject getOwner() {
		return owner;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setValue(Object value) {
		Method method = null;
		for (Method candidateMethod : owner.getClass().getMethods()) {
			if (candidateMethod.getName().equals("set" + BNString.capitalized(name))) {
				method = candidateMethod;
				break;
			}
		}
		
		if (method == null) {
			System.err.println("Could not setValue for slot " + name + " on object of class: " + owner.getClass().getName());
		} else {
			try {
				method.invoke(owner, value);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public Object getValue() {
		Method method;
		try {
			method = owner.getClass().getMethod("get" + BNString.capitalized(name), (Class<?>[])null);
			return method.invoke(owner);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

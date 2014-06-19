package org.bitmarkets.bitnash;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BNObjectSlot {
	private static final Logger log = LoggerFactory.getLogger(BNObjectSlot.class);
	
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
			log.warn("Could not setValue for slot {} on object of class: {}", name, owner.getClass().getName());
		} else {
			try {
				//System.err.println(name);
				//System.err.println(value);
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

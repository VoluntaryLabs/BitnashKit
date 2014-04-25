package org.bitmarkets.bitnash;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

public abstract class BNObject {
	BNObject bnParent; //object that "awoke" this object
	
	List<String> bnSlotNames;
	
	List <BNObjectSlot> bnSlots;
	
	public BNObject() {
		bnSlotNames = new ArrayList<String>();
		resetSlots();
	}
	
	public List<BNObjectSlot> bnSlots() {
		if (bnSlots == null) {
			bnSlots = new ArrayList<BNObjectSlot>();
			for (String name : bnSlotNamesList()) {
				BNObjectSlot slot = new BNObjectSlot();
				slot.setOwner(this);
				slot.setName(name);
				bnSlots.add(slot);
			}
		}
		
		return bnSlots;
	}
	
	ArrayList<String>bnSlotNamesList() {
		ArrayList<String> slotNamesList = new ArrayList<String>();
		for (String name : bnSlotNames) {
			slotNamesList.add(name);
		}
		return slotNamesList;
	}
	
	public void setBnParent(BNObject bnParent) {
		this.bnParent = bnParent;
	}
	
	public void setParent(BNObject parent) {
		this.bnParent = parent;
	}
	
	public BNObject getParent() {
		return bnParent;
	}
	
	public void deserialzeFromJSONObject(JSONObject jsonObject) {
		for (BNObjectSlot slot: bnSlots()) {
			BNObjectDeserializer d = new BNObjectDeserializer();
			d.setSerialization(jsonObject.get(slot.getName()));
			d.setBnParent(this);
			
			slot.setValue(d.deserialize());
		}
	}
	
	void didDeserializeSelf() {}
	
	public void didDeserialize() {
		didDeserializeSelf();
		for (BNObjectSlot slot: bnSlots()) {
			BNObjectDeserializer.didDeserializeObject(slot.getValue());
		}
	}
	
	void willSerializeSelf() {}
	
	void resetSlots() {
		
	}
	
	public void willSerialize() {
		resetSlots();
		willSerializeSelf();
		for (BNObjectSlot slot: bnSlots()) {
			BNObjectSerializer.willSerializeObject(slot.getValue());
		}
	}
	
	public BNObject apiPing(Object args) {
		return this;
	}
}

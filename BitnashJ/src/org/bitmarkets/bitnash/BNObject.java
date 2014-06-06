package org.bitmarkets.bitnash;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

public abstract class BNObject {
	BNObject bnParent; //object that "awoke" this object
	JSONObject metaData; //persistent data associated with this object
	
	List<String> bnSlotNames;
	
	List <BNObjectSlot> bnSlots;
	
	public BNObject() {
		bnSlotNames = new ArrayList<String>();
		metaData = new JSONObject();
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
	
	public JSONObject getMetaData() {
		return metaData;
	}
	
	public void setMetaData(JSONObject metaData) {
		this.metaData = metaData;
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
		//readMetaData(); TODO Synchronize this to avoid race conditions?
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
		//writeMetaData(); TODO Synchronize this to avoid race conditions?
	}
	
	public BNObject apiPing(Object args) {
		return this;
	}
	
	public void readMetaData() {
		BNMetaDataDb.shared().readToBnObject(this);
	}
	
	public void writeMetaData() {
		BNMetaDataDb.shared().writeFromBnObject(this);
	}
	
	public String id() {
		return Integer.toHexString(hashCode());
	}
}

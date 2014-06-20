package org.bitmarkets.bitnash;

import java.math.BigInteger;
import java.util.Arrays;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.Utils;

public class BNKey extends BNObject {
	String pubKey;
	String address;
	Number creationTime;
	
	ECKey key;
	
	public BNKey() {
		super();
		bnSlotNames.addAll(Arrays.asList("pubKey", "address", "creationTime"));
	}
	
	public String getPubKey() {
		return pubKey;
	}
	
	public void setPubKey(String pubKey) {
		this.pubKey = pubKey;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public Number getCreationTime() {
		return creationTime;
	}
	
	public void setCreationTime(Number creationTime) {
		this.creationTime = creationTime;
	}
	
	public ECKey getKey() {
		return key;
	}
	
	public void setKey(ECKey key) {
		this.key = key;
	}
	
	public boolean equals(Object o) {
		if (o instanceof BNKey) {
			BNKey bnKey = (BNKey) o;
			
			return bnKey.getKey().equals(key);
		} else {
			return false;
		}
	}
	
	BNWallet bnWallet() {
		return (BNWallet)bnParent;
	}
	
	void didDeserializeSelf() {
		key = bnWallet().wallet().findKeyFromPubKey(Utils.parseAsHexOrBase58(pubKey));
	}
	
	void willSerializeSelf() {
		setPubKey(Utils.bytesToHexString(key.getPubKey()));
		setAddress(new Address(bnWallet().wallet().getParams(), key.getPubKeyHash()).toString());
		setCreationTime(BigInteger.valueOf(key.getCreationTimeSeconds()*1000));
	}
}

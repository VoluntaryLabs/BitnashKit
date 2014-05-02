package org.bitmarkets.bitnash;

import java.math.BigInteger;
import java.util.Arrays;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.Utils;

public class BNKey extends BNObject {
	String pubKey;
	String address;
	BigInteger creationTime;
	
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
	
	public BigInteger getCreationTime() {
		return creationTime;
	}
	
	public void setCreationTime(BigInteger creationTime) {
		this.creationTime = creationTime;
	}
	
	public ECKey getKey() {
		return key;
	}
	
	public void setKey(ECKey key) {
		this.key = key;
	}
	
	BNWallet bnWallet() {
		return (BNWallet)bnParent;
	}
	
	void willSerializeSelf() {
		setPubKey(Utils.bytesToHexString(key.getPubKey()));
		setAddress(new Address(bnWallet().wallet().getParams(), key.getPubKeyHash()).toString());
		setCreationTime(BigInteger.valueOf(key.getCreationTimeSeconds()*1000));
	}
}

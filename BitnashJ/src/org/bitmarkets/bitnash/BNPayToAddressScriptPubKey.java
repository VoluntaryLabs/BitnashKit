package org.bitmarkets.bitnash;

import java.util.Arrays;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.Coin;

public class BNPayToAddressScriptPubKey extends BNScriptPubKey {
	String address;
	
	public BNPayToAddressScriptPubKey() {
		super();
		bnSlotNames.addAll(Arrays.asList("address"));
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return address;
	}
	
	@Override
	void didDeserializeSelf() {
		try {
			if (address != null && !bnTx().existsInWallet()) {
				bnTx().getTransaction().addOutput(Coin.valueOf(txOut().getValue().longValue()), new Address(networkParams(), address));
			}
		} catch (AddressFormatException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	void willSerializeSelf() {
		setAddress(script().getToAddress(networkParams()).toString());
	}
}

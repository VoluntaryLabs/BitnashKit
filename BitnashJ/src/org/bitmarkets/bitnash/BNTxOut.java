package org.bitmarkets.bitnash;

import java.math.BigInteger;
import java.util.Arrays;

import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionOutput;

public class BNTxOut extends BNObject {
	public Number getValue() {
		return value;
	}
	
	public void setValue(Number value) {
		this.value = value;
	}
	
	public BNScriptPubKey getScriptPubKey() {
		return scriptPubKey;
	}
	
	public void setScriptPubKey(BNScriptPubKey scriptPubKey) {
		this.scriptPubKey = scriptPubKey;
	}
	
	public int index() {
		return bnTx().getOutputs().indexOf(this);
	}
	
	BNTx bnTx() {
		return (BNTx) getParent();
	}
	
	Transaction transaction() {
		return bnTx().getTransaction();
	}
	
	TransactionOutput transactionOutput() {
		return transaction().getOutput(index());
	}
	
	Number value;
	BNScriptPubKey scriptPubKey;
	
	public BNTxOut() {
		super();
		bnSlotNames.addAll(Arrays.asList("value", "scriptPubKey"));
	}
	
	void didDeserializeSelf() {
		//delegate this to script
	}
	
	void willSerializeSelf() {
		TransactionOutput transactionOutput = transactionOutput();
		
		if (transactionOutput.getScriptPubKey().isSentToMultiSig()) {
			scriptPubKey = new BNMultisigScriptPubKey();
		} else {
			scriptPubKey = new BNPayToAddressScriptPubKey();
		}
		
		scriptPubKey.setParent(this);
		scriptPubKey.willSerialize();
		
		setScriptPubKey(scriptPubKey);
		
		setValue(BigInteger.valueOf(transactionOutput.getValue().longValue()));
	}
}

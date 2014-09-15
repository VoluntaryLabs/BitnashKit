package org.bitmarkets.bitnash;

import java.math.BigInteger;
import java.util.Arrays;

import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionOutput;

public class BNTxOut extends BNObject {
	public static BNTxOut fromOutput(TransactionOutput output) {
		BNTx bnTx = new BNTx();
		bnTx.setBnParent(BNWallet.shared());
		bnTx.setTransaction(output.getParentTransaction());
		bnTx.willSerialize();
		
		int index = -1;
		for (int i = 0; i < output.getParentTransaction().getOutputs().size(); i++) {
            if (output.getParentTransaction().getOutputs().get(i) == output)
            {
            	index = i;
            	break;
            }
        }
		
		return (BNTxOut) bnTx.getOutputs().get(index);
	}
	
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
	
	public String getDescription() {
		return (String) metaData.get("description");
	}
	
	@SuppressWarnings("unchecked")
	public void setDescription(String description) {
		metaData.put("description", description);
	}
	
	public String getTxType() {
		return (String) metaData.get("txType");
	}
	
	@SuppressWarnings("unchecked")
	public void setTxType(String txType) {
		metaData.put("txType", txType);
	}
	
	public int index() {
		return bnTx().getOutputs().indexOf(this);
	}
	
	@SuppressWarnings("unchecked")
	public void lock() {
		metaData.put("isLocked", Boolean.valueOf(true));
	}
	
	@SuppressWarnings("unchecked")
	public void markAsBroadcast() {
		metaData.put("wasBroadcast", Boolean.valueOf(true));
	}
	
	public boolean wasBroadcast() {
		Boolean wasBroadcast = (Boolean) metaData.get("wasBroadcast");
		return wasBroadcast != null && wasBroadcast.booleanValue();
	}
	
	@SuppressWarnings("unchecked")
	public void unlock() {
		metaData.put("isLocked", Boolean.valueOf(false));
	}
	
	public String id() {
		return bnTx().id() + "." + Integer.toHexString(index());
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

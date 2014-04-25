package org.bitmarkets.bitnash;

import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.script.Script;


public class BNScriptPubKey extends BNObject {
	public Script script() {
		return txOut().transactionOutput().getScriptPubKey();
	}
	
	BNTxOut txOut() {
		return (BNTxOut) getParent();
	}
	
	BNTx bnTx() {
		return ((BNTx)txOut().getParent());
	}
	
	NetworkParameters networkParams() {
		return bnTx().networkParams();
	}
}

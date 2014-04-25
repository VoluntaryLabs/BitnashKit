package org.bitmarkets.bitnash;

import com.google.bitcoin.core.Utils;
import com.google.bitcoin.script.Script;

public class BNScriptSig extends BNObject {
	public BNScriptSig() {
		super();
		bnSlotNames.add("programHexBytes");
	}
	
	public String getProgramHexBytes() {
		return programHexBytes;
	}
	
	public void setProgramHexBytes(String programHexBytes) {
		this.programHexBytes = programHexBytes;
	}
	
	public Script script() { 
		return txIn().transactionInput().getScriptSig();
	}
	
	String programHexBytes;
	
	BNTxIn txIn() {
		return (BNTxIn) bnParent;
	}
	
	void didDeserializeSelf() {
		if (programHexBytes != null) {
			txIn().transactionInput().setScriptSig(new Script(Utils.parseAsHexOrBase58(programHexBytes)));
		}
	}
	
	void willSerializeSelf() {
		if (script().getProgram().length > 0) {
			programHexBytes = Utils.bytesToHexString(script().getProgram());
		}
	}
}

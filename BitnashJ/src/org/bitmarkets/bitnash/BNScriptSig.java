package org.bitmarkets.bitnash;

import java.util.Arrays;

import com.google.bitcoin.core.Utils;
import com.google.bitcoin.script.Script;

public class BNScriptSig extends BNObject {
	String programHexBytes;
	Boolean isMultisig;
	
	public BNScriptSig() {
		super();
		bnSlotNames.addAll(Arrays.asList("programHexBytes", "isMultisig"));
	}
	
	public String getProgramHexBytes() {
		return programHexBytes;
	}
	
	public void setProgramHexBytes(String programHexBytes) {
		this.programHexBytes = programHexBytes;
	}
	
	public Boolean getIsMultisig() {
		return isMultisig;
	}
	
	public void setIsMultisig(Boolean isMultisig) {
		this.isMultisig = isMultisig;
	}
	
	public Script script() { 
		return txIn().transactionInput().getScriptSig();
	}
	
	public boolean isMultisig() {
		return script().getChunks().size() > 2; //TODO will this always be the case?
	}
	
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
			setIsMultisig(Boolean.valueOf(script().getChunks().size() > 2)); //TODO fix this
		} else {
			setIsMultisig(Boolean.valueOf(false));
		}
	}
}

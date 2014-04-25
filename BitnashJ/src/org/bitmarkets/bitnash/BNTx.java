package org.bitmarkets.bitnash;

import java.math.BigInteger;
import java.util.Arrays;

import org.json.simple.JSONArray;

import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Wallet;

public class BNTx extends BNObject {
	public BNError getError() {
		return error;
	}
	
	public void setError(BNError error) {
		this.error = error;
	}
	
	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}
	
	public Transaction getTransaction() {
		return transaction;
	}
	
	public JSONArray getInputs() {
		return inputs;
	}
	
	public void setInputs(JSONArray inputs) {
		this.inputs = inputs;
	}
	
	public JSONArray getOutputs() {
		return outputs;
	}
	
	public void setOutputs(JSONArray outputs) {
		this.outputs = outputs;
	}
	
	public String getHash() {
		return hash;
	}
	
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	public Boolean getIsLocked() {
		return isLocked;
	}
	
	public void setIsLocked(Boolean isLocked) {
		this.isLocked = isLocked;
	}
	
	//TODO subtract fees evenly from change outputs rather than multisig?
	public BNTx apiSubtractFee(Object args) {
		long fee = ((transaction.bitcoinSerialize().length + transaction.getInputs().size()*74)/1000 + 1)*Transaction.REFERENCE_DEFAULT_MIN_TX_FEE.longValue();
		
		TransactionOutput output = transaction.getOutputs().get(0);
		output.setValue(BigInteger.valueOf(Math.max(output.getValue().longValue() - fee, 0)));
		
		BNTxOut firstTxOut = (BNTxOut) outputs.get(0);
		firstTxOut.setValue(output.getValue());
		
		return this;
	}
	
	public BNTx apiSign(Object args) {
		for (Object input : inputs) {
			BNTxIn txIn = (BNTxIn) input;
			txIn.sign();
		}
		
		return this;
	}
	
	public BNTx apiBroadcast(Object args) throws AddressFormatException {
		for (TransactionInput input : getTransaction().getInputs()) {
			input.verify(input.getConnectedOutput());
//System.err.println("VERIFIED SUCCESSFULLY");
		}
		
//System.err.println(getTransaction().toString(null));
		
		bnWallet().peerGroup().broadcastTransaction(getTransaction());
		
		return this;
	}
	
	BNError error;
	JSONArray inputs;
	JSONArray outputs;
	String hash;
	Boolean isLocked;
	
	Transaction transaction;
	
	public BNTx() {
		super();
		bnSlotNames.addAll(Arrays.asList("error", "inputs", "outputs", "hash", "isLocked"));
	}
	
	void resetSlots() {
		inputs = new JSONArray();
		outputs = new JSONArray();
	}
	
	BNWallet bnWallet() {
		return BNApp.getSharedBnWallet();
	}
	
	Wallet wallet() {
		return bnWallet().wallet();
	}
	
	NetworkParameters networkParams() {
		return wallet().getNetworkParameters();
	}
	
	void didDeserializeSelf() {
		//TODO load from hash
		transaction = new Transaction(networkParams());
	}
	
	
	@SuppressWarnings("unchecked")
	void willSerializeSelf() {
		for (@SuppressWarnings("unused") TransactionInput input : transaction.getInputs()) {
			BNTxIn txIn = new BNTxIn();
			inputs.add(txIn); //make sure that txIn.index() is available
			txIn.setBnParent(this);
			txIn.willSerialize();
		}
		
		for (@SuppressWarnings("unused") TransactionOutput transactionOutput : transaction.getOutputs()) {
			BNTxOut txOut = new BNTxOut();
			outputs.add(txOut);
			txOut.setBnParent(this);
			txOut.willSerialize();
		}
		
		setHash(transaction.getHashAsString());
	}
}

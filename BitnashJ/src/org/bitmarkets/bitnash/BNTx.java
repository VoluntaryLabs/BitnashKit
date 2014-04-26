package org.bitmarkets.bitnash;

import java.math.BigInteger;
import java.util.Arrays;

import org.json.simple.JSONArray;

import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionConfidence;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Wallet;

public class BNTx extends BNObject {
	public BNTx() {
		super();
		bnSlotNames.addAll(Arrays.asList("error", "inputs", "outputs", "hash"));
	}
	
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
	
	public Boolean apiIsConfirmed(Object args) {
		return Boolean.valueOf(transaction.getConfidence().getConfidenceType() == TransactionConfidence.ConfidenceType.BUILDING);
	}
	
	public BNTx apiMarkInputsAsSpent(Object args) {
		for (Object inputObj : inputs) {
			TransactionInput input = ((BNTxIn) inputObj).transactionInput();
			input.getConnectedOutput().markAsSpent(input);
		}
		return this;
	}
	
	public BNTx apiMarkInputsAsUnspent(Object args) {
		for (Object inputObj : inputs) {
			TransactionInput input = ((BNTxIn) inputObj).transactionInput();
			input.getConnectedOutput().markAsUnspent();
		}
		return this;
	}
	
	BNError error;
	JSONArray inputs;
	JSONArray outputs;
	String hash;
	
	Transaction transaction;
	
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
		if (hash != null) {
			transaction = wallet().getTransaction(new Sha256Hash(hash));
		}
		
		if (transaction == null) {
			transaction = new Transaction(networkParams());
		}
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

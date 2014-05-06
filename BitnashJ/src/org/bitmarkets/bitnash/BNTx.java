package org.bitmarkets.bitnash;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.simple.JSONArray;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.InsufficientMoneyException;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionConfidence;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.script.ScriptChunk;

public class BNTx extends BNObject {
	BNError error;
	JSONArray inputs;
	JSONArray outputs;
	String txHash;
	BigInteger netValue;
	BigInteger updateTime;
	String counterParty;
	
	Transaction transaction;
	
	public BNTx() {
		super();
		bnSlotNames.addAll(Arrays.asList(
				"error",
				"inputs",
				"outputs",
				"txHash",
				"netValue",
				"updateTime",
				"counterParty"
		));
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
	
	public String getTxHash() {
		return txHash;
	}
	
	public void setTxHash(String txHash) {
		this.txHash = txHash;
	}
	
	public BigInteger getNetValue() {
		return netValue;
	}
	
	public void setNetValue(BigInteger netValue) {
		this.netValue = netValue;
	}
	
	public BigInteger getUpdateTime() {
		return updateTime;
	}
	
	public void setUpdateTime(BigInteger updateTime) {
		this.updateTime = updateTime;
	}
	
	public String getCounterParty() {
		return counterParty;
	}
	
	public void setCounterParty(String counterParty) {
		this.counterParty = counterParty;
	}
	
	public BNTx apiAddInputsAndChange(Object args) throws InsufficientMoneyException {
		Wallet.SendRequest req = Wallet.SendRequest.forTx(transaction);
		
		try {
			wallet().completeTx(req);
		}
		catch (InsufficientMoneyException e) {
			error = new BNError();
			error.setInsufficientValue(BigInteger.valueOf(Math.max(e.missing.longValue(), Transaction.MIN_NONDUST_OUTPUT.longValue())));
			return this;
		}
		
		for (TransactionInput input : transaction.getInputs()) {
			input.setScriptSig(new Script(new byte[0])); //Remove signatures
		}
		
		lastOutput().setValue(lastOutput().getValue().add(fees()));
		
		return this;
		
	}
	
	//TODO subtract fees evenly from change outputs rather than first?
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
	
	public BNTx apiBroadcast(Object args) {
		for (TransactionInput input : getTransaction().getInputs()) {
			try {
				input.verify(input.getConnectedOutput());
				System.err.println("VERIFIED SUCCESSFULLY");
			} catch (Exception e) {
				System.err.println("VERIFY FAILED:");
				System.err.println(transaction.toString());
				throw new RuntimeException(e);
			}
		}
		
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
	
	public BNTx apiRemoveForeignInputs(Object args) {
		ArrayList<TransactionInput> allInputs = new ArrayList<TransactionInput>(transaction.getInputs());
		transaction.clearInputs();
		for (TransactionInput input : allInputs) {
			if (input.getConnectedOutput().isMine(wallet())) {
				transaction.addInput(input);
			}
		}
		return this;
	}
	
	public BigInteger apiInputValue(Object args) {
		return inputValue();
	}
	
	void resetSlots() {
		inputs = new JSONArray();
		outputs = new JSONArray();
	}
	
	BigInteger inputValue() {
		BigInteger value = BigInteger.valueOf(0);
		
		for (TransactionInput input : transaction.getInputs()) {
			value = value.add(input.getConnectedOutput().getValue());
		}
		
		return value;
	}
	
	BigInteger fees() {
		BigInteger fees = inputValue();
		
		for (TransactionOutput output : transaction.getOutputs()) {
			fees = fees.subtract(output.getValue());
		}
		
		return fees;
	}
	
	TransactionOutput lastOutput() {
		return transaction.getOutputs().get(transaction.getOutputs().size() - 1);
	}
	
	BNWallet bnWallet() {
		return BNWallet.shared();
	}
	
	Wallet wallet() {
		return bnWallet().wallet();
	}
	
	NetworkParameters networkParams() {
		return wallet().getNetworkParameters();
	}
	
	void didDeserializeSelf() {
		if (txHash != null) {
			transaction = wallet().getTransaction(new Sha256Hash(txHash));
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
		
		setTxHash(transaction.getHashAsString());
		setNetValue(transaction.getValue(wallet()));
System.err.println(transaction.getUpdateTime());
		setUpdateTime(BigInteger.valueOf(transaction.getUpdateTime().getTime()));
		
		setupCounterParty();
	}
	
	void setupCounterParty() { //TODO properly check previous output for type.  Handle all types.
		if (netValue.longValue() > 0) {
			for (Object input : inputs) {
				BNTxIn txIn = (BNTxIn) input;
				if (txIn.getScriptSig().isMultisig()) {
					for (ScriptChunk chunk : txIn.getScriptSig().script().getChunks()) {
						if (chunk.data.length > 1) {
							if (!wallet().hasKey(new ECKey(null, chunk.data))) {
								setCounterParty(Utils.bytesToHexString(chunk.data));
								break;
							}
						}
					}
					if (counterParty != null) {
						break;
					}
				} else {
					ECKey key = new ECKey(null, txIn.getScriptSig().script().getChunks().get(1).data);
					if (!wallet().hasKey(key)) {
						setCounterParty(key.toAddress(networkParams()).toString());
						break;
					}
				}
			}
		} else {
			for (Object output : outputs) {
				BNTxOut txOut = (BNTxOut) output;
				BNScriptPubKey scriptPubKey = txOut.getScriptPubKey();
				if (scriptPubKey.script().isSentToMultiSig()) {
					for (Object pubKey : ((BNMultisigScriptPubKey)scriptPubKey).getPubKeys()) {
						if (!wallet().hasKey(new ECKey(null, Utils.parseAsHexOrBase58((String)pubKey)))) {
							setCounterParty((String)pubKey);
							break;
						}
					}
					if (counterParty != null) {
						break;
					}
				} else if (!txOut.transactionOutput().isMine(wallet())) {
					setCounterParty(scriptPubKey.script().getToAddress(networkParams()).toString());
					break;
				}
			}
		}
	}
}

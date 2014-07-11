package org.bitmarkets.bitnash;

import java.util.List;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.simple.JSONArray;

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

public class BNTx extends BNObject {
	BNError error;
	JSONArray inputs;
	JSONArray outputs;
	String txHash;
	String serializedHex;
	Number netValue;
	Number fee;
	Number updateTime;
	String counterParty;
	Number confirmations;
	
	Transaction transaction;
	
	public BNTx() {
		super();
		bnSlotNames.addAll(Arrays.asList(
				"error",
				"inputs",
				"outputs",
				"txHash",
				"serializedHex",
				"netValue",
				"fee",
				"updateTime",
				"counterParty",
				"confirmations"
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
	
	public String getSerializedHex() {
		return serializedHex;
	}
	
	public void setSerializedHex(String serializedHex) {
		this.serializedHex = serializedHex;
	}
	
	public Number getNetValue() {
		return netValue;
	}
	
	public void setNetValue(Number netValue) {
		this.netValue = netValue;
	}
	
	public Number getFee() {
		return fee;
	}
	
	public void setFee(Number fee) {
		this.fee = fee;
	}
	
	public Number getUpdateTime() {
		return updateTime;
	}
	
	public void setUpdateTime(Number updateTime) {
		this.updateTime = updateTime;
	}
	
	public String getCounterParty() {
		return counterParty;
	}
	
	public void setCounterParty(String counterParty) {
		this.counterParty = counterParty;
	}
	
	public Number getConfirmations() {
		return confirmations;
	}
	
	public void setConfirmations(Number confirmations) {
		this.confirmations = confirmations;
	}
	
	public BNTx apiAddInputsAndChange(Object args) throws InsufficientMoneyException {
		
		List<TransactionOutput> outputsBefore = new ArrayList<TransactionOutput>(transaction.getOutputs());
		
		Wallet.SendRequest req = Wallet.SendRequest.forTx(transaction);
		
		req.changeAddress = bnWallet().apiCreateKey(null).getKey().toAddress(networkParams());
		req.aesKey = bnWallet().keyParameter;
		
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
		
		
		List<TransactionOutput> outputsAfter = new ArrayList<TransactionOutput>(transaction.getOutputs());
		
		transaction.clearOutputs();
		
		for (TransactionOutput out : outputsBefore)
		{
			transaction.addOutput(out);
		}
		
		for (TransactionOutput out : outputsAfter)
		{
			if (!transaction.getOutputs().contains(out))
			{
				transaction.addOutput(out);
			}
		}
		
		setFee(BigInteger.valueOf(req.fee.longValue()));
		lastOutput().setValue(lastOutput().getValue().add(req.fee));
		
		return this;
		
	}
	
	public BNTx apiEmptyWallet(Object args) throws InsufficientMoneyException {
		
		Wallet.SendRequest req = Wallet.SendRequest.forTx(transaction);
		req.emptyWallet = true;
		
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
		
		setFee(BigInteger.valueOf(req.fee.longValue()));
		lastOutput().setValue(lastOutput().getValue().add(req.fee));
		
		return this;
		
	}
	
	public BNTx apiSubtractFee(Object args) {
		System.err.println("apiSubtractFee: " + transaction.toString());
		int changeOutputCount = Math.max(1, transaction.getOutputs().size() - 1);
		
		long fee = changeOutputCount - 1 + ((transaction.bitcoinSerialize().length + transaction.getInputs().size()*74)/1000 + 1)*Transaction.REFERENCE_DEFAULT_MIN_TX_FEE.longValue();
		setFee(BigInteger.valueOf(fee));
		
		long feePerOutput = fee/changeOutputCount;
		
		for (int i = 0; i < changeOutputCount; i ++) {
			int outputIndex = transaction.getOutputs().size() - 1 - i;
			
			TransactionOutput output = transaction.getOutputs().get(outputIndex);
			long newValue = Math.max(output.getValue().longValue() - feePerOutput, 0);
			if (newValue < Transaction.MIN_NONDUST_OUTPUT.longValue()) {
				List<TransactionOutput> outputs = transaction.getOutputs();
				transaction.clearOutputs();
				for (int j = 0; j < outputIndex; j ++) {
					transaction.addOutput(outputs.get(j));
				}
			}
			output.setValue(BigInteger.valueOf(newValue));
		}
		
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
		boolean allInputsMine = true;
		
		for (TransactionInput input : getTransaction().getInputs()) {
			TransactionOutput output = input.getConnectedOutput();
			if (!allInputsMine || output == null || !output.isMine(wallet())) {
				allInputsMine = false;
			}
			try {
				input.verify(input.getConnectedOutput());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		if (allInputsMine) {
			getTransaction().getConfidence().setSource(TransactionConfidence.Source.SELF);
		}
		
		bnWallet().peerGroup().broadcastTransaction(getTransaction());
		
		this.apiLockInputs(args); //TODO unlock if it fails
		
		return this;
	}
	
	public Boolean apiIsConfirmed(Object args) {
		return Boolean.valueOf(transaction.getConfidence().getConfidenceType() == TransactionConfidence.ConfidenceType.BUILDING);
	}
	
	public Boolean apiWasBroadcast(Object args) {
		return Boolean.valueOf(transaction.getConfidence().getConfidenceType() != TransactionConfidence.ConfidenceType.UNKNOWN);
	}
	
	public BNTx apiLockInputs(Object args) {
		for (Object inputObj : inputs) {
			BNTxOut bnTxOut = ((BNTxIn) inputObj).bnTxOut();
			bnTxOut.readMetaData();
			bnTxOut.lock();
			bnTxOut.writeMetaData();
		}
		return this;
	}
	
	public BNTx apiSetDescription(Object args) {
		for (Object inputObj : inputs) {
			BNTxOut bnTxOut = ((BNTxIn) inputObj).bnTxOut();
			if (bnTxOut == null) {
				return null;
			}
			bnTxOut.setDescription((String)args);
			bnTxOut.writeMetaData();
		}
		return this;
	}
	
	public String apiGetDescription(Object args) {
		for (Object inputObj : inputs) {
			BNTxOut bnTxOut = ((BNTxIn) inputObj).bnTxOut();
			if (bnTxOut == null) {
				return null;
			}
			bnTxOut.readMetaData();
			if (bnTxOut.getDescription() != null) {
				return bnTxOut.getDescription();
			}
		}
		return null;
	}
	
	public BNTx apiUnlockInputs(Object args) {
		for (Object inputObj : inputs) {
			BNTxOut bnTxOut = ((BNTxIn) inputObj).bnTxOut();
			bnTxOut.readMetaData();
			bnTxOut.unlock();
			bnTxOut.writeMetaData();
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
	
	public Number apiConfirmations(Object args) {
		TransactionConfidence confidence = transaction.getConfidence();
		if (confidence == null) {
			return Integer.valueOf(0);
		} else {
			return Integer.valueOf(confidence.getDepthInBlocks());
		}
	}
	
	public BigInteger apiInputValue(Object args) {
		return inputValue();
	}
	
	//Tx that spends an input owned by this wallet.
	//It might be the same tx with the same txhash,
	//the same tx with a different txhash (tx malleability),
	//or a tx that spent an input of this tx before this tx was able to  
	public BNTx apiSubsumingTx(Object args) {
		for (TransactionInput input : transaction.getInputs()) {
			TransactionOutput output = input.getConnectedOutput();
			if (output != null) {
				TransactionInput spendingInput = output.getSpentBy();
				if (spendingInput != null) {
					Transaction subsumingTransaction = spendingInput.getParentTransaction();
					if (subsumingTransaction != null && !subsumingTransaction.equals(transaction)) {
						BNTx bnTx = new BNTx();
						bnTx.setParent(bnParent);
						bnTx.setTransaction(subsumingTransaction);
						return bnTx;
					}
				}
			}
		}
		return null;
	}
	
	//are all outputs owned by this wallet?
	public Boolean apiIsSentToSelf(Object args)
	{
		for (TransactionOutput txout : transaction.getOutputs()) {
			if (!txout.isMine(wallet())) {
				return Boolean.valueOf(false);
			}
		}
		
		return Boolean.valueOf(true);
	}
	
	public boolean existsInWallet() {
		return (txHash != null) && (wallet().getTransaction(new Sha256Hash(txHash)) != null);
	}
	
	public String id() {
		return transaction.getHashAsString();
	}
	
	void resetSlots() {
		inputs = new JSONArray();
		outputs = new JSONArray();
	}
	
	BigInteger inputValue() {
		BigInteger value = BigInteger.valueOf(0);
		
		for (TransactionInput input : transaction.getInputs()) {
			TransactionOutput transactionOutput = input.getConnectedOutput();
			if (transactionOutput == null) {
				return null;
			} else {
				value = value.add(BigInteger.valueOf(transactionOutput.getValue().longValue()));
			}
		}
		
		return value;
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
		setSerializedHex(Utils.bytesToHexString(transaction.bitcoinSerialize()));
		setNetValue(BigInteger.valueOf(transaction.getValue(wallet()).longValue()));
		setUpdateTime(BigInteger.valueOf(transaction.getUpdateTime().getTime()));
		
		setConfirmations(apiConfirmations(null));
	}
}

package org.bitmarkets.bitnash;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.Transaction.SigHash;
import com.google.bitcoin.crypto.TransactionSignature;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.script.ScriptBuilder;

public class BNTxIn extends BNObject {
	public static Logger log = LoggerFactory.getLogger(BNTxIn.class);
	BNScriptSig scriptSig;
	Number previousOutIndex;
	String previousTxSerializedHex;
	String previousTxHash;
	
	public BNTxIn() {
		super();
		bnSlotNames.addAll(Arrays.asList(
				"scriptSig",
				"previousOutIndex",
				"previousTxSerializedHex",
				"previousTxHash"
		));
	}
	
	public BNScriptSig getScriptSig() {
		return scriptSig;
	}
	
	public void setScriptSig(BNScriptSig scriptSig) {
		this.scriptSig = scriptSig;
	}
	
	public Number getPreviousOutIndex() {
		return previousOutIndex;
	}
	
	public void setPreviousOutIndex(Number previousOutIndex) {
		this.previousOutIndex = previousOutIndex;
	}
	
	public String getPreviousTxSerializedHex() {
		return previousTxSerializedHex;
	}
	
	public void setPreviousTxSerializedHex(String previousTxSerializedHex) {
		this.previousTxSerializedHex = previousTxSerializedHex;
	}
	
	public String getPreviousTxHash() {
		return previousTxHash;
	}
	
	public void setPreviousTxHash(String previousTxHash) {
		this.previousTxHash = previousTxHash;
	}
	
	public int index() {
		return bnTx().getInputs().indexOf(this);
	}
	
	public void sign() {
		Script scriptPubKey = transactionInput().getConnectedOutput().getScriptPubKey();
		if (scriptPubKey.isSentToAddress()) {
			signPayToAddress();
		} else if (scriptPubKey.isSentToMultiSig()) {
			signMultisig();
		} else {
			throw new RuntimeException("Can't sign input w/ connected script: " + scriptPubKey.toString());
		}
	}
	
	public void verify() {
		try {
			transactionInput().verify();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public BNTxOut bnTxOut() {
		TransactionOutput txOut = transactionInput().getConnectedOutput();
		if (txOut == null) {
			return null;
		}
		return BNTxOut.fromOutput(txOut);
	}
	
	void signPayToAddress() {
		ECKey key = transactionInput().getOutpoint().getConnectedKey(wallet());
		if (key != null) {
			TransactionSignature txSig = signUsingKey(key);
			if (txSig != null) {
				transactionInput().setScriptSig(ScriptBuilder.createInputScript(txSig, key));
			}
		}
	}
	
	void signMultisig() {
		ArrayList<TransactionSignature> transactionSignatures = new ArrayList<TransactionSignature>();
		
		TransactionSignature existingTxSig = null;
		Script existingScriptSig = transactionInput().getScriptSig();
		
		if (existingScriptSig != null && existingScriptSig.getProgram().length > 0) {
			existingTxSig = new TransactionSignature(
					TransactionSignature.decodeFromDER(transactionInput().getScriptSig().getChunks().get(1).data),
					SigHash.ALL,
					false);
		}
		
		Script scriptPubKey = transactionInput().getConnectedOutput().getScriptPubKey();
		for (int i = 1; i <= 2; i ++) {
			TransactionSignature txSig = signUsingKey(wallet().findKeyFromPubKey(scriptPubKey.getChunks().get(i).data));
			if (txSig != null) {
				transactionSignatures.add(txSig);
			} else if (existingTxSig != null) {
				transactionSignatures.add(existingTxSig);
			}
		}
		
		transactionInput().setScriptSig(ScriptBuilder.createMultiSigInputScript(transactionSignatures));
	}
	
	TransactionSignature signUsingKey(ECKey key) {
		if (key == null) {
			return null;
		}
		
		Transaction transaction = transaction();
		
		if (key.isEncrypted()) {
			key = key.decrypt(wallet().getKeyCrypter(), bnTx().bnWallet().keyParameter);
		}
		
		return transaction.calculateSignature(
				index(),
				key,
				transactionInput().getOutpoint().getConnectedOutput().getScriptPubKey(),
				SigHash.ALL,
				false
		);
	}
	
	BNTx bnTx() {
		return (BNTx) getParent();
	}
	
	Transaction transaction() {
		return bnTx().getTransaction();
	}
	
	Wallet wallet() {
		return bnTx().wallet();
	}
	
	NetworkParameters networkParams() {
		return bnTx().networkParams();
	}
	
	TransactionInput transactionInput() {
		return transaction().getInput(index());
	}
	
	void didDeserializeSelf() {
		Transaction tx = transaction();
		
		Transaction previousTx = wallet().getTransaction(new Sha256Hash(previousTxHash));
		
		if (previousTx == null && previousTxSerializedHex != null) {
			previousTx = new Transaction(networkParams(), Utils.parseAsHexOrBase58(previousTxSerializedHex));
		}
		
		if (previousTx != null && !bnTx().existsInWallet()) {
			tx.addInput(previousTx.getOutput(previousOutIndex.intValue()));
		}
	}
	
	void willSerializeSelf() {
		setPreviousOutIndex(BigInteger.valueOf(transactionInput().getOutpoint().getIndex()));
		setPreviousTxHash(transactionInput().getOutpoint().getHash().toString());
		
		
		if (transactionInput().getConnectedOutput() != null) {
			setPreviousTxSerializedHex(Utils.bytesToHexString(transactionInput().getConnectedOutput().getParentTransaction().bitcoinSerialize()));
		}
		
		if (transactionInput().getScriptSig() != null && transactionInput().getScriptSig().getChunks().size() > 0) {
			scriptSig = new BNScriptSig();
			scriptSig.setBnParent(this);
			scriptSig.willSerialize();
		}
	}
}

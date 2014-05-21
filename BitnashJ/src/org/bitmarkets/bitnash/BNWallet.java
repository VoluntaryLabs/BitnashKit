package org.bitmarkets.bitnash;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import org.json.simple.JSONArray;
import org.spongycastle.crypto.params.KeyParameter;

import com.google.bitcoin.core.*;
import com.google.bitcoin.crypto.KeyCrypter;
import com.google.bitcoin.crypto.KeyCrypterScrypt;
import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.script.Script;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;

//https://code.google.com/p/bitcoinj/wiki/WorkingWithContracts
@SuppressWarnings("unchecked")
public class BNWallet extends BNObject {
	public static enum NetworkType {TestNet3, MainNet};
	static BNWallet shared;
	
	KeyParameter keyParameter;
	String status;
	
	public WalletAppKit walletAppKit;
	
	public BNWallet() {
		super();
		status = "initialized";
	} 
	
	public static BNWallet bnDeserializerInstance() {
		return shared();
	}
	
	public static BNWallet shared() {
		if (shared == null) {
			shared = new BNWallet();
		}
		return shared;
	}
	
	public WalletAppKit getWalletAppKit() {
		return walletAppKit;
	}
	
	public void setWalletAppKit(WalletAppKit walletAppKit) {
		this.walletAppKit = walletAppKit;
	}
	
	public boolean setPassphrase(String passphrase) {
		if (keyParameter != null) {
			//Passphrase is being changed.  Decrypt it first.
	        wallet().decrypt(keyParameter);
		}
		
		if (passphrase == null) {
			keyParameter = null;
			return true;
		} else {
			KeyCrypter keyCrypter = wallet().getKeyCrypter();
			if (keyCrypter == null) {
				keyCrypter = new KeyCrypterScrypt();
			}
	        
			keyParameter = keyCrypter.deriveKey(passphrase);
			
			if (wallet().isEncrypted()) {
				if (!wallet().checkAESKey(keyParameter)) {
					keyParameter = null;
					return false;
				}
			} else {
				wallet().encrypt(keyCrypter, keyParameter);
			}
			
			return true;
		}
	}
	
	public BigInteger getBalance() {
		return walletAppKit.wallet().getBalance();
	}
	
	public KeyParameter getKeyParameter() {
		return keyParameter;
	}
	
	public void markPendingOutputsAsUnspent() {
		for (Transaction tx : wallet().getTransactions(true)) {
			if (!tx.isPending()) {
				for (TransactionOutput txo : tx.getOutputs()) {
					if (txo.isMine(wallet())) {
						txo.markAsUnspent();
					}
				}
			}
		}
	}
	
	public Boolean apiSetPassphrase(Object args) {
		return Boolean.valueOf(this.setPassphrase((String)args));
	}
	
	public BigInteger apiBalance(Object args) {
		return wallet().getBalance();
	}
	
	public String apiStatus(Object args) {
		return status;
	}
	
	public BNKey apiCreateKey(Object args) {
		ECKey key = null;
		
		if (wallet().isEncrypted()) {
			key = wallet().addNewEncryptedKey(wallet().getKeyCrypter(), keyParameter);
		} else {
			key = new ECKey();
			walletAppKit.wallet().addKey(key);
		}
		
		BNKey bnKey = new BNKey();
		bnKey.setBnParent(this);
		bnKey.setKey(key);
		
		return bnKey;
	}
	
	public JSONArray apiTransactions(Object args) {
		JSONArray transactions = new JSONArray();
		for (Transaction transaction : wallet().getTransactions(true)) {
			BNTx bnTx = new BNTx();
			bnTx.setTransaction(transaction);
			transactions.add(bnTx);
		}
		return transactions;
	}
	
	public JSONArray apiKeys(Object args) {
		JSONArray keys = new JSONArray();
		for (ECKey key : wallet().getKeys()) {
			BNKey bnKey = new BNKey();
			bnKey.setBnParent(this);
			bnKey.setKey(key);
			keys.add(bnKey);
		}
		return keys;
	}
	
	public BNKey apiDepositKey(Object args) {
		for (ECKey key : wallet().getKeys()) {
			ECKey candidateKey = key;
			for (Transaction transaction : wallet().getTransactions(true)) {
				if (candidateKey == null) {
					break;
				}
				for (TransactionOutput transactionOutput : transaction.getOutputs()) {
					if (candidateKey == null) {
						break;
					}
					Script scriptPubKey = transactionOutput.getScriptPubKey();
					if (scriptPubKey.isSentToMultiSig()) {
						for (int i = 1; i < 3; i ++) {
							if (candidateKey == null) {
								break;
							}
							if (Arrays.equals(scriptPubKey.getChunks().get(i).data, key.getPubKey())) {
								candidateKey = null;
							}
						}
					} else {
						if (Arrays.equals(key.getPubKeyHash(), scriptPubKey.getPubKeyHash())) {
							candidateKey = null;
						}
					}
				}
			}
			
			if (candidateKey != null) {
				BNKey bnKey = new BNKey();
				bnKey.setKey(candidateKey);
				bnKey.setBnParent(this);
				return bnKey;
			}
		}
		
		return apiCreateKey(null);
	}
	
	public void start() {
		status = "starting";
		
		walletAppKit.startAsync();
		walletAppKit.addListener(new Service.Listener(){
			public void running() {
				status = "started";
				//markPendingOutputsAsUnspent();
			}
		}, MoreExecutors.sameThreadExecutor());
	}
	
	public Wallet wallet() {
		return walletAppKit.wallet();
	}
	
	public PeerGroup peerGroup() {
		return walletAppKit.peerGroup();
	}
	
	public String transactionsPath() throws IOException {
		return new File(this.path(), "transactions").getCanonicalPath();
	}
	
	public void setup() throws IOException {
		new File(this.transactionsPath()).mkdirs();
	}
	
	public String path() throws IOException {
		return walletAppKit.directory().getCanonicalPath();
	}
}

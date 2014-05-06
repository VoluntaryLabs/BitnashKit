package org.bitmarkets.bitnash;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import org.json.simple.JSONArray;
import org.spongycastle.crypto.params.KeyParameter;

import com.google.bitcoin.core.*;
import com.google.bitcoin.crypto.KeyCrypter;
import com.google.bitcoin.crypto.KeyCrypterScrypt;
import com.google.bitcoin.kits.WalletAppKit;
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
			System.err.println("INITIAL ENCRYPTION");
			wallet().encrypt(keyCrypter, keyParameter);
		}
		
		return true;
	}
	
	public BigInteger getBalance() {
		return walletAppKit.wallet().getBalance();
	}
	
	public KeyParameter getKeyParameter() {
		return keyParameter;
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
	
	public void start() {
		status = "starting";
		
		walletAppKit.startAsync();
		walletAppKit.addListener(new Service.Listener(){
			public void running() {
				status = "started";
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

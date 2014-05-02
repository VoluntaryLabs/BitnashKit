package org.bitmarkets.bitnash;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import org.json.simple.JSONArray;

import com.google.bitcoin.core.*;
import com.google.bitcoin.kits.WalletAppKit;

//https://code.google.com/p/bitcoinj/wiki/WorkingWithContracts
@SuppressWarnings("unchecked")
public class BNWallet extends BNObject {
	public static enum NetworkType {TestNet3, MainNet};
	
	public WalletAppKit walletAppKit;
	
	public static BNWallet bnDeserializerInstance() {
		return BNServer.bnDeserializerInstance().getBnWallet();
	}
	
	public WalletAppKit getWalletAppKit() {
		return walletAppKit;
	}
	
	public void setWalletAppKit(WalletAppKit walletAppKit) {
		this.walletAppKit = walletAppKit;
	}
	
	public BigInteger apiBalance(Object args) {
		return wallet().getBalance();
	}
	
	public BigInteger getBalance() {
		return walletAppKit.wallet().getBalance();
	}
	
	public BNKey apiCreateKey(Object args) {
		ECKey key = new ECKey();
		walletAppKit.wallet().addKey(key);
		
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

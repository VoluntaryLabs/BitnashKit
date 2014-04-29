package org.bitmarkets.bitnash;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import org.json.simple.JSONArray;

import com.google.bitcoin.core.*;
import com.google.bitcoin.kits.WalletAppKit;

//https://code.google.com/p/bitcoinj/wiki/WorkingWithContracts
public class BNWallet extends BNObject {
	public static enum NetworkType {TestNet3, MainNet};
	
	public WalletAppKit walletAppKit;
	
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
	
	public String apiCreateAddress(Object args) {
		ECKey key = new ECKey();
		walletAppKit.wallet().addKey(key);
		
		return key.toAddress(walletAppKit.wallet().getParams()).toString();
	}
	
	public String apiCreatePubKey(Object args) {
		ECKey key = new ECKey();
		walletAppKit.wallet().addKey(key);
		
		return Utils.bytesToHexString(key.getPubKey());
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray apiTransactions() {
		JSONArray transactions = new JSONArray();
		for (Transaction transaction : wallet().getTransactions(true)) {
			transactions.add(transaction);
		}
		return transactions;
	}
	
	public Wallet wallet() {
		return walletAppKit.wallet();
	}
	
	public PeerGroup peerGroup() {
		return walletAppKit.peerGroup();
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray apiGetDepositAddresses() {
		JSONArray list = new JSONArray();
		for (ECKey key : walletAppKit.wallet().getKeys()) {
			list.add(new Address(walletAppKit.wallet().getParams(), key.getPubKeyHash()).toString());
		}
		
		return list;
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

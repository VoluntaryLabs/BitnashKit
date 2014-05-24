package org.bitmarkets.bitnash;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;

import org.json.simple.JSONArray;
import org.spongycastle.crypto.params.KeyParameter;

import com.google.bitcoin.core.*;
import com.google.bitcoin.crypto.KeyCrypter;
import com.google.bitcoin.crypto.KeyCrypterScrypt;
import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.params.TestNet3Params;
import com.google.bitcoin.script.Script;

//https://code.google.com/p/bitcoinj/wiki/WorkingWithContracts
@SuppressWarnings("unchecked")
public class BNWallet extends BNObject {
	public static enum BNWalletState { Initialized, Starting, Connecting, Downloading, Running };
	static BNWallet shared;
	
	KeyParameter keyParameter;
	BNWalletState state;
	int blocksToDownload;
	int blocksDownloaded;
	
	public WalletAppKit walletAppKit;
	
	public BNWallet() {
		super();
		state = BNWalletState.Initialized;
		setupWalletAppKit();
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
		switch (state) {
			case Initialized:
				return "initialized";
			case Starting:
				return "starting ...";
			case Connecting:
				return "connecting to peers ..."; // (" + walletAppKit.peerGroup().numConnectedPeers() + "/" + walletAppKit.peerGroup().getMaxConnections() + ")";
			case Downloading:
				return "downloading blocks ..."; //(" + blocksDownloaded + "/" + blocksToDownload + ")";
			case Running:
				return "started";
			default:
				return "unknown state";
		}
	}
	
	public Float apiProgress(Object args) {
		switch (state) {
			case Downloading:
				return Float.valueOf((float)blocksDownloaded/blocksToDownload);
			default:
				return null;
		}
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
	
	public JSONArray apiUsedKeys(Object args) {
		JSONArray usedKeys = new JSONArray();
		
		for (ECKey key : wallet().getKeys()) {
			BNKey bnKey = new BNKey();
			bnKey.setParent(this);
			bnKey.setKey(key);
			
			for (Transaction transaction : wallet().getTransactions(true)) {
				for (TransactionOutput transactionOutput : transaction.getOutputs()) {
					Script scriptPubKey = transactionOutput.getScriptPubKey();
					if (scriptPubKey.isSentToMultiSig()) {
						for (int i = 1; i < 3; i ++) {
							if (Arrays.equals(scriptPubKey.getChunks().get(i).data, key.getPubKey())) {
								usedKeys.add(bnKey);
							}
						}
					} else {
						if (Arrays.equals(key.getPubKeyHash(), scriptPubKey.getPubKeyHash())) {
							usedKeys.add(bnKey);
						}
					}
				}
			}
		}
		
		return usedKeys;
	}
	
	public Boolean apiIsValidAddress(Object obj) {
		String address = (String) obj;
		try {
			new Address(walletAppKit.params(), address);
			return Boolean.valueOf(true);
		}
		catch (Exception e) {
			return Boolean.valueOf(false);
		}
	}
	
	public void start() {
		state = BNWalletState.Starting;
		
		walletAppKit.startAsync();
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
	
	void setupWalletAppKit() {
		walletAppKit = new WalletAppKit(new TestNet3Params(), new File("."), "bitnash") {
			protected void onSetupCompleted() {
				this.peerGroup().setMaxConnections(4);
				state = BNWalletState.Connecting;
			}
		};
		
		walletAppKit.setDownloadListener(new DownloadListener(){
			protected void startDownload(int blocksRemaining) {
				if (state == BNWalletState.Connecting) {
					state = BNWalletState.Downloading;
				}
				
				blocksToDownload = blocksRemaining;
			}
			
			protected void progress(double pct, int blocksRemaining, Date date) {
				blocksDownloaded = blocksToDownload - blocksRemaining;
		    }
			
			protected void doneDownload() {
				state = BNWalletState.Running;
				blocksDownloaded = 0;
				blocksToDownload = 0;
		    }
		});
		
		try {
			walletAppKit.setPeerNodes(
					new PeerAddress(InetAddress.getByName("54.83.28.75"), 18333),
					new PeerAddress(InetAddress.getByName("107.170.35.88"), 18333),
					new PeerAddress(InetAddress.getByName("192.187.125.226"), 18333),
					new PeerAddress(InetAddress.getByName("144.76.175.228"), 18333),
					new PeerAddress(InetAddress.getByName("107.170.107.245"), 18333),
					new PeerAddress(InetAddress.getByName("54.83.21.194"), 18333),
					new PeerAddress(InetAddress.getByName("184.107.180.2"), 18333),
					new PeerAddress(InetAddress.getByName("66.172.10.161"), 18333),
					new PeerAddress(InetAddress.getByName("5.9.119.49"), 18333),
					new PeerAddress(InetAddress.getByName("94.102.53.181"), 18333),
					new PeerAddress(InetAddress.getByName("5.135.159.139"), 18333),
					new PeerAddress(InetAddress.getByName("84.74.97.62"), 18333),
					new PeerAddress(InetAddress.getByName("107.170.99.148"), 18333),
					new PeerAddress(InetAddress.getByName("206.125.175.243"), 18333),
					new PeerAddress(InetAddress.getByName("198.50.156.105"), 18333),
					new PeerAddress(InetAddress.getByName("37.34.60.19"), 18333),
					new PeerAddress(InetAddress.getByName("37.59.21.113"), 18333),
					new PeerAddress(InetAddress.getByName("15.125.110.219"), 18333),
					new PeerAddress(InetAddress.getByName("203.195.193.90"), 18333),
					new PeerAddress(InetAddress.getByName("54.225.176.205"), 18333),
					new PeerAddress(InetAddress.getByName("37.187.40.137"), 18333),
					new PeerAddress(InetAddress.getByName("46.182.106.2"), 18333),
					new PeerAddress(InetAddress.getByName("104.33.110.107"), 18333),
					new PeerAddress(InetAddress.getByName("188.165.246.217"), 18333),
					new PeerAddress(InetAddress.getByName("200.215.116.113"), 18333)
			);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		walletAppKit.setAutoStop(false);
		walletAppKit.setAutoSave(true);
		walletAppKit.setBlockingStartup(false);
	}
}

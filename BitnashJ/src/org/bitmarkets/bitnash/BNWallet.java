package org.bitmarkets.bitnash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
//import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.params.KeyParameter;

import com.google.bitcoin.core.*;
import com.google.bitcoin.crypto.KeyCrypter;
import com.google.bitcoin.crypto.KeyCrypterScrypt;
import com.google.bitcoin.kits.WalletAppKit;
//import com.google.bitcoin.params.TestNet3Params;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.script.Script;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.State;

//https://code.google.com/p/bitcoinj/wiki/WorkingWithContracts
@SuppressWarnings("unchecked")
public class BNWallet extends BNObject {
	public static Logger log = LoggerFactory.getLogger(BNWallet.class);
	public static enum BNWalletState { Initialized, Starting, Connecting, Downloading, Running, Error };
	static BNWallet shared;
	
	Number requiredConfirmations;
	boolean usesTestNet;
	String checkpointsPath;
	
	KeyParameter keyParameter;
	BNWalletState state;
	int blocksToDownload;
	int blocksDownloaded;
	
	public WalletAppKit walletAppKit;
	
	public BNWallet() {
		super();
		state = BNWalletState.Initialized;
		requiredConfirmations = Integer.valueOf(1);
		usesTestNet = true;
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
	
	boolean usesTestNet() {
		return usesTestNet;
	}
	
	public void setUsesTestNet(boolean usesTestNet) {
		this.usesTestNet = usesTestNet;
	}
	
	public String getCheckpointsPath() {
		return checkpointsPath;
	}
	
	public void setCheckpointsPath(String checkpointsPath) {
		this.checkpointsPath = checkpointsPath;
	}
	
	public Number getRequiredConfirmations() {
		return requiredConfirmations;
	}
	
	public void setRequiredConfirmations(Number requiredConfirmations) {
		this.requiredConfirmations = requiredConfirmations;
	}
	
	public WalletAppKit getWalletAppKit() {
		return walletAppKit;
	}
	
	public void setWalletAppKit(WalletAppKit walletAppKit) {
		this.walletAppKit = walletAppKit;
	}
	
	public BNWalletState getState() {
		return state;
	}
	
	public void setState(BNWalletState state) {
		this.state = state;
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
	
	public KeyParameter getKeyParameter() {
		return keyParameter;
	}
	
	public void lockAllOutputs() {
		for (Transaction tx : wallet().getTransactions(true)) {
			if (!tx.isPending()) {
				for (TransactionOutput txo : tx.getOutputs()) {
					if (txo.isMine(wallet())) {
						BNTxOut bnTxOut = BNTxOut.fromOutput(txo);
						bnTxOut.lock();
						bnTxOut.writeMetaData();
					}
				}
			}
		}
	}
	
	public void unlockAllOutputs() {
		for (Transaction tx : wallet().getTransactions(true)) {
			if (!tx.isPending()) {
				for (TransactionOutput txo : tx.getOutputs()) {
					if (txo.isMine(wallet())) {
						BNTxOut bnTxOut = BNTxOut.fromOutput(txo);
						bnTxOut.unlock();
						bnTxOut.writeMetaData();
					}
				}
			}
		}
	}
	
	public BNWallet apiSetRequiredConfirmations(Object args)
	{
		setRequiredConfirmations((Number)args);
		return this;
	}
	
	public Boolean apiSetPassphrase(Object args) {
		return Boolean.valueOf(this.setPassphrase((String)args));
	}
	
	public BigInteger apiBalance(Object args) {
		return BigInteger.valueOf(walletAppKit.wallet().getBalance(new BNUnlockedCoinSelector()).longValue());
	}
	
	public String apiStatus(Object args) {
		switch (state) {
			case Initialized:
				return "initialized";
			case Starting:
				return "starting ...";
			case Connecting:
				return "connecting ..."; // (" + walletAppKit.peerGroup().numConnectedPeers() + "/" + walletAppKit.peerGroup().getMaxConnections() + ")";
			case Downloading:
				return "syncing ..."; //(" + blocksDownloaded + "/" + blocksToDownload + ")";
			case Running:
				return "started";
			case Error:
				return "error";
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
//System.err.println(transaction.getHashAsString());
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
	
	public BNKey apiDepositKey(Object obj) {
		readMetaData();
		
		JSONObject serializedDepositKey = (JSONObject) metaData.get("depositKey");
		if (serializedDepositKey == null) {
			BNKey bnKey = apiCreateKey(obj);
			setDepositKey(bnKey);
			return bnKey;
		} else {
			BNObjectDeserializer d = new BNObjectDeserializer();
			d.setSerialization(serializedDepositKey);
			d.setBnParent(this);
			BNKey bnKey = (BNKey) d.deserialize();
			BNObjectDeserializer.didDeserializeObject(bnKey);
			
			if (this.apiUsedKeys(obj).contains(bnKey)) {
				bnKey = apiCreateKey(obj);
				setDepositKey(bnKey);
				return bnKey;
			} else {
				return bnKey;
			}
		}
	}
	
	public void start() {
		if (walletAppKit == null) {
			setupWalletAppKit();
		}
		
		state = BNWalletState.Starting;
		
		BNMetaDataDb.shared().setPath("./meta-data");
		
		log.info("Wallet Starting ...");
		
		walletAppKit.start();
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
	
	public String id() {
		return "shared";
	}
	
	void setDepositKey(BNKey depositKey) {
		depositKey.willSerialize();
		BNObjectSerializer s = new BNObjectSerializer();
		s.setObjectToSerialize(depositKey);
		try {
			metaData.put("depositKey", s.serialize());
			writeMetaData();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	void setupWalletAppKit() {
		walletAppKit = new WalletAppKit(
			usesTestNet ? new BNTestNet3Params() : new MainNetParams(),
			new File("."),
			"bitnash"
		) {
			protected void onSetupCompleted() {
				this.peerGroup().setMaxConnections(4);
				log.info("Wallet Connecting to Peers ...");
				setState(BNWalletState.Connecting);
			}
		};
		
		walletAppKit.setDownloadListener(new DownloadListener(){
			protected void startDownload(int blocksRemaining) {
				if (state == BNWalletState.Connecting) {
					log.info("Wallet Downloading Blocks ...");
					state = BNWalletState.Downloading;
				}
				
				blocksToDownload = blocksRemaining;
			}
			
			protected void progress(double pct, int blocksRemaining, Date date) {
				blocksDownloaded = blocksToDownload - blocksRemaining;
		    }
			
			protected void doneDownload() {
				log.info("Wallet Running");
				state = BNWalletState.Running;
				blocksDownloaded = 0;
				blocksToDownload = 0;
				//lockAllOutputs();
		    }
		});
		
		walletAppKit.addListener(new Service.Listener() {

			@Override
			public void failed(State arg0, Throwable arg1) {
				state = BNWalletState.Error;
			}

			@Override
			public void running() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void starting() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void stopping(State arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void terminated(State arg0) {
				// TODO Auto-generated method stub
				
			}
			
		}, MoreExecutors.sameThreadExecutor());
		
		/*
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
		//*/
		
		/*
		try {
			walletAppKit.setPeerNodes(
					new PeerAddress(InetAddress.getByName("54.83.28.76"), 18333),
					new PeerAddress(InetAddress.getByName("54.83.28.77"), 18333)
			);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		*/
		
		walletAppKit.setAutoStop(false);
		walletAppKit.setAutoSave(true);
		walletAppKit.setBlockingStartup(false);
		
		if (checkpointsPath != null) {
			try {
				walletAppKit.setCheckpoints(new FileInputStream(new File(checkpointsPath)));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}
}

package org.bitmarkets.bitnash;

import java.io.File;
import java.io.IOException;

import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.params.TestNet3Params;

public class BNApp {
	static BNWallet sharedBnWallet;
	
	public static BNWallet getSharedBnWallet() {
		return sharedBnWallet;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		WalletAppKit walletAppKit = new WalletAppKit(new TestNet3Params(), new File("."), "bitnash");
		walletAppKit.startAndWait();
		
		//System.err.println(walletAppKit.wallet().getTransaction(new Sha256Hash("da81c21c8ea06feb3554f547090363e1b29626c4bdc4b1adc19e95e37bf32cca")));
		
		//TODO why aren't wallets watching addresses from http://blockexplorer.com/testnet/tx/da81c21c8ea06feb3554f547090363e1b29626c4bdc4b1adc19e95e37bf32cca
		
		BNWallet.walletAppKit = walletAppKit;
		BNApp.sharedBnWallet = new BNWallet();
		BNApp.sharedBnWallet.setup();
		
		BNServer server = new BNServer();
		server.start();
		
		walletAppKit.stopAndWait();
		System.err.println("App Exited");
	}

}

package org.bitmarkets.bitnash;

import java.io.File;
import java.io.IOException;

import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.params.TestNet3Params;

public class BNApp {
	public static void main(String[] args) throws IOException {
		WalletAppKit walletAppKit = new WalletAppKit(new TestNet3Params(), new File("."), "bitnash");
		walletAppKit.setAutoSave(true);
		
		BNServer server = BNServer.shared();
		server.getBnWallet().setWalletAppKit(walletAppKit);
		server.start();
	}
}

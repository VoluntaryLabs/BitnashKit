package org.bitmarkets.bitnash;

import java.io.File;
import java.io.FileInputStream;
//import java.io.FileOutputStream;
import java.io.IOException;

//import java.io.PrintStream;

import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.params.TestNet3Params;

public class BNApp {
	public static void main(String[] args) throws IOException {
		/*
		File file = new File("/tmp/BitnashJ.txt");
		FileOutputStream fos = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fos);
		System.setErr(ps);
		*/
		
		WalletAppKit walletAppKit = new WalletAppKit(new TestNet3Params(), new File("."), "bitnash");
		walletAppKit.setAutoStop(false);
		walletAppKit.setAutoSave(true);
		if (args.length == 1) {
			walletAppKit.setCheckpoints(new FileInputStream(new File(args[0])));
		}
		BNWallet.shared().setWalletAppKit(walletAppKit);
		BNWallet.shared().start();
		
		BNServer server = new BNServer();
		server.start();
	}
}

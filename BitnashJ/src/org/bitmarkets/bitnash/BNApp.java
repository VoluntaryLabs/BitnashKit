package org.bitmarkets.bitnash;

import java.io.File;
import java.io.FileInputStream;
//import java.io.FileOutputStream;
import java.io.IOException;

public class BNApp {
	public static void main(String[] args) throws IOException {
		System.err.println("AFTER");
		if (args.length == 1) {
			BNWallet.shared().getWalletAppKit().setCheckpoints(new FileInputStream(new File(args[0])));
		}
		
		BNWallet.shared().start();
		
		BNServer server = new BNServer();
		server.start();
	}
}

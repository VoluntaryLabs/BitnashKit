package org.bitmarkets.bitnash;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
//import java.io.FileOutputStream;
import java.io.IOException;

public class BNApp {
	public static void main(String[] args) throws IOException {
		try {
			System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream("bitnash.log", true))));
			
			for (int i = 0; i < args.length; i += 2) {
				String name = args[i];
				if (name == "-testnet") {
					BNWallet.shared().setUsesTestNet(Boolean.valueOf(args[i + 1]).booleanValue());
				} else if (name == "-checkpoints") {
					BNWallet.shared().getWalletAppKit().setCheckpoints(new FileInputStream(new File(args[i + 1])));
				}
			}
			
			BNWallet.shared().start();
			
			BNServer server = new BNServer();
			server.start();
		}
		catch (Throwable e) {
			e.printStackTrace(System.err);
			System.err.flush();
			System.exit(1);
		}
	}
}

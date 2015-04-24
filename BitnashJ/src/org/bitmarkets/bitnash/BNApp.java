package org.bitmarkets.bitnash;

import java.io.BufferedOutputStream;
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
				if (name.equals("-testnet")) {
					BNWallet.shared().setUsesTestNet(Boolean.valueOf(args[i + 1]).booleanValue());
				} else if (name.equals("-checkpoints")) {
					BNWallet.shared().setCheckpointsPath(args[i + 1]);
				} else if (name.equals("-tor-socks-port")) {
					System.setProperty("socksProxyHost", "127.0.0.1");
					System.err.println("Using Tor Socks Proxy on port " + args[i + 1]);
					System.setProperty("socksProxyPort", args[i + 1]);
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

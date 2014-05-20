package org.bitmarkets.bitnash;

import java.io.File;
import java.io.FileInputStream;
//import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;

//import java.io.PrintStream;

import com.google.bitcoin.core.PeerAddress;
import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.params.TestNet3Params;

public class BNApp {
	public static void main(String[] args) throws IOException {
		WalletAppKit walletAppKit = new WalletAppKit(new TestNet3Params(), new File("."), "bitnash") {
			protected void onSetupCompleted() {
				this.peerGroup().setMaxConnections(4);
			}
		};
		
		walletAppKit.setAutoStop(false);
		walletAppKit.setAutoSave(true);
		//*
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
		//*/
		if (args.length == 1) {
			walletAppKit.setCheckpoints(new FileInputStream(new File(args[0])));
		}
		
		BNWallet.shared().setWalletAppKit(walletAppKit);
		BNWallet.shared().start();
		
		BNServer server = new BNServer();
		server.start();
	}
}

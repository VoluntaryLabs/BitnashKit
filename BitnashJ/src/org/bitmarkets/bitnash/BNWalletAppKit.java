package org.bitmarkets.bitnash;

import java.io.File;

import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.net.BlockingClientManager;

public class BNWalletAppKit extends WalletAppKit {

	public BNWalletAppKit(NetworkParameters params, File directory,
			String filePrefix) {
		super(params, directory, filePrefix);
		// TODO Auto-generated constructor stub
	}
	
	protected PeerGroup createPeerGroup() {
        return new PeerGroup(params, vChain, new BlockingClientManager());
    }
}

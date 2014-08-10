package org.bitmarkets.bitnash;

import com.google.bitcoin.params.TestNet3Params;

public class BNTestNet3Params extends TestNet3Params {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6256655171357836834L;

	public BNTestNet3Params() {
		super();
		dnsSeeds = new String[] {
				"testnet-seed.alexykot.me",           // Alex Kotenko
				"testnet-seed.bitcoin.petertodd.org"  // Peter Todd
	    };
	}
}

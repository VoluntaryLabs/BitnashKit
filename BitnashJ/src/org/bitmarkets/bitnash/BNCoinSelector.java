package org.bitmarkets.bitnash;

import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.wallet.DefaultCoinSelector;

public class BNCoinSelector extends DefaultCoinSelector {
	private BNWallet bnWallet;
	
	public void setBnWallet(BNWallet bnWallet) {
		this.bnWallet = bnWallet;
	}
	
	public BNWallet getBnWallet() {
		return bnWallet;
	}
	
	public static boolean isSelectable(Transaction tx) {
        if (DefaultCoinSelector.isSelectable(tx)) {
        	return true;
        } else {
        	return false;
        }
    }
}

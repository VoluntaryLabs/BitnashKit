package org.bitmarkets.bitnash;

import java.math.BigInteger;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.InsufficientMoneyException;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.script.ScriptBuilder;
import com.google.common.collect.ImmutableList;

public class BNEscrowTx extends BNTx {
	public BNEscrowTx apiFillForValue(Object args) throws InsufficientMoneyException {
		long desiredValue = ((Number) args).longValue();
		
		ECKey myKey = new ECKey();
		wallet().addKey(myKey);
		
		transaction.addOutput(BigInteger.valueOf(desiredValue), ScriptBuilder.createMultiSigOutputScript(2, ImmutableList.of(myKey, new ECKey())));
		Wallet.SendRequest req = Wallet.SendRequest.forTx(transaction);
		
		//TOOD make sure insufficient is > MIN_NONDUST_OUTPUT o.w they can't redeem
		try {
			wallet().completeTx(req);
		}
		catch (InsufficientMoneyException e) {
			error = new BNError();
			error.setInsufficientValue(e.missing);
			return this;
		}
		
		for (TransactionInput input : transaction.getInputs()) {
			input.setScriptSig(new Script(new byte[0])); //TODO why?
		}
		
		return this;
	}
}

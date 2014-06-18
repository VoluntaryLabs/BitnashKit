package org.bitmarkets.bitnash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;

import com.google.bitcoin.core.Coin;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.script.ScriptBuilder;

public class BNMultisigScriptPubKey extends BNScriptPubKey {
	JSONArray pubKeys;
	
	public BNMultisigScriptPubKey() {
		super();
		bnSlotNames.addAll(Arrays.asList("pubKeys"));
	}
	
	public JSONArray getPubKeys() {
		return pubKeys;
	}
	
	public void setPubKeys(JSONArray pubKeys) {
		this.pubKeys = pubKeys;
	}
	
	void didDeserializeSelf() {
		if (pubKeys.size() > 0 && !bnTx().existsInWallet()) {
			Script scriptPubKey = ScriptBuilder.createMultiSigOutputScript(pubKeys.size(), ecPubKeys());
			bnTx().getTransaction().addOutput(Coin.valueOf(txOut().getValue().longValue()), scriptPubKey);
		}
	}
	
	@SuppressWarnings("unchecked")
	void willSerializeSelf() {
		if (script().getChunks().size() == 5) {
			pubKeys.add(Utils.HEX.encode(script().getChunks().get(1).data));
			pubKeys.add(Utils.HEX.encode(script().getChunks().get(2).data));
		}
	}
	
	void resetSlots() {
		pubKeys = new JSONArray();
	}
	
	List<ECKey> ecPubKeys() {
		ArrayList<ECKey>ecPubKeys = new ArrayList<ECKey>();
		for (Object pubKey : pubKeys) {
			ecPubKeys.add(ECKey.fromPublicOnly(Utils.parseAsHexOrBase58((String)pubKey)));
		}
		return ecPubKeys;
	}
}

package org.bitmarkets.bitnash;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class BNServer extends BNObject implements Runnable {
	public void start() {
		new Thread(this).start();
	}
	
	public void run() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String line = "notnull";
			JSONParser parser = new JSONParser();
			while (line != null) { 
				try {
//System.err.println("waiting for line ...");
					line = reader.readLine();
					if (line == null) {
						break;
					} else {
//System.err.println("BitnashJ BNServer Received: " + line);
						try {
							JSONObject message = (JSONObject)parser.parse(line);
							
							try {
								String messageName = (String)message.get("name");
								
								BNObjectDeserializer d = new BNObjectDeserializer();
								d.setSerialization(message.get("obj"));
								Object receiver = d.deserialize();
								BNObjectDeserializer.didDeserializeObject(receiver);
								
								Method method = receiver.getClass().getMethod("api" + BNString.capitalized(messageName), Object.class);
								
								Object result = method.invoke(receiver, message.get("arg"));
								
								BNObjectSerializer.willSerializeObject(result);
								BNObjectSerializer s = new BNObjectSerializer();
								s.setObjectToSerialize(result);
								
								this.respondToMessage(message, s.serialize());
							}
							catch (Exception e) {
								e.printStackTrace();
								this.respondToMessage(message, null, e);
							}
						}
						catch (Exception e) {
							e.printStackTrace();
							
							JSONObject message = new JSONObject();
							
							this.respondToMessage(message, null, e);
						}
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
	        }
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
System.err.println("Stopping Server ...");
			try {
				bnWallet().getWalletAppKit().stopAsync();
				bnWallet().getWalletAppKit().awaitTerminated(5, TimeUnit.SECONDS);
				System.exit(0);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
			finally {
				System.err.println("Server Stopped");
				System.exit(1);
			}
		}
	}
	
	BNWallet bnWallet() {
		return BNWallet.shared();
	}
	
	@SuppressWarnings("unchecked")
	private void respondToMessage(JSONObject incomingMessage, Object obj, Exception e) {
		JSONObject outgoingMessage = new JSONObject();
		outgoingMessage.put("name", incomingMessage.get("name"));
		outgoingMessage.put("obj", obj);
		if (e != null) {
			BNError bnError = new BNError();
			bnError.setDescription(e.toString());
			
			BNObjectSerializer s = new BNObjectSerializer();
			s.setObjectToSerialize(bnError);
			
			try {
				outgoingMessage.put("error", s.serialize());
			}
			catch (Exception e2) {
				e2.printStackTrace(System.err);
				outgoingMessage.put("error", e2.toString());
			}
		}
//System.err.println("BitnashJ BNServer Sent: " + outgoingMessage.toJSONString());
		System.out.println(outgoingMessage.toJSONString());
	}
	
	private void respondToMessage(JSONObject incomingMessage, Object data) {
		this.respondToMessage(incomingMessage, data, null);
	}
}

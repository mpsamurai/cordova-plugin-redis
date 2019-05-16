package org.apache.cordova.redis;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import redis.clients.jedis.*;


/**
 * This class echoes a string called from JavaScript.
 */
public class Redis extends CordovaPlugin {
	
	private Jedis jedis;
	private JedisPubSub jedisPubSub = new JedisPubSub() {};
	
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("initialize")) {
        	String host = args.getString(0);
        	Integer port = Integer.parseInt(args.getString(1));
            this.initialize(host, port, callbackContext);
            return true;
        }else if (action.equals("setStringValue")) {
        	String key = args.getString(0);
        	String value = args.getString(1);
            this.setStringValue(key, value, callbackContext);
            return true;
        }else if (action.equals("setIntegerValue")) {
        	String key = args.getString(0);
        	Integer value = Integer.parseInt(args.getString(1));
            this.setIntegerValue(key, value, callbackContext);
            return true;
        }else if (action.equals("setJsonValue")) {
        	String key = args.getString(0);
        	StringBuilder value = new StringBuilder("");
    		for(int n=1; n<args.length(); n++) {
    			value.append(args.getString(n));
        	}
            this.setJsonValue(key, value.toString(), callbackContext);
            return true;
        }else if (action.equals("getStringValue")) {
        	String key = args.getString(0);
            this.getStringValue(key, callbackContext);
            return true;
        }else if (action.equals("getIntegerValue")) {
        	String key = args.getString(0);
            this.getIntegerValue(key, callbackContext);
            return true;
        }else if (action.equals("getJsonValue")) {
        	String key = args.getString(0);
            this.getJsonValue(key, callbackContext);
            return true;
        }
    	else if (action.equals("publish")) {
        	String channel = args.getString(0);
        	String message = args.getString(1);
            this.publish(channel, message, callbackContext);
            return true;
        }else if (action.equals("subscribe")) {
        	String[] channels = new String[args.length()];
        	for(int n=0; n<args.length(); n++) {
        		channels[n] = args.getString(n);
	        }
            this.subscribe(channels, callbackContext);
            return true;
        }else if (action.equals("finalize")) {
            this.finalize(callbackContext);
            return true;
        }
        return false;
    }

	private void initialize(String host, Integer port, CallbackContext callbackContext) {
		try {
			this.jedis = new Jedis(host, port, 10000);
			callbackContext.success("Connect to " + host + ":" + port);
		} catch (Exception e) {
	        callbackContext.error("Failed to connect to " + host + ":" + port);
    	}
    }
	
	private void finalize(CallbackContext callbackContext) {
		try {
			if(this.jedisPubSub.isSubscribed()){
				this.jedisPubSub.unsubscribe();
			}
    		this.jedis.quit();
			callbackContext.success("Success to disconnect");
		} catch (Exception e) {
	        callbackContext.error("Failed to disconnect");
    	}	
    }
	
    private void setStringValue(String key, String value, CallbackContext callbackContext) {
    	try {
        	this.jedis.set(key, value);
    		callbackContext.success("set key:" + key + " value:" + value);
    	} catch (Exception e) {
    		callbackContext.error("cannot set key:" + key + " value:" + value);
    	}		
    }
	
	private void setIntegerValue(String key, Integer value, CallbackContext callbackContext) {
    	try {
        	this.jedis.set(key, String.valueOf(value));
    		callbackContext.success("set key:" + key + " value:" + String.valueOf(value));
    	} catch (Exception e) {
    		callbackContext.error("cannot set key:" + key + " value:" + String.valueOf(value));
    	}		
    }

    private void setJsonValue(String key, String value, CallbackContext callbackContext) {
    	try {
        	this.jedis.set(key, value);
    		callbackContext.success("set key:" + key + " value:" + value);
    	} catch (Exception e) {
    		callbackContext.error("cannot set key:" + key + " value:" + value);
    	}		
    }
	
	private void getStringValue(String key, CallbackContext callbackContext) {
    	try {
        	String value = this.jedis.get(key);
    		callbackContext.success(value);
    	} catch (Exception e) {
    		callbackContext.error("cannnot get key:" + key);
    	}		
    }
    
	private void getIntegerValue(String key, CallbackContext callbackContext) {
    	try {
    		Integer value = Integer.parseInt(this.jedis.get(key));
    		callbackContext.success(value);
    	} catch (Exception e) {
    		callbackContext.error("cannnot get key:" + key);
    	}		
    }
	
	private void getJsonValue(String key, CallbackContext callbackContext) {
    	try {
    		JSONObject value = new JSONObject(this.jedis.get(key));
    		callbackContext.success(value);
    	} catch (Exception e) {
    		callbackContext.error("cannnot get key:" + key);
    	}		
    }
	
	private void publish(String channel, String message, CallbackContext callbackContext) {
    	try {
        	this.jedis.publish(channel, message);
    		callbackContext.success(message);
    	} catch (Exception e) {
    		callbackContext.error("Cannnot publish channel:" + channel + " message:" + message);
    	}		
    }
	
	private void subscribe(String[] channels, CallbackContext callbackContext) {
    	try {
    		this.jedis.subscribe(this.jedisPubSub, channels);
			callbackContext.success("Success subscribe");
		} catch (Exception e) {
			callbackContext.error("Failed subscribe");
		}
    }
}

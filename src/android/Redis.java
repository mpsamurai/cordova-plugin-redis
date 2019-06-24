package org.apache.cordova.redis;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import redis.clients.jedis.*;


/**
 * This class echoes a string called from JavaScript.
 */
public class Redis extends CordovaPlugin {

    private Jedis jedis;
    private ArrayList<Subscriber> subscribers = new ArrayList<>();
    private String host;
    private Integer port;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("initialize")) {
            String host = args.getString(0);
            Integer port = Integer.parseInt(args.getString(1));
            this.initialize(host, port, callbackContext);
            return true;
        } else if (action.equals("setStringValue")) {
            String key = args.getString(0);
            String value = args.getString(1);
            this.setStringValue(key, value, callbackContext);
            return true;
        } else if (action.equals("setIntegerValue")) {
            String key = args.getString(0);
            Integer value = args.getInt(1);
            this.setIntegerValue(key, value, callbackContext);
            return true;
        } else if (action.equals("setJsonValue")) {
            String key = args.getString(0);
            JSONObject value = args.getJSONObject(1);
            this.setJsonValue(key, value, callbackContext);
            return true;
        } else if (action.equals("getStringValue")) {
            String key = args.getString(0);
            this.getStringValue(key, callbackContext);
            return true;
        } else if (action.equals("getIntegerValue")) {
            String key = args.getString(0);
            this.getIntegerValue(key, callbackContext);
            return true;
        } else if (action.equals("getJsonValue")) {
            String key = args.getString(0);
            this.getJsonValue(key, callbackContext);
            return true;
        } else if (action.equals("publish")) {
            String channel = args.getString(0);
            String message = args.getString(1);
            this.publish(channel, message, callbackContext);
            return true;
        } else if (action.equals("subscribe")) {
            String channel = args.getString(0);
            this.subscribe(channel, callbackContext);
            return true;
        } else if (action.equals("unsubscribe")) {
            String channel = args.getString(0);
            this.unsubscribe(channel, callbackContext);
            return true;
        } else if (action.equals("finalize")) {
            this.finalize(callbackContext);
            return true;
        }
        return false;
    }

    private void initialize(String host, Integer port, CallbackContext callbackContext) {
        try {
            this.noCallbackFinalize();
            this.host = host;
            this.port = port;
            this.jedis = new Jedis(host, port);
            callbackContext.success("Connect to " + host + ":" + port);
        } catch (Exception e) {
            callbackContext.error("Failed to connect to " + host + ":" + port);
        }
    }

    private void noCallbackFinalize() {
        if (this.jedis != null) {
            this.jedis.close();
            this.jedis = null;
        }
        for (Subscriber s : subscribers) {
            s.stop();
        }
        subscribers.clear();
    }

    private void finalize(CallbackContext callbackContext) {
        try {
            this.noCallbackFinalize();
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
            callbackContext.error("Cannot set key:" + key + " value:" + value);
        }
    }

    private void setIntegerValue(String key, Integer value, CallbackContext callbackContext) {
        try {
            this.jedis.set(key, String.valueOf(value));
            callbackContext.success("set key:" + key + " value:" + String.valueOf(value));
        } catch (Exception e) {
            callbackContext.error("Cannot set key:" + key + " value:" + String.valueOf(value));
        }
    }

    private void setJsonValue(String key, JSONObject value, CallbackContext callbackContext) {
        try {
            this.jedis.set(key, value.toString());
            callbackContext.success("set key:" + key + " value:" + value.toString());
        } catch (Exception e) {
            callbackContext.error("Cannot set key:" + key + " value:" + value.toString());
        }
    }

    private void getStringValue(String key, CallbackContext callbackContext) {
        try {
            String value = this.jedis.get(key);
            callbackContext.success(value);
        } catch (Exception e) {
            callbackContext.error("Cannot get key:" + key);
        }
    }

    private void getIntegerValue(String key, CallbackContext callbackContext) {
        try {
            Integer value = Integer.parseInt(this.jedis.get(key));
            callbackContext.success(value);
        } catch (Exception e) {
            callbackContext.error("Cannot get key:" + key);
        }
    }

    private void getJsonValue(String key, CallbackContext callbackContext) {
        try {
            JSONObject value = new JSONObject(this.jedis.get(key));
            callbackContext.success(value);
        } catch (Exception e) {
            callbackContext.error("Cannot get key:" + key);
        }
    }

    private void publish(String channel, String message, CallbackContext callbackContext) {
        try {
            this.jedis.publish(channel, message);
            callbackContext.success(message);
        } catch (Exception e) {
            callbackContext.error("Cannot publish channel:" + channel + " message:" + message);
        }
    }

    private void subscribe(String channel, CallbackContext callbackContext) {
        try {
            Subscriber s = new Subscriber();
            this.subscribers.add(s);
            new Thread(() -> {
                try {
                    s.start(this.host, this.port, channel, (c, m) -> {
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, m);
                        pluginResult.setKeepCallback(true);
                        callbackContext.sendPluginResult(pluginResult);
                    });
                } catch (Exception e) {
                    callbackContext.error("Failed subscribe");
                }
            }, "subscriberThread").start();
        } catch (Exception e) {
            callbackContext.error("Failed subscribe");
        }
    }

    private void unsubscribe(String channel, CallbackContext callbackContext) {
        Iterator<Subscriber> itr = this.subscribers.iterator();
        while (itr.hasNext()) {
            Subscriber s = itr.next();
            if (s.getChannel().equals(channel)) {
                s.stop();
                itr.remove();
            }
        }
        callbackContext.success();
    }
}

@FunctionalInterface
interface MessageCallback {
    void onMessage(String channel, String message);
}

class Subscriber extends JedisPubSub {

    Jedis jedis;
    String channel;
    MessageCallback messageCallback;

    public void start(String host, Integer port, String channel, MessageCallback messageCallback) {
        if (this.jedis != null) {
            return;
        }
        this.jedis = new Jedis(host, port);
        this.channel = channel;
        this.messageCallback = messageCallback;
        try {
            jedis.subscribe(this, channel);
        } catch (Exception e) {
            this.jedis.close();
            this.jedis = null;
        }
    }

    public void stop() {
        if (this.jedis != null) {
            try {
                this.jedis.quit();
            } catch (Exception e) {
            }
            this.jedis.close();
            this.jedis = null;
        }
    }

    public String getChannel() {
        return this.channel;
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        this.stop();
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
    }

    @Override
    public void onMessage(String channel, String message) {
        if (this.messageCallback != null) {
            this.messageCallback.onMessage(channel, message);
        }
    }
}
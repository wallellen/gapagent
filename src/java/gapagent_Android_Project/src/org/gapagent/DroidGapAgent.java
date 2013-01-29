package org.gapagent;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.DroidGap;
import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.gapagent.JWebSocket.MessageListener;
import org.json.JSONObject;

import android.util.Log;

public class DroidGapAgent extends DroidGap implements MessageListener {
	
	public static final String LOG_TAG = "Agent";
	
	public static AgentPageLog log;
	
	public static String agentUrl = "http://gapagent.sinaapp.com/log.html?please_add_permittion_for_this_origin";
	
	public static boolean track = true;
	
	private JWebSocket webSocket;
	
	private boolean loaded = false;

	@Override
	public void init() {
		super.init();
		log = new AgentPageLog() {
			public void callJavascript(String javascript) {
				if(null != DroidGapAgent.this.appView) {
					Log.d(LOG_TAG, "javascript:" + javascript);
					try {
						DroidGapAgent.this.appView.loadUrl("javascript:" + javascript);
					} catch(Exception e) {
						
					}
					
				}
			}
		};
		try {
			webSocket = new JWebSocket(this);
			webSocket.start();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "error: " + e.getMessage());
			log.e(LOG_TAG, "error: " + e.getMessage());
		}
	}
	
	protected boolean exec(String service, String action, String callbackId, String arguments) {
		if(!track) {
			return this.appView.pluginManager.exec(service, action, callbackId, arguments);
		}
		
		
		CordovaPlugin plugin = this.appView.pluginManager.getPlugin(service);
    	CordovaWebView app = this.appView;
        try {
            CallbackContext callbackContext = new CallbackContext(callbackId, app) {
				public void sendPluginResult(PluginResult pluginResult) {
					DroidGapAgent.this.onPluginResult(pluginResult, this.getCallbackId());
				}
            };
            return plugin.execute(action, arguments, callbackContext);
        } catch (Exception e) {
        	e.printStackTrace();
            PluginResult cr = new PluginResult(PluginResult.Status.JSON_EXCEPTION);
            this.onPluginResult(cr, callbackId);
            return true;
        }
        
	}

	@SuppressWarnings("deprecation")
	protected void onPluginResult(PluginResult result, String callbackId) {
		try {
			String resultJson = result.getJSONString();
			JSONObject obj = new JSONObject(resultJson);
			obj.put("callbackId", callbackId);
			resultJson = obj.toString();
			Log.d(LOG_TAG, "result : " + resultJson);
			log.d(LOG_TAG, "result : " + resultJson);
			this.send(resultJson);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "error : " + e.getMessage());
			log.d(LOG_TAG, "error : " + e.getMessage());
			this.send(result.toErrorCallbackString(callbackId));
		}
		
	}

	protected String onData(String data) {
		try {
			Log.d(LOG_TAG, "data : " + data);
			JSONObject obj = new JSONObject(data);
			String service  = obj.getString("service");
			String action = obj.getString("action");
			String args = obj.getString("args");
			String callbackId = obj.getString("callbackId");
			this.exec(service, action, callbackId, args);
		} catch (Exception e) {
			Log.e(LOG_TAG, "error : " + e.getMessage());
			log.d(LOG_TAG, "error : " + e.getMessage());
		}
		return null;
	}
	
	public void send(String msg) {
		if(webSocket != null) {
			webSocket.send(msg);
		}
	}
	
	public void loadUrl(String url) {
		if(track) {
			super.loadUrl(agentUrl);
		} else {
			super.loadUrl(url);
		}
	}
	
	public void loadUrl(String url, int time) {
		if(track) {
			this.loadUrl(agentUrl);
		} else {
			super.loadUrl(url, time);
		}
	}
	
	public void onDestroy() {
		super.onDestroy();
		try {
			webSocket.stop();
		} catch (Exception e) {}
	}

	@Override
	public void onMessage(String message, String id) {
		this.onData(message);
	}
	
}

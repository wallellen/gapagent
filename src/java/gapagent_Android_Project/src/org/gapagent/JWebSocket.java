package org.gapagent;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import android.util.Log;

public class JWebSocket extends WebSocketServer {
	
	public static final String LOG_TAG = DroidGapAgent.LOG_TAG;
	
	String connIDPrefix = "conn_";
	int connIDGen = 0;

	Map<String, WebSocket> connMap = new Hashtable<String, WebSocket>();
	Map<WebSocket, String> connIdMap = new Hashtable<WebSocket, String>();
	
	static int SERVER_PORT = 8989;
	
	MessageListener listener;

	public JWebSocket( int port , Draft d ) throws UnknownHostException {
		super( new InetSocketAddress( port ), Collections.singletonList( d ) );
		Log.d(LOG_TAG, "websocket start");
		DroidGapAgent.log.d(LOG_TAG, "websocket start");
	}
	
	public JWebSocket(MessageListener listener) throws UnknownHostException {
		this(SERVER_PORT, new Draft_17());
		this.listener = listener;
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		Log.d(LOG_TAG, "connect : " + conn.getRemoteSocketAddress().getHostName());
		DroidGapAgent.log.d(LOG_TAG, "connect : " + conn.getRemoteSocketAddress().getHostName());
		String id = connIDPrefix + connIDGen++;
		connMap.put(id, conn);
		connIdMap.put(conn, id);
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
		Log.d(LOG_TAG, "close : " + conn.getRemoteSocketAddress().getHostName());
		DroidGapAgent.log.d(LOG_TAG, "close : " + conn.getRemoteSocketAddress().getHostName());
		String id = connIdMap.get(conn);
		connIdMap.remove(conn);
		connMap.remove(id);
	}

	@Override
	public void onError( WebSocket conn, Exception ex ) {
		Log.d(LOG_TAG, "error : " + conn.getRemoteSocketAddress().getHostName());
		DroidGapAgent.log.d(LOG_TAG, "error : " + conn.getRemoteSocketAddress().getHostName());
		ex.printStackTrace();
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
		Log.d(LOG_TAG, "message : " + message);
		DroidGapAgent.log.d(LOG_TAG, "message : " + message);
		listener.onMessage(message, connIdMap.get(conn));
	}
	
	public void send(String msg) {
		Iterator<String> iter = connMap.keySet().iterator();
		String id = null;
		while(iter.hasNext()) {
			id = iter.next();
		}
		WebSocket socket = connMap.get(id);
		if(socket != null) {
			Log.d(LOG_TAG, "send to " + id + " : " + msg);
			DroidGapAgent.log.d(LOG_TAG, "send to " + id + " : " + msg);
			socket.send(msg);
		}
	}
	
	public interface MessageListener {
		void onMessage(String message, String id);
	}
}

package es.phyl.phyllis.helper;

import android.util.Log;

import com.google.gson.JsonElement;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.InvalidStateException;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.MessageReceivedHandler;
import microsoft.aspnet.signalr.client.NullLogger;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.SimpleEntry;
import microsoft.aspnet.signalr.client.http.CookieCredentials;
import microsoft.aspnet.signalr.client.http.HttpConnection;
import microsoft.aspnet.signalr.client.http.HttpConnectionFuture;
import microsoft.aspnet.signalr.client.http.HttpConnectionFuture.ResponseCallback;
import microsoft.aspnet.signalr.client.http.Request;
import microsoft.aspnet.signalr.client.http.Response;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;

public class HubConnectionFactory {
	public static final String GET_GROUPS = "requestGroups";

	private static HubConnectionFactory mInstance= null;
	private HubConnection mConnection;
	private HubProxy mChat;
	
	protected HubConnectionFactory(){}

    public static synchronized HubConnectionFactory getInstance(){
    	if(null == mInstance){
    		mInstance = new HubConnectionFactory();
    	}
    	return mInstance;
    }
	
	public HubConnection getHubConnection() {
		return mConnection;
	}
	
	public HubProxy getChatHub() {
		return mChat;
	}
	
	public SignalRFuture<Void> connect(String url, CookieCredentials cookieCredentials) {
		final SignalRFuture<Void> future = new SignalRFuture<Void>();
		createObjects(url, future, cookieCredentials);
		
		return future;
	}
	
	public SignalRFuture<Void> connect(final String url, final String username, final String password) {	
		
		final SignalRFuture<Void> connectFuture = new SignalRFuture<Void>();
		
		HttpConnection conn = Platform.createHttpConnection(new NullLogger());

		String loginUrl = url + "Account/Login";
		
		Request request = new Request("POST");
		request.setUrl(loginUrl);
		List<Entry<String, String>> formData = new ArrayList<Entry<String, String>>();

		formData.add(new SimpleEntry<String, String>("Email", username));
		formData.add(new SimpleEntry<String, String>("Password1", password));
		
		request.setFormContent(formData);
	
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		
		final CookieCredentials cc = new CookieCredentials();
		HttpURLConnection.setFollowRedirects(false);
		final HttpConnectionFuture connFuture = conn.execute(request, new ResponseCallback() {
			
			@Override
			public void onResponse(Response response) throws Exception {
				int status = response.getStatus();
				if (status != 302) {
					
					connectFuture.triggerError(new Exception("Login failed"));
					return;
				}
				
				List<String> cookieHeaders = response.getHeaders().get("Set-Cookie");
				
				for (String cookie : cookieHeaders) {
					String hc = cookie.split("\\;")[0];
					String[] nameValue = hc.split("\\=");
					
					if(nameValue.length > 1) {
						String name = nameValue[0];
						String value = nameValue[1];
						cc.addCookie(name, value);
					}
				}
				
				createObjects(url, connectFuture, cc);
			}
		});
		
		connFuture.onError(new ErrorCallback() {
			
			@Override
			public void onError(Throwable error) {
				connectFuture.triggerError(error);
				Log.d("Phyllis", "Error getting credentials: " + error.toString());
			}
		});
			
		return connectFuture;
	}

	public void createObjects(String url, final SignalRFuture<Void> future, CookieCredentials cc){

		final Logger logger = new Logger() {

			@Override
			public void log(String message, LogLevel level) {
				//Log.d("Phyllis", level.toString() + ": " + message);
			}
		};
		mConnection = new HubConnection(url, "", true, logger);

		mConnection.setCredentials(cc);

		try {
			mChat = mConnection.createHubProxy("MainHub");
			mChat.subscribe(new Object() {
				@SuppressWarnings("unused")
				public void messageReceived(String name, String message) {
					Log.d("Phyllis", "mConnection.connected(new Runnable())");
				}
			});
		} catch (InvalidStateException e) {
			Log.d("Phyllis", "Error getting creating proxy: " + e.toString());
			future.triggerError(e);
		}

		mConnection.received(new MessageReceivedHandler() {

			@Override
			public void onMessageReceived(JsonElement json) {
				Log.d("Phyllis", "mConnection.connected(new Runnable())" + json.toString());
			}
		});



		mConnection.connected(new Runnable() {
			@Override
			public void run() {
				Log.d("Phyllis", "mConnection.connected(new Runnable())");
				future.setResult(null);
			}
		});

		SignalRFuture<Void> connectionFuture = mConnection.start(new ServerSentEventsTransport(logger));

		connectionFuture.done(new Action<Void>() {

			@Override
			public void run(Void obj) throws Exception {
				Log.d("Phyllis", "connectionFuture.done");
				future.setResult(obj);
			}
		});
		
		connectionFuture.onError(new ErrorCallback() {

			@Override
			public void onError(Throwable error) {
				Log.d("Phyllis", "Connection error: " + error.toString());
				future.triggerError(error);
			}
		});
		
		mConnection.error(new ErrorCallback() {

			@Override
			public void onError(Throwable error) {
				Log.d("Phyllis", "Connection error: " + error.toString());

				if (!future.isDone()) {
					future.triggerError(error);
				}
			}
		});
		
	}
	
	
	public void disconnect() {
		mChat = null;
		mConnection.stop();
	}
}
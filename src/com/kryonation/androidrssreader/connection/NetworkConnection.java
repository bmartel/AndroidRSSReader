package com.kryonation.androidrssreader.connection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkConnection {
	private static NetworkConnection instance = null;
	private boolean isNetworkAvailable;
	private Context context;
	
	/**
	 * Singleton constructor
	 * @param context
	 */
	private NetworkConnection(Context context){
		setContext(context);
	}
	
	/**
	 * Singleton Accesor method
	 * @param context
	 * @return
	 */
	public static NetworkConnection getInstance(Context context){
		if(instance == null){
			instance = new NetworkConnection(context);
		}
		return instance;
	}
	public boolean retry() {
        isNetworkAvailable = false;
       ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
       if (connectivity == null) {
           return false;
       } else {
          NetworkInfo[] info = connectivity.getAllNetworkInfo();
          if (info != null) {
             for (int i = 0; i < info.length; i++) {
                if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                    isNetworkAvailable = true;
                   return true;
                }
             }
          }
       }
       return false;
    }//isNetworkAvailable()
	
	//Return last known status of the connection
	public boolean checkAvailable(){
		if(!isNetworkAvailable){
			retry();
		}
		return isNetworkAvailable;
	}
	
	//Set the application context
	public void setContext(Context context){
		this.context = context;	
	}
}

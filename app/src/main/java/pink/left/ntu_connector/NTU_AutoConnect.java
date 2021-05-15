package pink.left.ntu_connector;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NTU_AutoConnect extends Service {

    private IntentFilter intentFilter;

    private NetworkChangeReceiver networkChangeReceiver;

    private final String tag = "WIFI链接状况";

    String Net_Operators;
    String IP;
    String username;
    String password;
    String uurl;
    String WifiSSID;

    public NTU_AutoConnect() {
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver,intentFilter);
        readData();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(networkChangeReceiver);
        super.onDestroy();

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.CONNECTED)) {

                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                    // 当前WIFI名称
                    WifiSSID=wifiInfo.getSSID().replace("\"","");
                    if (WifiSSID.equals("NTU")){
                        int ipAddress = wifiInfo.getIpAddress();
                        IP = int2ip(ipAddress);

                        uurl="http://210.29.79.141:801/eportal/?c=Portal&a=login&callback=dr1003&login_method=1&user_account=%2C1%2C"+username+Net_Operators+"&user_password="+password+"&wlan_user_ip="+IP+"&wlan_user_ipv6=&wlan_user_mac=000000000000&wlan_ac_ip=&wlan_ac_name=&jsVersion=3.3.2&v=3654";
                        //Log.d(tag, "用户： " + username);
                        //Log.d(tag, "密码： " + password);
                        //Log.d(tag, "IP： " + IP);
                        httpGet();

                        sendNotification();

                    }
                }
            }
        }
    }

    public void readData(){
        SharedPreferences data=getSharedPreferences("data",MODE_PRIVATE);
        Net_Operators=data.getString("Net_Operator","null");
        username=data.getString("username","");
        password=data.getString("password","");
    }

    /**
     * https://www.cnblogs.com/derekhan/p/11612919.html
     * 将ip的整数形式转换成ip形式
     */
    public static String int2ip(int ipInt) {
        return (ipInt & 0xFF) + "." +
                ((ipInt >> 8) & 0xFF) + "." +
                ((ipInt >> 16) & 0xFF) + "." +
                ((ipInt >> 24) & 0xFF);
    }

    public void httpGet(){
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, uurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        //textView.setText("Response is: "+ response.substring(0,500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //textView.setText("That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void sendNotification() {
        NotificationUtils notificationUtils = new NotificationUtils(this);
        notificationUtils.sendNotification("NTU", "已连接至校园网");
    }

}
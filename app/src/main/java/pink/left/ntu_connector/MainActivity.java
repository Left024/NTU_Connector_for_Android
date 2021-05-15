package pink.left.ntu_connector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallbackWithBeforeParam;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.NetworkChannel;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RadioButton NTU_Button;
    RadioButton CMCC_Button;
    RadioButton Unicom_Button;
    RadioButton Telecom_Button;
    TextView username_input;
    TextView password_input;
    Button login_button;
    Button logout_button;
    CheckBox AutoConnect;

    String Net_Operators;
    String IP;
    String username;
    String password;
    String uurl;

    Boolean AutoConnect_isChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NTU_Button=findViewById(R.id.NTU_Button);
        CMCC_Button=findViewById(R.id.CMCC_Button);
        Unicom_Button=findViewById(R.id.Unicom_Button);
        Telecom_Button=findViewById(R.id.Telecom_Button);
        username_input=findViewById(R.id.username_input);
        password_input=findViewById(R.id.password_input);
        login_button=findViewById(R.id.login_button);
        logout_button=findViewById(R.id.logout_button);
        AutoConnect=findViewById(R.id.AutoConnect);
        readData();
        switch (Net_Operators){
            case "":
                NTU_Button.setChecked(true);
                break;
            case "%40cmcc":
                CMCC_Button.setChecked(true);
                break;
            case "%40unicom":
                Unicom_Button.setChecked(true);
                break;
            case "%40telecom":
                Telecom_Button.setChecked(true);
                break;
        }
        username_input.setText(username);
        password_input.setText(password);
        AutoConnect.setChecked(AutoConnect_isChecked);
        if (AutoConnect_isChecked){
            startService();
        }

        //校园网选项框
        NTU_Button.setOnClickListener(v -> {
            if(NTU_Button.isChecked()){
                Net_Operators="";
                CMCC_Button.setChecked(false);
                Unicom_Button.setChecked(false);
                Telecom_Button.setChecked(false);
            }
        });

        //移动选项框
        CMCC_Button.setOnClickListener(v -> {
            if(CMCC_Button.isChecked()){
                Net_Operators="%40cmcc";
                NTU_Button.setChecked(false);
                Unicom_Button.setChecked(false);
                Telecom_Button.setChecked(false);
            }
        });

        //联通选项框
        Unicom_Button.setOnClickListener(v -> {
            if (Unicom_Button.isChecked()){
                Net_Operators="%40unicom";
                NTU_Button.setChecked(false);
                CMCC_Button.setChecked(false);
                Telecom_Button.setChecked(false);
            }
        });

        //电信选项框
        Telecom_Button.setOnClickListener(v -> {
            if (Telecom_Button.isChecked()){
                Net_Operators="%40telecom";
                NTU_Button.setChecked(false);
                CMCC_Button.setChecked(false);
                Unicom_Button.setChecked(false);
            }
        });

        //连接按钮
        login_button.setOnClickListener(v -> {
            IP=getLocalIpAddress(MainActivity.this);
            username=username_input.getText().toString();
            password=password_input.getText().toString();
            if (!NTU_Button.isChecked()&&!CMCC_Button.isChecked()&&!Unicom_Button.isChecked()&&!Telecom_Button.isChecked()){
                Toast.makeText(MainActivity.this, "请选择运营商", Toast.LENGTH_SHORT).show();
            }else if(username.equals("")){
                Toast.makeText(MainActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            }else if (password.equals("")){
                Toast.makeText(MainActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
            }else{
                Thread thread = new Thread(() -> {
                    uurl="http://210.29.79.141:801/eportal/?c=Portal&a=login&callback=dr1003&login_method=1&user_account=%2C1%2C"+username+Net_Operators+"&user_password="+password+"&wlan_user_ip="+IP+"&wlan_user_ipv6=&wlan_user_mac=000000000000&wlan_ac_ip=&wlan_ac_name=&jsVersion=3.3.2&v=3654";
                    httpGet();
                });
                thread.start();
                Toast.makeText(MainActivity.this, "已连接", Toast.LENGTH_SHORT).show();
                saveData();
            }
        });

        //注销按钮
        logout_button.setOnClickListener(v -> {
            Thread thread = new Thread(() -> {
                IP=getLocalIpAddress(MainActivity.this);
                uurl="http://210.29.79.141:801/eportal/?c=Portal&a=logout&callback=dr1003&login_method=1&user_account=drcom&user_password=123&ac_logout=0&register_mode=1&wlan_user_ip="+IP+"&wlan_user_ipv6=&wlan_vlan_id=0&wlan_user_mac=000000000000&wlan_ac_ip=&wlan_ac_name=ME60&jsVersion=3.3.2&v=4080";
                httpGet();
            });
            thread.start();
            Toast.makeText(MainActivity.this, "已注销", Toast.LENGTH_SHORT).show();
            /**
            IP=getLocalIpAddress(MainActivity.this);
            username_input.setText(IP);**/
        });

        //自动重连
        AutoConnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    username=username_input.getText().toString();
                    password=password_input.getText().toString();
                    if(Net_Operators.equals("null")||username.equals("")||password.equals("")){
                        Toast.makeText(MainActivity.this, "请补全登录信息", Toast.LENGTH_SHORT).show();
                        AutoConnect.setChecked(false);
                    }else{
                        AutoConnect_isChecked=true;
                        saveData();
                        getPermission();
                        startService();
                    }
                }else{
                    AutoConnect_isChecked=false;
                    saveData();
                    stopService();
                }
            }
        });

    }



    public void saveData(){
        SharedPreferences.Editor data=getSharedPreferences("data",MODE_PRIVATE).edit();
        data.putString("Net_Operator",Net_Operators);
        data.putString("username",username);
        data.putString("password",password);
        data.putBoolean("AutoConnect",AutoConnect_isChecked);
        data.apply();
    }

    public void readData(){
        SharedPreferences data=getSharedPreferences("data",MODE_PRIVATE);
        Net_Operators=data.getString("Net_Operator","null");
        username=data.getString("username","");
        password=data.getString("password","");
        AutoConnect_isChecked=data.getBoolean("AutoConnect",false);
    }

    public void httpGet(){
        try  {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(uurl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                InputStream in = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getPermission(){
        PermissionX.init(this)
                .permissions(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                .onForwardToSettings(new ForwardToSettingsCallback() {
                    @Override
                    public void onForwardToSettings(ForwardScope scope, List<String> deniedList) {
                        scope.showForwardToSettingsDialog(deniedList, "您需要去应用程序设置当中手动开启权限", "我已明白");
                    }
                })
                .request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                        if (allGranted) {
                            Toast.makeText(MainActivity.this, "所有申请的权限都已通过", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "您拒绝了如下权限：" + deniedList, Toast.LENGTH_SHORT).show();
                            AutoConnect.setChecked(false);
                        }
                    }
                });
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

    /**
     * 获取当前ip地址
     *
     */
    public static String getLocalIpAddress(Context context) {
        try {

            WifiManager wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            return int2ip(i);
        } catch (Exception ex) {
            return " 获取IP出错鸟!!!!请保证是WIFI,或者请重新打开网络!\n" + ex.getMessage();
        }
        // return null;
    }

    public void startService(){
        Intent startIntent =new Intent(this,NTU_AutoConnect.class);
        startService(startIntent);
    }

    public void stopService(){
        Intent stopIntent = new Intent(this,NTU_AutoConnect.class);
        stopService(stopIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService();
    }
}
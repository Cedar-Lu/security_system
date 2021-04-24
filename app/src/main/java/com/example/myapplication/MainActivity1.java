package com.example.myapplication;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ScheduledExecutorService;


public class MainActivity1 extends AppCompatActivity {
    private String serverUri = "tcp://81.70.200.251:1883";  //这里可以填上各种云平台的物联网云平台的域名+1883端口号，什么阿里云腾讯云百度云天工物接入都可以，
    // 这里我填的是我在我的阿里云服务器上搭建的EMQ平台的地址，
    // 注意：前缀“tcp：//”不可少，之前我没写，怎么都连不上，折腾了好久
    private String userName = "admin";                    //然后是你的用户名，阿里云腾讯云百度云天工物接入这些平台你新建设备后就自动生成了
    private String passWord = "public";                    //用户名对应的密码，同样各种云平台都会对应生成密码，这里我的EMQ平台没做限制，所以用户名和密码可以随便填写
    private String clientId = "app" + System.currentTimeMillis(); //clientId很重要，不能重复，否则就会连不上，所以我定义成 app+当前时间
    private String mqtt_sub_topic = "test";          //需要订阅的主题
    private String mqtt_pub_topic = "test";          //需要发布的主题

    private MqttClient mqtt_client;                         //创建一个mqtt_client对象
    MqttConnectOptions options;

    private ScheduledExecutorService scheduler;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sendmessage);
        Button button1 = (Button) findViewById(R.id.button_1) ;
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeToast("clientID:" + clientId);
                mqtt_init_Connect();
                String secretKey = "1234567890123456";
                String data = JSONObject();
                String mess = null;
                try {
                    mess = AESUtils.encrypt(secretKey, data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                publishMessage(mqtt_sub_topic, mess);
            }
        });


    }

    public void mqtt_init_Connect() {
        try {
            //实例化mqtt_client，填入我们定义的serverUri和clientId，然后MemoryPersistence设置clientid的保存形式，默认为以内存保存
            mqtt_client = new MqttClient(serverUri, clientId, new MemoryPersistence());
            //创建并实例化一个MQTT的连接参数对象
            options = new MqttConnectOptions();
            //然后设置对应的参数
            options.setUserName(userName);                  //设置连接的用户名
            options.setPassword(passWord.toCharArray());    //设置连接的密码
            options.setConnectionTimeout(30);               // 设置超时时间，单位为秒
            options.setKeepAliveInterval(50);               //设置心跳,30s
            options.setAutomaticReconnect(true);            //是否重连
            //设置是否清空session,设置为false表示服务器会保留客户端的连接记录，设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);

            //设置回调
            mqtt_client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    makeToast("connectionLost");
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                }

                @Override
                public void messageArrived(String topicName, MqttMessage message) throws Exception {
                    //subscribe后得到的消息会执行到这里面
                }
            });
            //连接mqtt服务器
            mqtt_client.connect(options);

        } catch (Exception e) {
            e.printStackTrace();
            makeToast(e.toString());
        }
    }

    public void publishMessage(String topic, String message_str) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(message_str.getBytes());
            if (mqtt_client.isConnected()) {
                mqtt_client.publish(topic, message);
                System.out.println("发送成功！");
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /* private void start() {
         try {
 // host为主机名，clientid即连接MQTT的客户端ID，一般以唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
             mqtt_client = new MqttClient(serverUri, clientId, new MemoryPersistence());
 // MQTT的连接设置
             options = new MqttConnectOptions();
 // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
             options.setCleanSession( true);
 // 设置连接的用户名
             options.setUserName(userName);
 // 设置连接的密码
             options.setPassword(passWord.toCharArray());
 // 设置超时时间 单位为秒
             options.setConnectionTimeout( 10);
 // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
             options.setKeepAliveInterval( 20);
 // 设置回调
             mqtt_client.setCallback( new PushCallback());
             MqttTopic topic = mqtt_client.getTopic(mqtt_sub_topic);
 //setWill方法，如果项目中需要知道客户端是否掉线可以调用该方法。设置最终端口的通知消息
 //options.setWill(topic, "close".getBytes(), 2, true);
             mqtt_client.connect(options);
 //订阅消息
             int[] Qos = { 1};
             String[] topic1 = {mqtt_sub_topic};
             mqtt_client.subscribe(topic1, Qos);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }*/
    private static String JSONObject() {
        JSONObject wangxiaoer = new JSONObject();
        Object nullObj = null;
        String data=null;
        try {
            wangxiaoer.put("name", "luxuesong");
            wangxiaoer.put("age", 23);
            wangxiaoer.put("birthday", "1998-10-05");
            wangxiaoer.put("school", "huadian");
            wangxiaoer.put("major", "security ");
             data = wangxiaoer.toString();

    } catch(JSONException e)

    {
        e.printStackTrace();
    }
        return data;
    }

       private void makeToast(String toast_str) {
        Toast.makeText(MainActivity1.this, toast_str, Toast.LENGTH_LONG).show();
    }
}




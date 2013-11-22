package org.cug.network;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.cug.util.Settings;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.IOException;
import java.net.*;

public class NetService extends Activity {

    private Context context;
    private static NetService instance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
    }

    public static NetService getInstance() {
        if (instance == null) {
            instance = new NetService();
        }
        return instance;
    }

    /**
     * 如果是网络连接超时返回false,其他的因为网络连接错误而造成的中断不包括在范围内，返回true
     *
     * @return
     */
    public Boolean isConnected(String response) {
        // if(response.equals(IEXCEPTION.CONNECTTIMEOUT))
        // return false;
        // else
        return true;
    }

    /**
     * 判断是否有网络可用
     *
     * @return true--网络可用；false--网络不可用
     */
    private Boolean isNetAvailable() {
        int index = CheckNet.checkNetworkType(context);
        if (index == CheckNet.TYPE_NET_WORK_DISABLED)
            return false;
        else
            return true;
    }

    /**
     * 获取不同网络的访问代理，wap需要，3G和net不需要代理
     */
    private HttpHost getProxy() {
        HttpHost httpHost = null;
        int index = CheckNet.checkNetworkType(context);
        if (index == CheckNet.TYPE_CM_CU_WAP) {
            httpHost = new HttpHost("10.0.0.172", 80, "http");
        } else if (index == CheckNet.TYPE_CT_WAP) {
            httpHost = new HttpHost("10.0.0.200", 80, "http");
        }
        return httpHost;
    }

    /**
     * 判断Android客户端网络是否连接 add by lihao
     *
     * @return 真假
     */
    public boolean checkNet(Context context) {

        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * 运用KSOAP调用WebService
     *
     * @param
     * @return
     */
    public String getWSReponse(String content) {

        // 命名空间
        String nameSpace = "";
        // EndPoint
        String endPoint = Settings.PROXYURL;
        // SOAP Action
        String soapAction = "";
        // 指定WebService的命名空间和调用的方法名
        SoapObject rpc = new SoapObject(nameSpace, Settings.WEBMETHODNAME);
        // 设置需调用WebService接口需要传入的参数
        
        rpc.addProperty("userName", "userName");
		rpc.addProperty("password", "password");
        rpc.addProperty("content", content);// 第三方应用交换的数据

        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.bodyOut = rpc;
        // 等价于envelope.bodyOut = rpc;
        envelope.setOutputSoapObject(rpc);
        HttpTransportSE transport = new HttpTransportSE(endPoint);
        try {
            // 调用WebService
            transport.call(soapAction, envelope);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // 获取返回的数据
        SoapObject object = (SoapObject) envelope.bodyIn;
        if (object == null) {
            return null;
        }
        // 获取返回的结果
        String result = object.getProperty(0).toString();
        return result;
    }

    /**
     * HTTP请求
     *
     * @param method
     * @param url
     * @return
     */
    private String HttpRequest(String method, String url) {

        HttpResponse httpResponse = null;
        StringBuffer result = new StringBuffer();

        // 首先判断网络是否可用
        if (!isNetAvailable()) {
            result.append(Settings.NETNOTAVAILABLE);
            return result.toString();
        } else {

            if (method.toLowerCase().equals("get")) {
                // 1.通过url创建HttpGet对象
                HttpGet httpGet = new HttpGet(url);
                // 2.通过DefaultClient的excute方法执行返回一个HttpResponse对象
                HttpClient httpClient = new DefaultHttpClient();
                // 网络代理设置
                HttpHost proxy = getProxy();
                if (proxy != null) {
                    httpClient.getParams().setParameter(
                            ConnRoutePNames.DEFAULT_PROXY, proxy);
                }
                httpClient.getParams().setParameter(
                        CoreConnectionPNames.CONNECTION_TIMEOUT,
                        Settings.CONNECTTIMEMIDDLE);// 连接超时
                httpClient.getParams().setParameter(
                        CoreConnectionPNames.SO_TIMEOUT,
                        Settings.CONNECTTIMEMIDDLE);// 请求超时
                try {
                    httpResponse = httpClient.execute(httpGet);
                    // 3.取得相关信息
                    // 取得HttpEntiy
                    HttpEntity httpEntity = httpResponse.getEntity();
                    // 得到一些数据
                    // 通过EntityUtils并指定编码方式取到返回的数据
                    try {
                        result.append(EntityUtils.toString(httpEntity, "utf-8"));
                    } catch (ParseException e) {
                        result.append(Settings.PARSEERROR);
                        Log.e("EXCEPTION", e.getStackTrace().toString());
                    } catch (IOException e) {
                        result.append(Settings.IOERROR);
                        Log.e("EXCEPTION", e.getStackTrace().toString());
                    }
                    // 得到StatusLine接口对象
                    StatusLine statusLine = httpResponse.getStatusLine();
                    // //得到协议
                    // result.append("协议:" + statusLine.getProtocolVersion() +
                    // "\r\n");
                    int statusCode = statusLine.getStatusCode();
                    if (statusCode != 200) {
                        // Toast.makeText(context, "服务器返回错误：错误码-"+statusCode,
                        // Toast.LENGTH_LONG).show();
                        result.append(Settings.SERVERRETURNERROR + statusCode);
                    }
                } catch (ConnectTimeoutException e) {
                    // 请求超时会抛出InterruptedIOException异常,连接超时会抛出ConnectTimeoutException异常
                    // ConnectTimeoutException继承自InterruptedIOException，所以只要捕获ConnectTimeoutException就可以了
                    result.append(Settings.CONNECTTIMEOUT);
                } catch (ClientProtocolException e) {
                    result.append(Settings.CLIENTPROTOCOLERROR);
                    Log.e("EXCEPTION", e.getStackTrace().toString());
                } catch (IOException e) {
                    result.append(Settings.IOERROR);
                    Log.e("EXCEPTION", e.toString());
                }

            } else if (method.toLowerCase().equals("post")) {
                // 1.通过url创建HttpGet对象
                HttpPost httpPost = new HttpPost(url);
                // 2.通过DefaultClient的excute方法执行返回一个HttpResponse对象
                HttpClient httpClient = new DefaultHttpClient();
                httpClient.getParams().setParameter(
                        CoreConnectionPNames.CONNECTION_TIMEOUT,
                        Settings.CONNECTTIMEMIDDLE);// 5秒连接超时
                httpClient.getParams().setParameter(
                        CoreConnectionPNames.SO_TIMEOUT,
                        Settings.CONNECTTIMEMIDDLE);// 5秒请求超时
                try {
                    httpResponse = httpClient.execute(httpPost);
                    // 3.取得相关信息
                    // 取得HttpEntiy
                    HttpEntity httpEntity = httpResponse.getEntity();
                    // 得到一些数据
                    // 通过EntityUtils并指定编码方式取到返回的数据
                    try {
                        result.append(EntityUtils.toString(httpEntity, "utf-8"));
                    } catch (ParseException e) {
                        result.append(e.getStackTrace().toString());
                        e.printStackTrace();
                    } catch (IOException e) {
                        result.append(e.getStackTrace().toString());
                        e.printStackTrace();
                    }
                    StatusLine statusLine = httpResponse.getStatusLine();
                    statusLine.getProtocolVersion();
                    int statusCode = statusLine.getStatusCode();
                    if (statusCode != 200) {
                        // Toast.makeText(context, "服务器返回错误：错误码-"+statusCode,
                        // Toast.LENGTH_LONG).show();
                        result.append(Settings.SERVERRETURNERROR + statusCode);
                        return null;
                    }
                } catch (ConnectTimeoutException e) {
                    // 请求超时会抛出InterruptedIOException异常,连接超时会抛出ConnectTimeoutException异常
                    // ConnectTimeoutException继承自InterruptedIOException，所以只要捕获ConnectTimeoutException就可以了
                    result.append(Settings.CONNECTTIMEOUT);
                    Log.e("EXCEPTION", e.getStackTrace().toString());
                    // Toast.makeText(context, "连接超时",
                    // Toast.LENGTH_LONG).show();
                } catch (ClientProtocolException e) {
                    result.append(Settings.CLIENTPROTOCOLERROR);
                    Log.e("EXCEPTION", e.getStackTrace().toString());
                } catch (IOException e) {
                    result.append(Settings.IOERROR);
                    Log.e("EXCEPTION", e.getStackTrace().toString());
                }

            } else {
                result.append(Settings.REQUESTTYPEERROR);
            }
        }

        return result.toString();

    }

    /**
     * UDP请求
     *
     * @param host
     * @param port
     * @param content
     * @return
     */
    public String UdpRequest(InetAddress host, int port, String content) {
        String response = null;
        try {
            DatagramSocket socket;
            DatagramPacket packet;
            byte[] re = content.getBytes();
            byte[] buffer = new byte[65536]; // maximum size of an IP packet
            // DatagramPacket incoming = new DatagramPacket(buffer,
            // buffer.length);
            socket = new DatagramSocket();
            // socket.
            packet = new DatagramPacket(re, re.length, host, port);
            socket.send(packet);
            packet = new DatagramPacket(buffer, buffer.length);
            socket.setSoTimeout(Settings.CONNECTTIMEMIDDLE);
            socket.receive(packet);
            response = new String(packet.getData());
            socket.close();
        } catch (UnknownHostException e) {
            response = Settings.UNKNOWNHOSTEXCEPTION;
            e.getStackTrace();
        } catch (SocketTimeoutException e) {
            response = Settings.CONNECTTIMEOUT;
            e.getStackTrace();
        } catch (Exception e) {
            response = Settings.CONNECTEXCEPTION;
            e.getStackTrace();
        }
        return response;
    }

}

package org.cug.util;

public class Settings {
    /*请求超时设置*/
    public final static int CONNECTTIMESHORT = 3000;// 请求超时设置（较短），单位毫秒
    public final static int CONNECTTIMEMIDDLE = 5000;// 请求超时设置（中间），单位毫秒
    public final static int CONNECTTIMELONG = 10000;// 请求超时设置（较长），单位毫秒

    /*返回的错误信息*/
    public final static String CONNECTTIMEOUT = "连接超时";
    public final static String UNKNOWNHOSTEXCEPTION = "无法解析的服务器地址";
    public final static String CONNECTEXCEPTION = "";
    public final static String REQUESTTYPEERROR = "请求类型错误";
    public final static String NETNOTAVAILABLE = "网络不可用";
    public final static String RETURNERROR = "回复错误";
    public final static String PARSEERROR = "解析出错";
    public final static String IOERROR = "输入输出错误";
    public final static String SERVERRETURNERROR = "服务器返回错误：错误码-";
    public final static String CLIENTPROTOCOLERROR = "客户端协议错误";

    /*请求WebService时的参数*/
    public final static String PROXYURL = "http://192.168.1.103:8080/PoliceWSNew/services/jwt.jws";// 前置机的IP
    public final static String WEBMETHODNAME = "execute";

    /*用户信息*/
    public static String USERID = "";// 登录用户ID
    public static String PASSWORD = "";// 密码
    public static String PHONEID = "";// 手机号
    public static String USERNAME = "";//用户姓名或者是车牌照

    /*测试模式标志*/
    public final static boolean TESTMODE = true;

    /* 解析成功标记 */
    public final static String SUCC = "ZDSUC";
    public final static String FAIL = "ZDFAL";

    /*地图操作的KEY*/
    public final static String KEY = "174A1E860A2E4856E379EE6879B86DC06769850B";

    public final static String DBDIRECTORY = "/dache/db/";


}
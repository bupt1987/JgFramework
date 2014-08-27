package com.zhaidaosi.game.jgframework.common.http;

import com.zhaidaosi.game.jgframework.common.excption.HttpException;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("unchecked")
public class BaseHttp {

    public static String GBK = "GBK";
    public static String GB2312 = "GB2312";
    public static String UTF8 = "UTF-8";
    public static String HEADER_HOST = "Host";
    public static String HEADER_REFERER = "Referer";

    private static final Logger log = LoggerFactory.getLogger(BaseHttp.class);
    private static int DEFAULT_TIMEOUT = 3000;
    private static String DEFAULT_CHARSET = "UTF-8";
    private static int DEFAULT_MAX_REDIRECT = 2;
    private static String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:12.0) Gecko/20100101 Firefox/22.0 zhaidaosi.com";

    public static int header(String url) throws Exception {
        return header(url, null, null, null, DEFAULT_TIMEOUT);
    }

    public static String get(String url) throws Exception {
        return get(url, null, null, DEFAULT_CHARSET, DEFAULT_MAX_REDIRECT, DEFAULT_TIMEOUT);
    }

    public static String get(String url, Map<String, String> header) throws Exception {
        return get(url, header, null, DEFAULT_CHARSET, DEFAULT_MAX_REDIRECT, DEFAULT_TIMEOUT);
    }

    public static String get(String url, String charset) throws Exception {
        return get(url, null, null, charset, DEFAULT_MAX_REDIRECT, DEFAULT_TIMEOUT);
    }

    public static String post(String url, Map<String, Object> params) throws Exception {
        return post(url, params, null, null, DEFAULT_CHARSET, DEFAULT_TIMEOUT);
    }

    public static String post(String url, Map<String, Object> params, int timeout) throws Exception {
        return post(url, params, null, null, DEFAULT_CHARSET, timeout);
    }

    public static String post(String url, Map<String, Object> params, Map<String, String> header) throws Exception {
        return post(url, params, header, null, DEFAULT_CHARSET, DEFAULT_TIMEOUT);
    }

    public static void sendRequest(String url) throws Exception {
        sendRequest(url, null, null, null, DEFAULT_CHARSET);
    }

    public static void sendRequest(String url, Map<String, Object> params) throws Exception {
        sendRequest(url, params, null, null, DEFAULT_CHARSET);
    }

    /**
     * 返回状态码
     *
     * @param url
     * @param header
     * @param cookie
     * @return
     * @throws HttpException
     */
    public static int header(String url, HashMap<String, String> header, BaseCookie[] cookie, String charset, int timeout) throws Exception {
        check(url);
        if (charset == null) {
            charset = DEFAULT_CHARSET;
        }
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("url", url);
        args.put("header", header);
        args.put("cookie", cookie);
        args.put("charset", charset);
        args.put("timeout", timeout);
        return doHeader(args);
    }

    /**
     * 模拟get
     *
     * @param url
     * @param header
     * @param cookie
     * @param charset
     * @param maxRedirect
     * @return
     * @throws HttpException
     */
    public static String get(String url, Map<String, String> header, BaseCookie[] cookie, String charset, int maxRedirect, int timeout) throws Exception {
        check(url);
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("url", url);
        args.put("header", header);
        args.put("cookie", cookie);
        args.put("charset", charset);
        args.put("maxRedirect", maxRedirect);
        args.put("timeout", timeout);
        return doGet(args, 0);
    }

    /**
     * 模拟post
     *
     * @param url
     * @param params
     *            可以是String和String[]
     * @param header
     * @param cookie
     * @param charset
     * @return
     * @throws HttpException
     */
    public static String post(String url, Map<String, Object> params, Map<String, String> header, BaseCookie[] cookie, String charset, int timeout) throws Exception {
        check(url);
        if (charset == null) {
            charset = DEFAULT_CHARSET;
        }
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("url", url);
        args.put("header", header);
        args.put("cookie", cookie);
        args.put("charset", charset);
        args.put("params", params);
        args.put("timeout", timeout);
        return doPost(args);
    }

    /**
     * 模拟http请求，不接受返回信息
     *
     * @param url
     * @param params
     *            可以是String和String[]
     * @param header
     * @param cookie
     * @param charset
     * @throws Exception
     */
    public static void sendRequest(String url, Map<String, Object> params, Map<String, String> header, BaseCookie[] cookie, String charset) throws Exception {
        check(url);
        if (charset == null) {
            charset = DEFAULT_CHARSET;
        }
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("url", url);
        args.put("header", header);
        args.put("cookie", cookie);
        args.put("charset", charset);
        args.put("params", params);
        doSendRequest(args);
    }

    private static void doSendRequest(Map<String, Object> args) throws URISyntaxException, UnknownHostException, IOException {
        String url = (String) args.get("url");
        HashMap<String, Object> params = (HashMap<String, Object>) args.get("params");
        HashMap<String, String> header = (HashMap<String, String>) args.get("header");
        BaseCookie[] cookie = (BaseCookie[]) args.get("cookie");
        String charset = (String) args.get("charset");
        URI uri = new URI(url);
        int port = uri.getPort();
        @SuppressWarnings("resource")
        Socket s = new Socket(uri.getHost(), port == -1 ? 80 : port);
        OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());
        StringBuffer sb = new StringBuffer();
        String postStr = null;
        if (params != null) {
            postStr = "";
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object val = entry.getValue();
                if (val instanceof String) {
                    postStr += (URLEncoder.encode(key, charset) + "=" + URLEncoder.encode((String) val, charset) + "&");
                } else if (entry.getValue() instanceof String[]) {
                    for (String value : (String[]) val) {
                        postStr += (URLEncoder.encode(key, charset) + "=" + URLEncoder.encode(value, charset) + "&");
                    }
                }
            }
            postStr = postStr.substring(0, postStr.length() - 1);
            sb.append("POST ");
        } else {
            sb.append("GET ");
        }
        String fullPath = uri.getQuery() == null ? uri.getPath() : uri.getPath() + "?" + uri.getQuery();
        sb.append(fullPath + " HTTP/1.1\r\n");
        sb.append("Host: " + uri.getHost() + "\r\n");
        sb.append("User-Agent: " + DEFAULT_USER_AGENT + "\r\n");
        sb.append("Connection: keep-alive\r\n");
        if (header != null) {
            for (Map.Entry<String, String> h : header.entrySet()) {
                sb.append(h.getKey() + ": " + h.getValue() + "\r\n");
            }
        }
        if (postStr != null) {
            sb.append("Content-type: application/x-www-form-urlencoded\r\n");
            sb.append("Content-Length: " + postStr.length() + "\r\n");
        }
        if (cookie != null) {
            int size = cookie.length;
            for (int i = 0; i < size; i++) {
                sb.append("Cookie: " + URLEncoder.encode(cookie[i].getKey(), charset) + "=" + URLEncoder.encode(cookie[i].getValue(), charset));
                if (i < size - 1) {
                    sb.append("; ");
                }
            }
        }
        sb.append("\r\nConnection: Close\r\n\r\n");
        if (postStr != null) {
            sb.append(postStr + "\r\n");
        }
        osw.write(sb.toString());
        osw.flush();
        osw.close();
    }

    /**
     * 模拟get
     *
     * @param args
     * @param retry
     *            重试第几次了
     * @return
     * @throws HttpException
     */
    private static String doGet(HashMap<String, Object> args, int retry) throws HttpException {
        int maxRedirect = (Integer) args.get("maxRedirect");
        String result = null;
        if (retry > maxRedirect) {
            return result;
        }
        String url = (String) args.get("url");
        HashMap<String, String> header = (HashMap<String, String>) args.get("header");
        BaseCookie[] cookie = (BaseCookie[]) args.get("cookie");
        String charset = (String) args.get("charset");
        Integer timeout = (Integer) args.get("timeout");
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);
        BaseHttp.init(client, method, header, cookie, charset, timeout);
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode == HttpStatus.SC_OK) {
                result = getResponse(method, charset);
            } else if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY || statusCode == HttpStatus.SC_SEE_OTHER
                    || statusCode == HttpStatus.SC_TEMPORARY_REDIRECT) {
                // 301, 302, 303, 307 跳转
                Header locationHeader = method.getResponseHeader("location");
                String location = null;
                if (locationHeader != null) {
                    location = locationHeader.getValue();
                    retry++;
                    args.put("url", location);
                    result = BaseHttp.doGet(args, retry);
                }
            }
        } catch (IOException e) {
            throw new HttpException("执行HTTP Get请求" + url + "时，发生异常！ => " + e.getMessage());
        } finally {
            method.releaseConnection();
        }
        return result;
    }

    /**
     * 模拟post
     *
     * @param args
     * @return
     * @throws HttpException
     */
    private static String doPost(HashMap<String, Object> args) throws HttpException {
        String result = null;
        String url = (String) args.get("url");
        HashMap<String, String> header = (HashMap<String, String>) args.get("header");
        BaseCookie[] cookie = (BaseCookie[]) args.get("cookie");
        String charset = (String) args.get("charset");
        Integer timeout = (Integer) args.get("timeout");
        HashMap<String, Object> params = (HashMap<String, Object>) args.get("params");
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(url);
        BaseHttp.init(client, method, header, cookie, charset, timeout);
        if (params != null) {
            NameValuePair[] data = new NameValuePair[params.size()];
            int i = 0;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object val = entry.getValue();
                if (val instanceof String) {
                    data[i] = new NameValuePair(key, (String) val);
                } else if (val instanceof String[]) {
                    for (String value : (String[]) val) {
                        data[i] = new NameValuePair(key, value);
                    }
                } else {
                    throw new HttpException("post 参数类型必须为 String 或 String[]");
                }
                i++;
            }
            method.setRequestBody(data);
        }
        try {
            if (client.executeMethod(method) == HttpStatus.SC_OK) {
                result = getResponse(method, charset);
            }
        } catch (IOException e) {
            throw new HttpException("执行HTTP Post请求" + url + "时，发生异常！ => " + e.getMessage());
        } finally {
            method.releaseConnection();
        }
        return result;
    }

    /**
     * 获取状态码
     *
     * @param args
     * @return
     * @throws HttpException
     */
    private static int doHeader(HashMap<String, Object> args) throws HttpException {
        int statusCode = 0;
        String url = (String) args.get("url");
        HashMap<String, String> header = (HashMap<String, String>) args.get("header");
        String charset = (String) args.get("charset");
        BaseCookie[] cookie = (BaseCookie[]) args.get("cookie");
        Integer timeout = (Integer) args.get("timeout");
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(url);
        BaseHttp.init(client, method, header, cookie, charset, timeout);
        try {
            statusCode = client.executeMethod(method);
        } catch (IOException e) {
            throw new HttpException("执行HTTP Header请求" + url + "时，发生异常！ => " + e.getMessage());
        } finally {
            method.releaseConnection();
        }
        return statusCode;
    }

    /**
     * 初始化
     *
     * @param client
     * @param method
     * @param headers
     * @param cookie
     * @throws UnsupportedEncodingException
     */
    private static void init(HttpClient client, HttpMethod method, HashMap<String, String> header, BaseCookie[] cookie, String charset, int timeout) {
        client.getParams().setParameter(HttpMethodParams.HTTP_URI_CHARSET, charset);
        client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, charset);
        client.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
        client.getHttpConnectionManager().getParams().setSoTimeout(timeout);
        method.setFollowRedirects(false);
        method.setRequestHeader("User-Agent", BaseHttp.DEFAULT_USER_AGENT);
        method.setRequestHeader("Cache-Control", "no-cache");
        method.setRequestHeader("Connection", "keep-alive");
        if (cookie != null) {
            String cookieString = "";
            for (BaseCookie ci : cookie) {
                try {
                    cookieString += URLEncoder.encode(ci.getKey(), charset) + "=" + URLEncoder.encode(ci.getValue(), charset) + "; ";
                } catch (UnsupportedEncodingException e) {
                    log.error(e.getMessage(), e);
                }
            }
            method.setRequestHeader("Cookie", cookieString);
        } else {
            method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        }
        if (header != null) {
            for (Entry<String, String> h : header.entrySet()) {
                method.setRequestHeader(h.getKey(), h.getValue());
            }
        }
    }

    /**
     * 获取http请求结果
     *
     * @param method
     * @param charset
     * @return
     * @throws IOException
     */
    private static String getResponse(HttpMethod method, String charset) {
        StringBuffer response = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), charset));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return response.toString();
    }

    /**
     * 检查url是否为空
     *
     * @param url
     * @throws HttpException
     */
    private static void check(String url) throws HttpException {
        if (url == null || url.trim().equals("")) {
            throw new HttpException("url不能为空");
        }
    }

}

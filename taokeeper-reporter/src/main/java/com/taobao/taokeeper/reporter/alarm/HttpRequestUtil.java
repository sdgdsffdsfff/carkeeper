package com.taobao.taokeeper.reporter.alarm;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

public class HttpRequestUtil {
    public static final String USER_AGENT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; .NET CLR 1.1.4322)";
    private static Log logger = LogFactory.getLog(HttpRequestUtil.class);

    public static String get(String url, String encoder) {
        HttpClient httpClient = new HttpClient();
        HttpConnectionManagerParams managerParams = httpClient.getHttpConnectionManager().getParams();

        managerParams.setConnectionTimeout(500 * 4); // 设置连接超时时间(单位毫秒)
        managerParams.setSoTimeout(500 * 4); // 设置读数据超时时间(单位毫秒)

        GetMethod getMethod = new GetMethod(url);
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        getMethod.getParams().setParameter(HttpMethodParams.USER_AGENT, USER_AGENT);
        getMethod.getParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
        try {
            int statusCode = httpClient.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
                logger.error("HttpRequest 返回 null");
                return null;
            }
            // byte[] arr = getMethod.getResponseBody();
            // return new String(arr, encoder);

            InputStream inputStream = getMethod.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, encoder));
            StringBuffer stringBuffer = new StringBuffer();
            String str = "";
            while ((str = br.readLine()) != null) {
                stringBuffer.append(str);
            }
            return stringBuffer.toString();

        } catch (HttpException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            getMethod.releaseConnection();
        }
    }

    public static String post(String url, String encoder, Map<String, Object> paramMap) {
        HttpClient httpClient = new HttpClient();
        HttpConnectionManagerParams managerParams = httpClient.getHttpConnectionManager().getParams();

        managerParams.setConnectionTimeout(500 * 1); // 设置连接超时时间(单位毫秒)
        managerParams.setSoTimeout(500 * 1); // 设置读数据超时时间(单位毫秒)

        PostMethod postMethod = new PostMethod(url);
        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        postMethod.getParams().setParameter(HttpMethodParams.USER_AGENT, USER_AGENT);
        postMethod.getParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
        postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, encoder);

        ArrayList<NameValuePair> pairList = new ArrayList<NameValuePair>();
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            String value = String.valueOf(entry.getValue());
            pairList.add(new NameValuePair(key, value));
        }
        postMethod.setRequestBody(pairList.toArray(new NameValuePair[0]));

        try {
            int statusCode = httpClient.executeMethod(postMethod);
            if (statusCode != HttpStatus.SC_OK) {
                logger.error("HttpRequest 返回 null");
                return null;
            }
            InputStream inputStream = postMethod.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, encoder));
            StringBuffer stringBuffer = new StringBuffer();
            String str = "";
            while ((str = br.readLine()) != null) {
                stringBuffer.append(str);
            }
            return stringBuffer.toString();

        } catch (HttpException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            postMethod.releaseConnection();
        }
    }

}

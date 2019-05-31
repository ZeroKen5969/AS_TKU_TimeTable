package com.example.as_tku_timetable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WWW {

    private enum Method {
        GET,
        POST
    }

    private Map<String, String> headers;
    private List<String> cookies;

    public WWW() {
        headers = new Hashtable<String, String>();
        cookies = new LinkedList<String>();
    }

    //使用者可從這加入客製化header
    public void addHeader(String header, String data) {
        if(header.equals("Cookie")) {
            cookies.add(data);
        } else {
            headers.put(header, data);
        }
    }

    //負責發送request及讀取response
    private String readWebPage(HttpURLConnection con) {
        try {
            /*******設置response cookie*******/
            Map<String, List<String>> hds = con.getHeaderFields();
            if(hds.containsKey("Set-Cookie")) {
                for(String cookie : hds.get("Set-Cookie")) {
                    cookies.add(cookie.split(";", 2)[0].trim());
                }
            }

            ///自動重定向
            if(con.getResponseCode() == 302) {
                String redirUrl = con.getHeaderField("location");
                return sendGet(redirUrl);
                //System.out.println(ul);
            }

            StringBuffer buf = new StringBuffer();
            InputStream is = con.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while((line = in.readLine()) != null) {
                buf.append(line);
            }
            is.close();
            in.close();
            return buf.toString();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //用於組裝所有cookie
    private String combineCookies() {
        StringBuffer buf = new StringBuffer();
        for (String cookie : cookies) {
            if (buf.length() != 0) buf.append(' ');
            buf.append(cookie);
            if(!cookie.endsWith(";")) buf.append(";");
        }
        return buf.toString();
    }

    //用於製作request
    private HttpURLConnection makeRequest(Method method, String url, Hashtable<String,String> postData) {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection)new URL(url).openConnection();
            con.setRequestMethod(method.name());

            /*******組裝cookies*******/
            if(cookies.size() > 0) {
                con.setRequestProperty("Cookie", combineCookies());
            }

            /*******設置使用者header*******/
            for(Map.Entry<String, String> header : headers.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            /*******設置預設header*******/
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
            con.setInstanceFollowRedirects(false);
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);

            if(method == Method.POST) {
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setDoOutput(true);
                StringBuffer buf = new StringBuffer();
                /*******組裝post data*******/
                for (Map.Entry<String, String> param : postData.entrySet()) {
                    if (buf.length() != 0) buf.append('&');
                    buf.append(param.getKey());
                    buf.append('=');
                    buf.append(param.getValue());
                }
                byte[] postDataBytes = buf.toString().getBytes("UTF-8");
                con.getOutputStream().write(postDataBytes);
            }
            //System.out.println(con.getRequestProperties());
            //System.out.println(con.getResponseCode());
            //System.out.println(con.getHeaderFields());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return con;
    }

    //用來發送GET請求
    public String sendGet(String path) {
        HttpURLConnection con = makeRequest(Method.GET, path, null);
        return con != null ? readWebPage(con) : null;
    }

    //用來發送POST請求
    public String sendPost(String path, Hashtable<String, String> postData) {
        HttpURLConnection con = makeRequest(Method.POST, path, postData);
        return con != null ? readWebPage(con) : null;
    }
}

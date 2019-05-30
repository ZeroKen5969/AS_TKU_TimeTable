package com.example.as_tku_timetable;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Hashtable;


public class LoginActivity extends AppCompatActivity {

    private TextView usr_editor;
    private TextView psw_editor;
    private Button lg_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usr_editor = findViewById(R.id.usr_editor);
        psw_editor = findViewById(R.id.psw_editor);
        findViewById(R.id.lg_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });
    }

    //private static String LOGIN_PAGE = "https://sso.tku.edu.tw/NEAI/logineb.jsp?myurl=https://sso.tku.edu.tw/aissinfo/emis/tmw0012.aspx";
    private String LOGIN_VIDCODE = "https://sso.tku.edu.tw/NEAI/ImageValidate";
    private String LOGIN_ACTION = "https://sso.tku.edu.tw/NEAI/login2.do?action=EAI";
    private String TIME_TABLE = "https://sso.tku.edu.tw/aissinfo/emis/TMWC090_result.aspx?YrSem=1072&stu_no=";
    private WWW www = new WWW();
    private void Login() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String response;
                Hashtable<String, String> data;
                //www.SendGet(LOGIN_PAGE);

                /*******取得驗證碼*******/
                www.SendGet(LOGIN_VIDCODE);
                data = new Hashtable<>();
                data.put("outType", "2");
                response = www.SendPost(LOGIN_VIDCODE, data);
                //System.out.println(response);

                /*******登入*******/
                final String usr = usr_editor.getText().toString();
                final String psw = psw_editor.getText().toString();
                data = new Hashtable<>();
                data.put("myurl", "https://sso.tku.edu.tw/aissinfo/emis/tmw0012.aspx");
                data.put("ln", "zh_TW");
                data.put("embed", "No");
                data.put("logintype", "logineb");
                data.put("username", usr);
                data.put("password", psw);
                data.put("vidcode", response);
                data.put("loginbtn", "登入");
                response = www.SendPost(LOGIN_ACTION, data);

                TextView tv = findViewById(R.id.textView4);
                if(response.contains("錯誤")) {
                    tv.setText("帳號或密碼輸入錯誤，請重新輸入");
                    System.out.println("帳號或密碼輸入錯誤，請重新輸入");
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            /*******新增帳號欄位*******/
                            ListData listData = new ListData();
                            listData.isCheck = false;
                            listData.content = usr;
                            listData.usr = usr;
                            listData.psw = psw;
                            MainActivity.AddData(listData);
                        }
                    });

                    /*******進入課表畫面*******/
                    www.SendGet(TIME_TABLE + usr); //要get兩次
                    response = www.SendGet(TIME_TABLE + usr);
                    //tv.setText(response);
                    System.out.println(response);
                    getTable(response);
                }
                //System.out.println(response);

            }
        }).start();
        TextView tv = findViewById(R.id.textView4);
        tv.setMovementMethod(new ScrollingMovementMethod());
    }

    private String getTable(String respond){
        String result = "";
        Document doc = Jsoup.parse(respond);
        Element table = doc.getElementById("Table1");
        Elements rows = table.getElementsByTag("tr");

        for(Element row : rows){
            Elements eles =row.getElementsByTag("td");
            for(Element ele : eles){
                    if(ele.text() != ""){
                        result += ele.text();
                    }
                    else{
                        result += "space";
                    }
            }
            result += "\n";
        }

        System.out.println(result);

        return result;
    }
}

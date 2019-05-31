package com.example.as_tku_timetable;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Hashtable;

public class LoginActivity extends AppCompatActivity {

    private static final String LOGIN_VIDCODE = "https://sso.tku.edu.tw/NEAI/ImageValidate";
    private static final String LOGIN_ACTION = "https://sso.tku.edu.tw/NEAI/login2.do?action=EAI";
    private static final String TIME_TABLE = "https://sso.tku.edu.tw/aissinfo/emis/TMWC090_result.aspx?YrSem=1072&stu_no=";
    private static final WWW www = new WWW();

    private TextView usr_editor;
    private TextView psw_editor;
    private static TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usr_editor = findViewById(R.id.usr_editor);
        psw_editor = findViewById(R.id.psw_editor);
        tv = findViewById(R.id.textView4);
        findViewById(R.id.lg_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login(usr_editor.getText().toString(), psw_editor.getText().toString());
            }
        });
    }

    private void Login(final String usr, final String psw) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String response;
                Hashtable<String, String> data;

                /*******取得驗證碼*******/
                www.SendGet(LOGIN_VIDCODE);
                data = new Hashtable<>();
                data.put("outType", "2");
                response = www.SendPost(LOGIN_VIDCODE, data);
                if(response == null) {
                    tv.setText("登入失敗, 請重新嘗試!");
                    System.out.println("登入失敗, 請重新嘗試!");
                    return;
                }

                /*******登入*******/
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
                if(response == null) {
                    tv.setText("登入失敗, 請重新嘗試!");
                    System.out.println("登入失敗, 請重新嘗試!");
                    return;
                }

                if(response.contains("錯誤")) {
                    tv.setText("帳號或密碼輸入錯誤，請重新輸入");
                    System.out.println("帳號或密碼輸入錯誤，請重新輸入");
                } else {
                    /*******取得課表資料*******/
                    www.SendGet(TIME_TABLE + usr); //要get兩次
                    response = www.SendGet(TIME_TABLE + usr);
                    if(response == null) {
                        tv.setText("登入失敗, 請重新嘗試!");
                        System.out.println("登入失敗, 請重新嘗試!");
                        return;
                    }

                    /*******傳遞課表資料*******/
                    ArrayList<String> timeTable = GetTimeTable(response);
                    Intent intent = new Intent(LoginActivity.this, TimeTableActivity.class);
                    SaveBundle saveBundle = new SaveBundle();
                    saveBundle.usr = usr;
                    saveBundle.timeTable = timeTable;
                    intent.putExtra("saveBundle", saveBundle);
                    startActivity(intent);
                    LoginActivity.this.finish();
                }
                //System.out.println(response);

            }
        }).start();
    }

    private static ArrayList<String> GetTimeTable(String response){
        ArrayList<String> timeTable = new ArrayList<>();
        Document doc = Jsoup.parse(response);
        Elements datas = doc.select("#Table1 > tbody > tr > td");

        for(Element data : datas){
            timeTable.add(data.text());
        }
        return timeTable;
    }
}

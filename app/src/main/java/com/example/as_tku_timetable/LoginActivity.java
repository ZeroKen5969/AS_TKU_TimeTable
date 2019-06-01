package com.example.as_tku_timetable;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class LoginActivity extends AppCompatActivity {


    private static final String LOGIN_VIDCODE = "https://sso.tku.edu.tw/NEAI/ImageValidate";
    private static final String LOGIN_ACTION = "https://sso.tku.edu.tw/NEAI/login2.do?action=EAI";
    private static final String TIME_TABLE = "https://sso.tku.edu.tw/aissinfo/emis/TMWC090_result.aspx?YrSem=1072&stu_no=";
    private static final WWW www = new WWW();
    private static final int LOGIN_ERROR = 0x00;
    private static final int NET_ERROR = 0x01;
    private static final int DIALOG_GET_TABLE = 0x10;
    private static final int DIALOG_CREATE_TABLE = 0x11;
    private static final int DIALOG_CLOSE = 0x12;

    private TextView usr_editor;
    private TextView psw_editor;
    private TextView terminal;
    private View dialogView;
    private TextView dialogText;
    private AlertDialog alertDialog;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case LOGIN_ERROR :
                    terminal.setText("帳號或密碼輸入錯誤，請重新輸入!");
                    break;
                case NET_ERROR:
                    terminal.setText("登入失敗, 請重新嘗試!");
                    break;
                case DIALOG_GET_TABLE:
                    dialogText.setText("取得課表中...");
                    break;
                case DIALOG_CREATE_TABLE:
                    dialogText.setText("建構課表中...");
                    break;
                case DIALOG_CLOSE:
                    alertDialog.dismiss();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dialogView = LayoutInflater.from(this).inflate(R.layout.progress_bar, null);
        dialogText = dialogView.findViewById(R.id.textView);
        alertDialog = createProgressDialog(dialogView);
        usr_editor = findViewById(R.id.usr_editor);
        psw_editor = findViewById(R.id.psw_editor);
        terminal = findViewById(R.id.terminal);
        findViewById(R.id.lg_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogText.setText("登入中...");
                login(usr_editor.getText().toString(), psw_editor.getText().toString());
                alertDialog.show();
            }
        });
    }

    private void login(final String usr, final String psw) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String response;
                Hashtable<String, String> data;

                /*******取得驗證碼*******/
                www.sendGet(LOGIN_VIDCODE);
                data = new Hashtable<>();
                data.put("outType", "2");
                response = www.sendPost(LOGIN_VIDCODE, data);
                if(response == null) {
                    handler.sendEmptyMessage(NET_ERROR);
                    handler.sendEmptyMessage(DIALOG_CLOSE);
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
                response = www.sendPost(LOGIN_ACTION, data);
                if(response == null) {
                    handler.sendEmptyMessage(NET_ERROR);
                    handler.sendEmptyMessage(DIALOG_CLOSE);
                    System.out.println("登入失敗, 請重新嘗試!");
                    return;
                }

                if(response.contains("錯誤")) {
                    handler.sendEmptyMessage(LOGIN_ERROR);
                    handler.sendEmptyMessage(DIALOG_CLOSE);
                    System.out.println("帳號或密碼輸入錯誤，請重新輸入");
                } else {
                    /*******取得課表資料*******/
                    handler.sendEmptyMessage(DIALOG_GET_TABLE);
                    www.sendGet(TIME_TABLE + usr); //要get兩次
                    response = www.sendGet(TIME_TABLE + usr);
                    if(response == null) {
                        handler.sendEmptyMessage(NET_ERROR);
                        handler.sendEmptyMessage(DIALOG_CLOSE);
                        System.out.println("登入失敗, 請重新嘗試!");
                        return;
                    }

                    /*******傳遞課表資料*******/
                    handler.sendEmptyMessage(DIALOG_CREATE_TABLE);
                    List<String> timeTable = getTimeTable(response);
                    Intent intent = new Intent(LoginActivity.this, TimeTableActivity.class);
                    intent.putExtra("saveBundle", new SaveBundle(usr, timeTable));
                    startActivity(intent);
                    finish();
                }
                handler.sendEmptyMessage(DIALOG_CLOSE);
            }
        }).start();
    }

    private AlertDialog createProgressDialog(View view) {
        return new AlertDialog.Builder(this)
                .setTitle("努力加載中")
                .setView(view)
                .setCancelable(false)
                .create();
    }

    private List<String> getTimeTable(String response){
        List<String> timeTable = new ArrayList<>();
        Document doc = Jsoup.parse(response);
        Elements datas = doc.select("#Table1 > tbody > tr > td");

        for(Element data : datas){
            timeTable.add(data.text());
        }
        return timeTable;
    }

    @Override
    protected void onStop() {
        MainActivity.saveUserInfo(this);
        super.onStop();
    }
}

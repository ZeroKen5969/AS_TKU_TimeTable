package com.example.as_tku_timetable;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*******用於保存主頁訊息*******/
class ListData {
    public String usr;
    public String psw;
    public String content;
    public boolean isCheck;
}

/*******要存檔的東西*******/
class SaveBundle implements Serializable {
    public String usr;
    public String psw;
}

public class MainActivity extends AppCompatActivity {

    public enum MenuState {
        Normal,
        Delete
    }

    public static MenuState menuState = MenuState.Normal;
    private static List<ListData> listDatas;
    private static RecyclerView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listDatas = LoadUserInfo();
        if(listDatas == null) {
            listDatas = new ArrayList<>();
        }

        /*******建構主頁內容*******/
        view = findViewById(R.id.list_view);
        view.setAdapter(new MyAdapter(listDatas));
        view.setLayoutManager(new LinearLayoutManager(this));
        view.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        /*******toorbar*******/
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*******add button*******/
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }

    private MenuItem action_cancel;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        action_cancel = menu.findItem(R.id.action_cancel);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.action_settings:
                return true;
            case R.id.action_delete:
                if(menuState == MenuState.Normal) {
                    action_cancel.setVisible(true);
                    menuState = MenuState.Delete;
                } else if (menuState == MenuState.Delete) {
                    /*******必須使用temp儲存要刪掉的東西, 否則直接刪除會發生異常*******/
                    List<ListData> tempData = new ArrayList<>();
                    for(ListData data : listDatas) {
                        if(data.isCheck) tempData.add(data);
                    }
                    listDatas.removeAll(tempData);
                }
                view.getAdapter().notifyDataSetChanged();
                break;
            case R.id.action_cancel:
                action_cancel.setVisible(false);
                for(ListData data : listDatas) {
                    data.isCheck = false;
                }
                menuState = MenuState.Normal;
                view.getAdapter().notifyDataSetChanged();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void AddData(ListData listData) {
        listDatas.add(listData);
        view.getAdapter().notifyItemInserted(listDatas.size() - 1);
    }

    private static final String SAVE_FILE = "UserInfo";
    public void SaveUserInfo() {
        try {
            List<SaveBundle> saveBundles = new ArrayList<>();
            for (ListData data : listDatas) {
                SaveBundle saveBundle = new SaveBundle();
                saveBundle.usr = AESCrypt.Encrypt(data.usr);
                saveBundle.psw = AESCrypt.Encrypt(data.psw);
                saveBundles.add(saveBundle);
            }

            ObjectOutputStream ow = new ObjectOutputStream(openFileOutput(SAVE_FILE, Context.MODE_PRIVATE));
            ow.writeObject(saveBundles);
            ow.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public List<ListData> LoadUserInfo() {
        try {
            ObjectInputStream or = new ObjectInputStream(openFileInput(SAVE_FILE));
            List<SaveBundle> saveBundles = (List<SaveBundle>) or.readObject();
            or.close();

            List<ListData> list = new ArrayList<>();
            for (SaveBundle saveBundle : saveBundles) {
                ListData listData = new ListData();
                listData.usr = AESCrypt.Decrypt(saveBundle.usr);
                listData.psw = AESCrypt.Decrypt(saveBundle.psw);
                listData.isCheck = false;
                listData.content = AESCrypt.Decrypt(saveBundle.usr);
                list.add(listData);
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();

        }

        return null;
    }

    @Override
    protected void onStop() {
        SaveUserInfo();
        System.out.println("stop");
        super.onStop();
    }
}
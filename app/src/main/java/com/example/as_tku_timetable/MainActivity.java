package com.example.as_tku_timetable;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*******用於保存主頁訊息*******/
class ListData {
    public String usr;
    public String title;
    public boolean isCheck;
    public List<String> timeTable;

    public ListData(String usr, String title, boolean isCheck, List<String> timeTable) {
        this.usr = usr;
        this.title = title;
        this.isCheck = isCheck;
        this.timeTable = timeTable;
    }

    public ListData(SaveBundle saveBundle) {
        usr = saveBundle.usr;
        title = saveBundle.usr;
        timeTable = saveBundle.timeTable;
        isCheck = false;
    }
}

/*******要存檔的東西*******/
class SaveBundle implements Serializable {
    public List<String> timeTable;
    public String usr;

    public SaveBundle(String usr, List<String> timeTable) {
        this.usr = usr;
        this.timeTable = timeTable;
    }

    public SaveBundle(ListData listData) {
        timeTable = listData.timeTable;
        usr = listData.usr;
    }
}

public class MainActivity extends AppCompatActivity {

    public enum MenuState {
        Normal,
        Delete
    }

    public static MenuState menuState;
    private static List<ListData> listDatas;
    private static RecyclerView view;
    private FloatingActionButton floatingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuState = MenuState.Normal;
        listDatas = loadUserInfo(this);
        if(listDatas == null) {
            listDatas = new ArrayList<>();
        }

        floatingBtn = findViewById(R.id.fab);
        /*******建構主頁內容*******/
        view = findViewById(R.id.list_view);
        view.setAdapter(new MainActivityAdapter(listDatas));
        view.setLayoutManager(new LinearLayoutManager(this));
        view.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0) {
                    floatingBtn.animate().translationY(
                            floatingBtn.getHeight() + ((ViewGroup.MarginLayoutParams)floatingBtn.getLayoutParams()).bottomMargin
                    ).setInterpolator(new LinearInterpolator()).start();
                } else {
                    floatingBtn.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
                };
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        /*******toorbar*******/
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*******add button*******/
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(action_cancel);
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

    public static void addData(ListData listData) {
        listDatas.add(listData);
        view.getAdapter().notifyItemInserted(listDatas.size() - 1);
    }

    public static ListData getData(String usr) {
        for(ListData listData : listDatas) {
            if(listData.usr.equals(usr)) return listData;
        }
        return null;
    }

    private static final String SAVE_FILE = "UserInfo";
    public static void saveUserInfo(Context context) {
        try {
            /*******同步資料*******/
            List<SaveBundle> saveBundles = new ArrayList<>();
            for (ListData data : listDatas) {
                saveBundles.add(new SaveBundle(data));
            }

            /*******存檔及加密*******/
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            ObjectOutputStream ow = new ObjectOutputStream(buf);
            ow.writeObject(saveBundles);
            ow.close();
            byte[] bytes = buf.toByteArray();
            String encryptStr = AESCrypt.encrypt(new String(bytes, "ISO_8859_1"));
            ObjectOutputStream fw =  new ObjectOutputStream(context.openFileOutput(SAVE_FILE, MODE_PRIVATE));
            fw.writeUTF(encryptStr);
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static List<ListData> loadUserInfo(Context context) {
        try {
            /*******讀檔及解密*******/
            ObjectInputStream fr = new ObjectInputStream(context.openFileInput(SAVE_FILE));
            String encryptStr = fr.readUTF();
            fr.close();
            String decryptStr = AESCrypt.decrypt(encryptStr);
            ByteArrayInputStream buf = new ByteArrayInputStream(decryptStr.getBytes("ISO_8859_1"));
            ObjectInputStream or = new ObjectInputStream(buf);
            List<SaveBundle> saveBundles = (List<SaveBundle>) or.readObject();
            or.close();

            /*******同步資料*******/
            List<ListData> list = new ArrayList<>();
            for (SaveBundle saveBundle : saveBundles) {
                list.add(new ListData(saveBundle));
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onStop() {
        saveUserInfo(this);
        super.onStop();
    }
}
package com.example.as_tku_timetable;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class TimeTableActivity extends AppCompatActivity {

    //最多能顯示的行列數
    private static final int MAX_ROW = 8;
    private static final int MAX_COL = 15;
    //要顯示的行列數
    private static final int ROW = 6;
    private static final int COL = MAX_COL;

    private TableLayout tableLayout;
    private SaveBundle saveBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);

        tableLayout = findViewById(R.id.time_table);

        Bundle bundle = getIntent().getExtras();
        saveBundle = (SaveBundle)bundle.get("saveBundle");
        createTable();

        final View saveBtn = findViewById(R.id.save_btn);
        if(bundle.containsKey("flag")) saveBtn.setVisibility(View.INVISIBLE);
        else {
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ListData listData = MainActivity.getData(saveBundle.usr);
                    if (listData == null) {
                        listData = new ListData(saveBundle);
                        MainActivity.addData(listData);
                    }
                    listData.timeTable = saveBundle.timeTable;
                    Toast.makeText(TimeTableActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    saveBtn.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    private String formatText(String content) {
        return content.replaceAll(" [^ ]+(?=_[^_]*$)", "") //刪除老師名稱
                .replaceAll(" (?=.*_.*)|_", "\n"); //改成每次空格或底線就換行
    }

    private void createTable() {
        saveBundle.timeTable.set(0, "");

        /*******創建內容*******/
        for(int i = 0; i < COL ; ++i) {
            TableRow tableRow = new TableRow(this);
            for(int j = 0; j < ROW; ++j) {
                TextView textView = new TextView(this);
                textView.setWidth(100);
                textView.setHeight(i == 0 ? 70 : 350);
                textView.setPadding(5, 5, 5, 5);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                textView.setText(formatText(saveBundle.timeTable.get(i * MAX_ROW + j)));
                tableRow.addView(textView);
            }
            tableLayout.addView(tableRow);
        }
    }

    @Override
    protected void onStop() {
        MainActivity.saveUserInfo(this);
        super.onStop();
    }
}

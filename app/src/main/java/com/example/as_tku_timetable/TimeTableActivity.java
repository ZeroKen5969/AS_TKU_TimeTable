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
        saveBundle = (SaveBundle)getIntent().getExtras().get("saveBundle");
        createTable();

        findViewById(R.id.save_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListData listData = MainActivity.GetData(saveBundle.usr);
                if(listData == null) {
                    listData = new ListData();
                    listData.title = saveBundle.usr;
                    listData.usr = saveBundle.usr;
                    MainActivity.AddData(listData);
                }
                listData.timeTable = saveBundle.timeTable;
                Toast.makeText(TimeTableActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatText(String content) {
        return content.replaceAll(" [^ ]+_.*", "") //刪除教室位置和老師名稱
                      .replaceAll(" ", "\n"); //改成每次空格就換行
    }

    private void createTable() {
        for(int i = 0; i < COL ; ++i) {
            TableRow tableRow = new TableRow(this);
            tableLayout.addView(tableRow);
            for(int j = 0; j < ROW; ++j) {
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
                TextView textView = new TextView(this);
                textView.setWidth(0);
                textView.setHeight(400);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                textView.setText(formatText(saveBundle.timeTable.get(i * MAX_ROW + j)));
                tableRow.addView(textView);
            }
        }
    }
}

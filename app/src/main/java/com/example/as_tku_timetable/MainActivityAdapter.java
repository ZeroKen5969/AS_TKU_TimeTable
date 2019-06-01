package com.example.as_tku_timetable;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class MainActivityAdapter extends RecyclerView.Adapter<MainActivityAdapter.ViewHolder> {

    private List<ListData> data;

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv;
        CheckBox cb;

        public ViewHolder(View v) {
            super(v);
            tv = v.findViewById(R.id.text);
            cb = v.findViewById(R.id.checkbox);
        }
    }

    public MainActivityAdapter(List<ListData> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.content_main, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final ListData viewData = data.get(position);
        viewHolder.tv.setText(viewData.title);
        viewHolder.cb.setVisibility(MainActivity.menuState == MainActivity.MenuState.Delete ? View.VISIBLE : View.INVISIBLE);

        /*******要設定為按下按鈕時改變狀態, 否則會造成checkbox錯亂*******/
        viewHolder.cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewData.isCheck = viewHolder.cb.isChecked();
            }
        });
        /*******不可添加到onClick, 否則會發生錯亂*******/
        viewHolder.cb.setChecked(viewData.isCheck);

        /*******開啟課表頁面*******/
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), TimeTableActivity.class);
                intent.putExtra("saveBundle", new SaveBundle(viewData.title, viewData.timeTable));
                intent.putExtra("flag", true);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}

package com.example.as_tku_timetable;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

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

    public MyAdapter(List<ListData> data) {
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
        viewHolder.tv.setText(viewData.content);
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

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Item " + position + " is clicked.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void AddData(ListData dt) {
        data.add(dt);
        notifyDataSetChanged();
    }
}

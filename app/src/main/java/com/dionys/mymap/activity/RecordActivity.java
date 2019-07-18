package com.dionys.mymap.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dionys.mymap.R;
import com.dionys.mymap.db.DBHelper;
import com.dionys.mymap.entity.PathRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * 展示记录列表的页面
 *
 */
public class RecordActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private RecordAdapter mAdapter;
    private ListView mAllRecordListView;
    private DBHelper mDataBaseHelper;
    private List<PathRecord> mAllRecord = new ArrayList<>();
    public static final String RECORD_ID = "record_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_list);
        mAllRecordListView = findViewById(R.id.list_record);
        mDataBaseHelper = new DBHelper(this);
        mDataBaseHelper.open();
        searchAllRecordFromDB();
        mAdapter = new RecordAdapter(this, mAllRecord);
        mAllRecordListView.setAdapter(mAdapter);
        mAllRecordListView.setOnItemClickListener(this);
        init();
     }

    private void init() {
        // 设置状态栏颜色及字体颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // 设置状态栏透明
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // 设置状态栏字体黑色
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private void searchAllRecordFromDB() {
        mAllRecord = mDataBaseHelper.queryRecordAll();
    }

    public void onBackClick(View view) {
        this.finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 获取记录
        PathRecord recordItem = (PathRecord) parent.getAdapter().getItem(position);

        Intent intent = new Intent(RecordActivity.this,
                RecordShowActivity.class);
        // 传入记录的id
        intent.putExtra(RECORD_ID, recordItem.getId());
        startActivity(intent);
    }
}


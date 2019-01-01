package com.runing.urilslibtest.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.runing.urilslibtest.R;
import com.runing.utilslib.adapter.BaseRvAdapter;
import com.runing.utilslib.adapter.HeaderRvAdapter;

import java.util.ArrayList;
import java.util.List;

public class HeaderRvAdapterCallTest {

  private void test(Context context) {
    RecyclerView recyclerView = new RecyclerView(context);
    List<String> data = new ArrayList<>();

    /* 首先创建原始 RecyclerView.Adapter（这里是用的封装的Adapter） */
    BaseRvAdapter adapter = new BaseRvAdapter(data, R.layout.test_item_layout) {
      @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // ...
      }
    };
    adapter.setOnItemClickListener(new BaseRvAdapter.OnItemClickListener() {
      @Override public void onItemClick(View view, int position) {
        // ...
      }
    });
    // ...

    /* 使用 HeaderRvAdapter 包装原始 Adapter */
    HeaderRvAdapter headerRvAdapter = new HeaderRvAdapter(adapter);
    /* 添加 Header 或 Footer */
    headerRvAdapter.addHeaderView(new Button(context));
    headerRvAdapter.addFooterView(new TextView(context));
    headerRvAdapter.addFooterView(new TextView(context));

    recyclerView.setAdapter(headerRvAdapter);

    /* 可以直接用 */
    HeaderRvAdapter rvAdapter = new HeaderRvAdapter();
    rvAdapter.addHeaderView(new Button(context));
    rvAdapter.addFooterView(new TextView(context));
    rvAdapter.addFooterView(new TextView(context));
    recyclerView.setAdapter(rvAdapter);
  }
}

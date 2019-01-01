package com.runing.urilslibtest.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.runing.urilslibtest.R;
import com.runing.utilslib.adapter.BaseLvAdapter;
import com.runing.utilslib.adapter.BaseRvAdapter;

import java.util.ArrayList;
import java.util.List;

public class BaseAdapterCallTest {

  public static void test(Context context) {

    List<String> data = new ArrayList<>();
    ListView listView = new ListView(context);

    /* 单类型布局 */
    BaseLvAdapter lvAdapter = new BaseLvAdapter(data, R.layout.test_item_layout) {
      @Override public void onBindViewHolder(ViewHolder holder, int position) {
        /* 泛型使代码更优雅，子 View 缓存避免了每次的 findView */
        ImageView imageView = holder.getView(R.id.iv_item);
        TextView textView = holder.getView(R.id.tv_item);
        Button button = holder.getView(R.id.btn_item);

        imageView.setImageResource(R.mipmap.ic_launcher);
        textView.setText("text");
        button.setText("button");
      }
    };

    /* 多类型布局 */
    BaseLvAdapter lvAdapter2 = new BaseLvAdapter(data, new BaseLvAdapter.MultiType() {
      @Override public int getItemLayoutId(int position) {
        /* 根据 position 返回对应布局 */
        if (position == 0) {
          return R.layout.test_item_layout;
        } else {
          return R.layout.test_item_layout2;
        }
      }

      @Override public int getItemTypeCount() {
        return 2;
      }
    }) {
      @Override public void onBindViewHolder(ViewHolder holder, int position) {
        /* 根据布局类型分别处理 */
        switch (getItemViewType(position)) {
        case R.layout.test_item_layout:
          ImageView imageView = holder.getView(R.id.iv_item);

          imageView.setImageResource(R.mipmap.ic_launcher);
          break;
        case R.layout.test_item_layout2:
          TextView textView = holder.getView(R.id.tv_item);
          Button button = holder.getView(R.id.btn_item);

          textView.setText("text");
          button.setText("button");
          break;
        }
      }
    };

    RecyclerView recyclerView = new RecyclerView(context);

    /* 单类型布局 */
    BaseRvAdapter rvAdapter = new BaseRvAdapter(data, R.layout.test_item_layout) {

      @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView imageView = holder.getView(R.id.iv_item);
        TextView textView = holder.getView(R.id.tv_item);
        Button button = holder.getView(R.id.btn_item);

        imageView.setImageResource(R.mipmap.ic_launcher);
        textView.setText("text");
        button.setText("button");
      }
    };

    /* 多类型布局 */
    BaseRvAdapter rvAdapter2 = new BaseRvAdapter(data, new BaseRvAdapter.MultiType() {
      @Override public int getItemLayoutId(int position) {
        /* 根据 position 返回对应布局 */
        if (position == 0) {
          return R.layout.test_item_layout;
        } else {
          return R.layout.test_item_layout2;
        }
      }
    }) {
      @Override public void onBindViewHolder(ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
        case R.layout.test_item_layout:
          ImageView imageView = holder.getView(R.id.iv_item);

          imageView.setImageResource(R.mipmap.ic_launcher);
          break;
        case R.layout.test_item_layout2:
          TextView textView = holder.getView(R.id.tv_item);
          Button button = holder.getView(R.id.btn_item);

          textView.setText("text");
          button.setText("button");
          break;
        }
      }
    };

    rvAdapter.setOnItemClickListener(new BaseRvAdapter.OnItemClickListener() {
      @Override public void onItemClick(View view, int position) { }
    });

    rvAdapter.setOnItemLongClickListener(new BaseRvAdapter.OnItemLongClickListener() {
      @Override public void onItemLongClick(View view, int position) { }
    });
  }
}

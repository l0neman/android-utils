# BaseLvAdapter

[源码 - BaseLvAdapter.java](./BaseLvAdapter.java)

1. 精简原始 BaseAdapter 的包含 ViewHolder 在内的重复代码。
2. 加入泛型和对 Item 子 View 的缓存，避免繁琐操作，凸显出核心逻辑。

- 单类型布局

```java
/* 单类型布局 */
BaseLvAdapter lvAdapter = new BaseLvAdapter(data, R.layout.test_item_layout) {
  @Override public void onBindViewHolder(ViewHolder holder, int position) {
    /* 泛型的加入使代码更优雅，子 View 缓存避免了每次的 findView */
    ImageView imageView = holder.getView(R.id.iv_item);
    TextView textView = holder.getView(R.id.tv_item);
    Button button = holder.getView(R.id.btn_item);

    imageView.setImageResource(R.mipmap.ic_launcher);
    textView.setText("text");
    button.setText("button");
  }
};

listView.setAdapter(lvAdapter);
```

- 多类型布局

```java
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

lvAdapter2.setAdapter(lvAdapter2);
```
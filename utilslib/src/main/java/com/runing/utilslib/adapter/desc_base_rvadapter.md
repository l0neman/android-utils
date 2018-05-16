# BaseRvAdapter

1. 精简原始 RecyclerView.Adapter 的包含 ViewHolder 在内的重复代码。
2. 加入泛型和对 Item 子 View 的缓存，避免繁琐操作，凸显出核心逻辑。
3. 加入 OnItemClickListener 和 OnItemLongClickListener 的支持。

- 单布局 

```java
BaseRvAdapter rvAdapter = new BaseRvAdapter(data, R.layout.test_item_layout) {

  @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    /* 泛型的加入使代码更优雅，子 View 缓存避免了每次的 findView */
    ImageView imageView = holder.getView(R.id.iv_item);
    TextView textView = holder.getView(R.id.tv_item);
    Button button = holder.getView(R.id.btn_item);

    imageView.setImageResource(R.mipmap.ic_launcher);
    textView.setText("text");
    button.setText("button");
  }
};
```

- 多类型布局

```java
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
```

- ItemClickListener

```java
rvAdapter.setOnItemClickListener(new BaseRvAdapter.OnItemClickListener() {
  @Override public void onItemClick(View view, int position) { /**/ }
});

rvAdapter.setOnItemLongClickListener(new BaseRvAdapter.OnItemLongClickListener() {
  @Override public void onItemLongClick(View view, int position) { /**/ }
});
```
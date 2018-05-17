# HeaderRvAdapter

1. 为 RecyclerView 提供添加 HeaderView 和 FooterView 的功能。
2. 采用包装模式，包装了原始 Adapter。

- 使用

```java
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

/* 使用 HeaderRvAdapter 包装原始 Adapter */
HeaderRvAdapter headerRvAdapter = new HeaderRvAdapter(adapter);
/* 添加 Header 或 Footer */
headerRvAdapter.addHeaderView(new Button(context));
headerRvAdapter.addFooterView(new TextView(context));
headerRvAdapter.addFooterView(new TextView(context));

/* 设置Adapter */
recyclerView.setAdapter(headerRvAdapter);
```


- 也可以直接创建使用（不推荐）

```java
/* 可以直接用 */
HeaderRvAdapter rvAdapter = new HeaderRvAdapter();
rvAdapter.addHeaderView(new Button(context));
rvAdapter.addFooterView(new TextView(context));
rvAdapter.addFooterView(new TextView(context));

recyclerView.setAdapter(rvAdapter);
```

推荐 [BaseRvAdapter - (好用的 RecyclerViewAdapter)](./BaseRvAdapter.java)
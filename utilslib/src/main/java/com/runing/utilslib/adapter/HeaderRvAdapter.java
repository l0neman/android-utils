package com.runing.utilslib.adapter;

import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Adds additional functionality for {@link HeaderRvAdapter#addFooterView(View)}
 * and {@link HeaderRvAdapter#addHeaderView(View)} to recyclerView' adapter.
 * <p>Wrapper in a primitive Adapter</p>
 * Created by runing on 2016/10/26.
 */

public final class HeaderRvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int ITEM_VIEW_TYPE_HEADER = Integer.MAX_VALUE;
  private static final int ITEM_VIEW_TYPE_FOOTER = Integer.MIN_VALUE;

  private SparseArrayCompat<View> mHeaderViews = new SparseArrayCompat<>();
  private SparseArrayCompat<View> mFooterViews = new SparseArrayCompat<>();

  private RecyclerView.Adapter mAdapter;

  public HeaderRvAdapter() { }

  public HeaderRvAdapter(RecyclerView.Adapter<? extends RecyclerView.ViewHolder> adapter) {
    this.mAdapter = adapter;
  }

  public void addHeaderView(View headerView) {
    mHeaderViews.put(ITEM_VIEW_TYPE_HEADER - getHeaderCount(), headerView);
  }

  public void addFooterView(View footerView) {
    mFooterViews.put(ITEM_VIEW_TYPE_FOOTER + getFooterCount(), footerView);
  }

  private int getHeaderCount() {
    return mHeaderViews.size();
  }

  private int getFooterCount() {
    return mFooterViews.size();
  }

  @Override
  @NonNull
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View headerView = mHeaderViews.get(viewType);
    if (headerView != null) {
      return createViewHolder(headerView);
    }
    View footerView = mFooterViews.get(viewType);
    if (footerView != null) {
      return createViewHolder(footerView);
    }
    return mAdapter.onCreateViewHolder(parent, viewType);
  }

  private RecyclerView.ViewHolder createViewHolder(final View view) {
    return new RecyclerView.ViewHolder(view) {};
  }

  @Override
  public long getItemId(int position) {
    int headerCount = getHeaderCount();
    final int adapterPosition = position - headerCount;
    if (mAdapter != null && position >= headerCount) {
      final int adapterCount = mAdapter.getItemCount();
      if (adapterPosition < adapterCount) {
        return mAdapter.getItemId(position);
      }
    }
    return -1;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    int headerCount = getHeaderCount();
    if (position < headerCount) { return; }

    final int adapterPosition = position - headerCount;
    if (mAdapter != null) {
      final int adapterCount = mAdapter.getItemCount();
      if (adapterPosition < adapterCount) {
        mAdapter.onBindViewHolder(holder, adapterPosition);
      }
    }
  }

  @Override
  public int getItemViewType(int position) {
    final int headerCount = getHeaderCount();
    if (position < headerCount) {
      return mHeaderViews.keyAt(position);
    }

    int adapterCount = 0;
    final int adapterPosition = position - headerCount;
    if (mAdapter != null) {
      adapterCount = mAdapter.getItemCount();
      if (adapterPosition < adapterCount) {
        return mAdapter.getItemViewType(adapterPosition);
      }
    }
    return mFooterViews.keyAt(adapterPosition - adapterCount);
  }

  @Override
  public int getItemCount() {
    if (mAdapter != null) {
      return mAdapter.getItemCount() + getHeaderCount() + getFooterCount();
    }
    return getHeaderCount() + getFooterCount();
  }
}

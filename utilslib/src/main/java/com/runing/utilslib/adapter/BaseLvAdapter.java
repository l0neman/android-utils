package com.runing.utilslib.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * {@link android.widget.ListView}'s base adapter (RecyclerView style)
 * Simplifies the creation of {@link BaseAdapter}
 * Support for adding multiple View types. {@link MultiType}
 * Created by runing on 2016/10/20.
 */

public abstract class BaseLvAdapter extends BaseAdapter {

  private final List<?> mData;
  private int mItemId;

  private MultiType mMultiType;

  /**
   * @param data          bean data
   * @param multiType     Multiple item types support
   */
  public BaseLvAdapter(List<?> data, MultiType multiType) {
    this.mData = data;
    this.mMultiType = multiType;
  }

  /**
   * @param mData  bean data
   * @param itemId The layout id of the item
   */
  public BaseLvAdapter(List<?> mData, int itemId) {
    this.mData = mData;
    this.mItemId = itemId;
  }

  /**
   * Multiple item types are supported
   */
  public interface MultiType {
    /**
     * Return the corresponding layout id of based on position.
     * The layout id of item will be viewType
     *
     * @param position position
     * @return The layout id of Item
     */
    int getItemLayoutId(int position);

    /**
     * @return layout type count
     */
    int getItemTypeCount();
  }

  @Override
  public final int getCount() {
    return mData.size();
  }

  @Override
  public final Object getItem(int position) {
    return mData.get(position);
  }

  @Override
  public final long getItemId(int position) {
    return position;
  }

  @Override
  public final View getView(final int position, View convertView, ViewGroup parent) {
    final int itemId;
    if (mMultiType == null) {
      itemId = mItemId;
    } else {
      itemId = mMultiType.getItemLayoutId(position);
    }

    ViewHolder holder = createViewHolder(parent.getContext(), convertView, parent, itemId);
    onBindViewHolder(holder, position);
    return holder.getItemView();
  }

  private ViewHolder createViewHolder(Context context, View convertView,
                                      ViewGroup parent, int itemId) {
    ViewHolder holder;
    if (convertView == null) {
      holder = new ViewHolder(LayoutInflater.from(context).inflate(itemId, parent, false));
      convertView = holder.getItemView();
      convertView.setTag(itemId, holder);
    } else {
      holder = (ViewHolder)convertView.getTag(itemId);
    }

    return holder;
  }

  @Override
  public int getViewTypeCount() {
    if (mMultiType == null) {
      return super.getViewTypeCount();
    }

    return mMultiType.getItemTypeCount();
  }

  @Override
  public int getItemViewType(int position) {
    if (mMultiType != null) {
      return mMultiType.getItemLayoutId(position);
    }

    return super.getItemViewType(position);
  }

  /**
   * Bind the data
   *
   * @param holder   view cache
   * @param position position
   */
  public abstract void onBindViewHolder(ViewHolder holder, int position);

  /**
   * Base viewHolder
   */
  public static class ViewHolder {
    private final View itemView;
    private SparseArrayCompat<View> views = new SparseArrayCompat<>();

    public View getItemView() {
      return itemView;
    }

    public ViewHolder(@NonNull View itemView) {
      this.itemView = itemView;
    }

    public <T extends View> T getView(int id) {
      View view = views.get(id);
      if (view == null) {
        view = itemView.findViewById(id);
        views.put(id, view);
      }
      @SuppressWarnings("unchecked")
      T t = (T)view;
      return t;
    }
  }
}

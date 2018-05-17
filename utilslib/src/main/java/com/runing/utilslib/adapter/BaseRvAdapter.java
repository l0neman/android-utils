package com.runing.utilslib.adapter;

import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Simplify {@link RecyclerView} Adapter
 * Simplifies the creation of {@link RecyclerView.Adapter}
 * <pre>
 * Contains the following features:
 * 1. Set the click event for item.
 *  {@link BaseRvAdapter#setOnItemClickListener(OnItemClickListener)}
 *  {@link BaseRvAdapter#setOnItemLongClickListener(OnItemLongClickListener)}
 * 2. Support for adding multiple View types. {@link MultiType}
 * </pre>
 * Created by runing on 2016/10/21.
 */

public abstract class BaseRvAdapter extends RecyclerView.Adapter<BaseRvAdapter.ViewHolder> {

  private final List<?> mData;

  private OnItemClickListener mOnItemClickListener;
  private OnItemLongClickListener mOnItemLongClickListener;

  private int mItemId;
  private MultiType mMultiType;

  /**
   * Multiple item types are supported
   */
  public interface MultiType {
    /**
     * Returns the corresponding layout id based on position.
     * The layout id of the item will be viewType
     *
     * @param position position
     * @return The layout id of item
     */
    int getItemLayoutId(int position);
  }

  /**
   * @param mData     bean data
   * @param multiType MultiType callback
   */
  protected BaseRvAdapter(List<?> mData, @NonNull MultiType multiType) {
    this.mMultiType = multiType;
    this.mData = mData;
  }

  /**
   * @param mData   bean data
   * @param mItemId The layout id of the item
   */
  protected BaseRvAdapter(List<?> mData, int mItemId) {
    this.mItemId = mItemId;
    this.mData = mData;
  }

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    this.mOnItemClickListener = onItemClickListener;
  }

  public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
    this.mOnItemLongClickListener = onItemLongClickListener;
  }

  @Override
  public int getItemViewType(int position) {
    if (mMultiType != null) {
      return mMultiType.getItemLayoutId(position);
    }
    return super.getItemViewType(position);
  }

  @Override
  public final int getItemCount() {
    return mData.size();
  }

  protected Object getItem(int position) {
    return mData.get(position);
  }

  @Override
  public final long getItemId(int position) {
    return position;
  }

  @Override
  @NonNull
  public final ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    final int layoutId;
    if (mMultiType == null) {
      layoutId = mItemId;
    } else {
      layoutId = viewType;
    }
    ViewHolder holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
        layoutId, parent, false));
    setClickListener(holder);
    return holder;
  }

  private void setClickListener(final ViewHolder holder) {
    if (mOnItemClickListener != null) {
      holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          mOnItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition());
        }
      });
    }
    if (mOnItemLongClickListener != null) {
      holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
          mOnItemLongClickListener.onItemLongClick(holder.itemView,
              holder.getAdapterPosition());
          return true;
        }
      });
    }
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
    if (payloads.isEmpty()) {
      onBindViewHolder(holder, position);
    }
  }

  public interface OnItemClickListener {
    void onItemClick(View view, int position);
  }

  public interface OnItemLongClickListener {
    void onItemLongClick(View view, int position);
  }

  protected static final class ViewHolder extends RecyclerView.ViewHolder {

    private SparseArrayCompat<View> views = new SparseArrayCompat<>();

    private ViewHolder(View itemView) {
      super(itemView);
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

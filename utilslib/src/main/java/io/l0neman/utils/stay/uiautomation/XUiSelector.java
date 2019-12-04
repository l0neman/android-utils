package io.l0neman.utils.stay.uiautomation;

import android.util.SparseArray;

import androidx.annotation.NonNull;

/**
 * Created by l0neman on 2019/11/23.
 */
public class XUiSelector {

  static final int SELECTOR_ENABLED = 0;
  static final int SELECTOR_CHECKED = 1;
  static final int SELECTOR_FOCUSED = 2;
  static final int SELECTOR_FOCUSABLE = 3;
  static final int SELECTOR_SCROLLABLE = 4;
  static final int SELECTOR_SELECTED = 5;
  static final int SELECTOR_CLICKABLE = 6;
  static final int SELECTOR_CHECKABLE = 7;
  static final int SELECTOR_LONG_CLICKABLE = 8;
  static final int SELECTOR_TYPE_STR = 9;
  static final int SELECTOR_TYPE_CLS = 10;
  static final int SELECTOR_RES_ID = 11;
  static final int SELECTOR_TEXT = 12;
  static final int SELECTOR_TEXT_MATCHES = 13;
  static final int SELECTOR_TEXT_STARTS_WITH = 14;
  static final int SELECTOR_TEXT_CONTAINS = 15;
  static final int SELECTOR_TEXT_ENDS_WITH = 16;
  static final int SELECTOR_TEXT_HINT = 17;
  static final int SELECTOR_CONTENT_DESC = 18;
  static final int SELECTOR_CONTENT_DESC_MATCHES = 19;
  static final int SELECTOR_CONTENT_DESC_CONTAINS = 20;
  static final int SELECTOR_CONTENT_DESC_STARTS_WITH = 21;
  static final int SELECTOR_CONTENT_DESC_ENDS_WITH = 22;

  private SparseArray<Object> mSelectorAttributes = new SparseArray<>();

  public synchronized XUiSelector enabled(boolean enabled) {
    mSelectorAttributes.put(SELECTOR_ENABLED, enabled);
    return this;
  }

  public synchronized XUiSelector checked(boolean checked) {
    mSelectorAttributes.put(SELECTOR_CHECKED, checked);
    return this;
  }

  public synchronized XUiSelector focused(boolean focused) {
    mSelectorAttributes.put(SELECTOR_FOCUSED, focused);
    return this;
  }

  public synchronized XUiSelector focusable(boolean focusable) {
    mSelectorAttributes.put(SELECTOR_FOCUSABLE, focusable);
    return this;
  }

  public synchronized XUiSelector scrollable(boolean scrollable) {
    mSelectorAttributes.put(SELECTOR_SCROLLABLE, scrollable);
    return this;
  }

  public synchronized XUiSelector selected(boolean selected) {
    mSelectorAttributes.put(SELECTOR_SELECTED, selected);
    return this;
  }

  public synchronized XUiSelector clickable(boolean clickable) {
    mSelectorAttributes.put(SELECTOR_CLICKABLE, clickable);
    return this;
  }

  public synchronized XUiSelector checkable(boolean checkable) {
    mSelectorAttributes.put(SELECTOR_CHECKABLE, checkable);
    return this;
  }

  public synchronized XUiSelector longClickable(boolean longClickable) {
    mSelectorAttributes.put(SELECTOR_LONG_CLICKABLE, longClickable);
    return this;
  }

  public synchronized XUiSelector type(Class<?> type) {
    mSelectorAttributes.put(SELECTOR_TYPE_CLS, type);
    return this;
  }

  public synchronized XUiSelector type(String type) {
    mSelectorAttributes.put(SELECTOR_TYPE_STR, type);
    return this;
  }

  public synchronized XUiSelector resId(String resId) {
    mSelectorAttributes.put(SELECTOR_RES_ID, resId);
    return this;
  }

  public synchronized XUiSelector text(String text) {
    mSelectorAttributes.put(SELECTOR_TEXT, text);
    return this;
  }

  public synchronized XUiSelector textMatches(String regex) {
    mSelectorAttributes.put(SELECTOR_TEXT_MATCHES, regex);
    return this;
  }

  public synchronized XUiSelector textStartsWith(String startsWith) {
    mSelectorAttributes.put(SELECTOR_TEXT_STARTS_WITH, startsWith);
    return this;
  }

  public synchronized XUiSelector textEndsWith(String endsWith) {
    mSelectorAttributes.put(SELECTOR_TEXT_ENDS_WITH, endsWith);
    return this;
  }

  public synchronized XUiSelector textContains(String textContains) {
    mSelectorAttributes.put(SELECTOR_TEXT_CONTAINS, textContains);
    return this;
  }

  public synchronized XUiSelector textHint(String textHint) {
    mSelectorAttributes.put(SELECTOR_TEXT_HINT, textHint);
    return this;
  }

  public synchronized XUiSelector desc(String contentDesc) {
    mSelectorAttributes.put(SELECTOR_CONTENT_DESC, contentDesc);
    return this;
  }

  public synchronized XUiSelector descMatches(String matches) {
    mSelectorAttributes.put(SELECTOR_CONTENT_DESC_MATCHES, matches);
    return this;
  }

  public synchronized XUiSelector descContains(String contains) {
    mSelectorAttributes.put(SELECTOR_CONTENT_DESC_CONTAINS, contains);
    return this;
  }

  public synchronized XUiSelector descStartsWith(String startsWith) {
    mSelectorAttributes.put(SELECTOR_CONTENT_DESC_STARTS_WITH, startsWith);
    return this;
  }

  public synchronized XUiSelector descEndsWith(String endsWith) {
    mSelectorAttributes.put(SELECTOR_CONTENT_DESC_ENDS_WITH, endsWith);
    return this;
  }

  public synchronized <T> T getSelectorAttribute(int selector) {
    //noinspection unchecked
    return (T) mSelectorAttributes.get(selector);
  }

  // future support:
  // depth(int depth);
  // depth(int min, int max);
  // hasChild(UiSelector selector);
  // scrollable(boolean isScrollable);

  @NonNull @Override public String toString() {
    return dumpToString();
  }

  private String dumpToString() {
    StringBuilder builder = new StringBuilder();
    builder.append(XUiSelector.class.getSimpleName()).append("[");
    final int criterionCount = mSelectorAttributes.size();
    for (int i = 0; i < criterionCount; i++) {
      if (i > 0) {
        builder.append(", ");
      }

      final int criterion = mSelectorAttributes.keyAt(i);
      switch (criterion) {
      case SELECTOR_ENABLED:
        builder.append("SELECTOR_ENABLED=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_CHECKED:
        builder.append("SELECTOR_CHECKED=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_FOCUSED:
        builder.append("SELECTOR_FOCUSED=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_FOCUSABLE:
        builder.append("SELECTOR_FOCUSABLE=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_SCROLLABLE:
        builder.append("SELECTOR_SCROLLABLE=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_SELECTED:
        builder.append("SELECTOR_SELECTED=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_CLICKABLE:
        builder.append("SELECTOR_CLICKABLE=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_CHECKABLE:
        builder.append("SELECTOR_CHECKABLE=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_LONG_CLICKABLE:
        builder.append("SELECTOR_LONG_CLICKABLE=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_TYPE_STR:
        builder.append("SELECTOR_TYPE_STR=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_TYPE_CLS:
        builder.append("SELECTOR_TYPE_CLS=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_RES_ID:
        builder.append("SELECTOR_RES_ID=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_TEXT:
        builder.append("SELECTOR_TEXT=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_TEXT_MATCHES:
        builder.append("SELECTOR_TEXT_MATCHES=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_TEXT_STARTS_WITH:
        builder.append("SELECTOR_TEXT_STARTS_WITH=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_TEXT_ENDS_WITH:
        builder.append("SELECTOR_TEXT_ENDS_WITH=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_TEXT_CONTAINS:
        builder.append("SELECTOR_TEXT_CONTAINS=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_TEXT_HINT:
        builder.append("SELECTOR_TEXT_HINT=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_CONTENT_DESC:
        builder.append("SELECTOR_CONTENT_DESC=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_CONTENT_DESC_MATCHES:
        builder.append("SELECTOR_CONTENT_DESC_MATCHES=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_CONTENT_DESC_CONTAINS:
        builder.append("SELECTOR_CONTENT_DESC_CONTAINS=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_CONTENT_DESC_STARTS_WITH:
        builder.append("SELECTOR_CONTENT_DESC_STARTS_WITH=").append(mSelectorAttributes.valueAt(i));
        break;
      case SELECTOR_CONTENT_DESC_ENDS_WITH:
        builder.append("SELECTOR_CONTENT_DESC_ENDS_WITH=").append(mSelectorAttributes.valueAt(i));
        break;
      default:
        builder.append("UNDEFINED=").append(criterion).append(" ").append(mSelectorAttributes.valueAt(i));
      }
    }

    builder.append("]");
    return builder.toString();
  }
}

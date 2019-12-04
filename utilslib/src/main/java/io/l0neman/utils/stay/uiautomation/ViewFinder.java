package io.l0neman.utils.stay.uiautomation;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import io.l0neman.utils.stay.log.XLogger;

/**
 * Created by l0neman on 2019/11/25.
 */
final class ViewFinder {

  private static final String TAG = ViewFinder.class.getSimpleName();

  private static final boolean DEBUG = false;

  private static ExecutorService mLogExecutor = Executors.newCachedThreadPool();

  static boolean findWithView(XUiObject with, List<XUiObject> results, XUiSelector selector) {
    // if results is null (means check has object), so return has object status fast.
    if (findWithViewInternal(with, results, selector) && results == null) {
      return true;
    }

    if (with.getView() instanceof ViewGroup) {
      final ViewGroup group = (ViewGroup) with.getView();
      final int childCount = group.getChildCount();
      for (int i = 0; i < childCount; i++) {
        if (findWithView(new XUiObject(with.getDevice(), group.getChildAt(i)), results, selector) &&
            results == null
        ) {
          return true;
        }
      }
    }

    return false;
  }

  private static boolean findWithViewInternal(final XUiObject with, List<XUiObject> results, XUiSelector selector) {
    if (DEBUG) {
      mLogExecutor.submit(new Runnable() {
        @Override public void run() {
          XLogger.d(TAG, "find view: %s", ViewPrinter.dumpSingleView(with.getView()));
        }
      });
    }

    final Boolean enabled = selector.getSelectorAttribute(XUiSelector.SELECTOR_ENABLED);

    if (enabled != null && !enabled.equals(with.isEnabled())) { return false; }

    final Boolean checkable = selector.getSelectorAttribute(XUiSelector.SELECTOR_CHECKABLE);

    if (checkable != null && !with.isCheckable()) { return false; }

    final Boolean checked = selector.getSelectorAttribute(XUiSelector.SELECTOR_CHECKED);
    if (checked != null && !with.isChecked()) { return false; }

    final Boolean focusable = selector.getSelectorAttribute(XUiSelector.SELECTOR_FOCUSABLE);
    if (focusable != null && !with.isFocusable()) { return false; }

    final Boolean focused = selector.getSelectorAttribute(XUiSelector.SELECTOR_FOCUSED);
    if (focused != null && !with.isFocused()) { return false; }

    final Boolean selected = selector.getSelectorAttribute(XUiSelector.SELECTOR_SELECTED);
    if (selected != null && !with.isSelected()) { return false; }

    final Boolean clickable = selector.getSelectorAttribute(XUiSelector.SELECTOR_CLICKABLE);
    if (clickable != null && !with.isClickable()) { return false; }

    final Boolean longClickable = selector.getSelectorAttribute(XUiSelector.SELECTOR_LONG_CLICKABLE);
    if (longClickable != null && !with.isLongClickable()) { return false; }

    final Class<?> tClass = selector.getSelectorAttribute(XUiSelector.SELECTOR_TYPE_CLS);
    final String tStr = selector.getSelectorAttribute(XUiSelector.SELECTOR_TYPE_STR);
    String clazz = tClass != null ? tClass.getName() : tStr;

    if (clazz != null && !clazz.equals(with.getType().getName())) { return false; }

    final String resId = selector.getSelectorAttribute(XUiSelector.SELECTOR_RES_ID);

    if (resId != null && !resId.equals(with.getResourceId())) { return false; }

    final String text = selector.getSelectorAttribute(XUiSelector.SELECTOR_TEXT);

    final String textMatches = selector.getSelectorAttribute(XUiSelector.SELECTOR_TEXT_MATCHES);

    final String textContains = selector.getSelectorAttribute(XUiSelector.SELECTOR_TEXT_CONTAINS);

    final String textStartsWith = selector.getSelectorAttribute(XUiSelector.SELECTOR_TEXT_STARTS_WITH);

    final String textEndsWith = selector.getSelectorAttribute(XUiSelector.SELECTOR_TEXT_ENDS_WITH);

    if (text != null || textMatches != null || textContains != null || textStartsWith != null ||
        textEndsWith != null) {

      if (!(with.getView() instanceof TextView)) { return false; }

      final String viewText = String.valueOf(with.getText());

      if (text != null && !text.equals(viewText)) { return false; }

      if (textMatches != null && !Pattern.matches(textMatches, viewText)) { return false; }

      if (textContains != null && !viewText.contains(textContains)) { return false; }

      if (textStartsWith != null && !viewText.startsWith(textStartsWith)) { return false; }

      if (textEndsWith != null && !viewText.startsWith(textEndsWith)) { return false; }
    }

    final String textHint = selector.getSelectorAttribute(XUiSelector.SELECTOR_TEXT_HINT);
    if (textHint != null && !textHint.equals(String.valueOf(with.getHint()))) { return false; }

    final String desc = selector.getSelectorAttribute(XUiSelector.SELECTOR_CONTENT_DESC);
    final String descMatches = selector.getSelectorAttribute(XUiSelector.SELECTOR_CONTENT_DESC_MATCHES);
    final String descContains = selector.getSelectorAttribute(XUiSelector.SELECTOR_CONTENT_DESC_CONTAINS);
    final String descStartsWith = selector.getSelectorAttribute(XUiSelector.SELECTOR_CONTENT_DESC_STARTS_WITH);
    final String descEndsWith = selector.getSelectorAttribute(XUiSelector.SELECTOR_CONTENT_DESC_ENDS_WITH);

    if (desc != null || descMatches != null || descContains != null || descStartsWith != null ||
        descEndsWith != null) {

      if (!(with.getView() instanceof TextView)) { return false; }

      final String viewDesc = String.valueOf(with.getContentDesc());

      if (desc != null && !desc.equals(viewDesc)) { return false; }

      if (descMatches != null && !Pattern.matches(descMatches, viewDesc)) { return false; }

      if (descContains != null && !viewDesc.contains(descContains)) { return false; }

      if (descStartsWith != null && !viewDesc.startsWith(descStartsWith)) { return false; }

      if (descEndsWith != null && !viewDesc.startsWith(descEndsWith)) { return false; }
    }

    if (results != null) { results.add(with); }

    return true;
  }

  /*
   * @return [package name]/id:[id name] or no-id.
   * Example: xxx:xx:xx/id:btn0
   */
  static String getId(View view) {
    if (view.getId() == View.NO_ID) return "no-id";
    else return view.getResources().getResourceName(view.getId());
  }
}

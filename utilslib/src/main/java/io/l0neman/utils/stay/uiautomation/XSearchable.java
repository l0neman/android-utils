package io.l0neman.utils.stay.uiautomation;

import java.util.List;

/**
 * Created by l0neman on 2019/11/28.
 */
public interface XSearchable {
  boolean hasObject(XUiSelector selector);

  XUiObject findObject(XUiSelector selector);

  XUiObject findIndexObject(XUiSelector selector, int index);

  XUiObject findLastObject(XUiSelector selector);

  List<XUiObject> findObjects(XUiSelector selector);
}

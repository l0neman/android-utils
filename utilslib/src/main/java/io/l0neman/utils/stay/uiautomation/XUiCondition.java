package io.l0neman.utils.stay.uiautomation;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by l0neman on 2019/11/28.
 */
public abstract class XUiCondition extends XCondition<XUiCondition, Boolean> {

  public static XSearchCondition<Boolean> gone(final XUiSelector selector) {
    return new XSearchCondition<Boolean>() {
      @Override public Boolean apply(XSearchable arg) {
        return !arg.hasObject(selector);
      }
    };
  }

  public static XSearchCondition<Boolean> hashObject(final XUiSelector selector) {
    return new XSearchCondition<Boolean>() {
      @Override public Boolean apply(XSearchable arg) {
        return arg.hasObject(selector);
      }
    };
  }

  public static XSearchCondition<XUiObject> findObject(final XUiSelector selector) {
    return new XSearchCondition<XUiObject>() {
      @Override public XUiObject apply(XSearchable arg) {
        return arg.findObject(selector);
      }
    };
  }

  public static XSearchCondition<List<XUiObject>> findObjects(final XUiSelector selector) {
    return new XSearchCondition<List<XUiObject>>() {
      @Override public List<XUiObject> apply(XSearchable arg) {
        final List<XUiObject> ret = arg.findObjects(selector);
        return ret.isEmpty() ? null : ret;
      }
    };
  }

  public static XUiObjectCondition<Boolean> checkable(final boolean isCheckable) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return arg.isCheckable() == isCheckable;
      }
    };
  }

  public static XUiObjectCondition<Boolean> checked(final boolean isChecked) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return arg.isChecked() == isChecked;
      }
    };
  }

  public static XUiObjectCondition<Boolean> clickable(final boolean isClickable) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return arg.isClickable() == isClickable;
      }
    };
  }

  public static XUiObjectCondition<Boolean> enabled(final boolean isEnabled) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return arg.isEnabled() == isEnabled;
      }
    };
  }

  public static XUiObjectCondition<Boolean> focusable(final boolean isFocusable) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return arg.isFocusable() == isFocusable;
      }
    };
  }

  public static XUiObjectCondition<Boolean> focused(final boolean isFocused) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return arg.isFocused() == isFocused;
      }
    };
  }

  public static XUiObjectCondition<Boolean> longClickable(final boolean isLongClickable) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return arg.isLongClickable() == isLongClickable;
      }
    };
  }

  public static XUiObjectCondition<Boolean> selected(final boolean isSelected) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return arg.isSelected() == isSelected;
      }
    };
  }

  public static XUiObjectCondition<Boolean> descMatches(final String regex) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return Pattern.matches(regex, arg.getText());
      }
    };
  }

  public static XUiObjectCondition<Boolean> descEquals(final String desc) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return arg.getContentDesc().toString().equals(desc);
      }
    };
  }

  public static XUiObjectCondition<Boolean> descContains(final String substring) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return arg.getContentDesc().toString().contains(substring);
      }
    };
  }

  public static XUiObjectCondition<Boolean> descStartsWith(final String substring) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return arg.getContentDesc().toString().startsWith(substring);
      }
    };
  }

  public static XUiObjectCondition<Boolean> descEndsWith(final String substring) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return arg.getContentDesc().toString().endsWith(substring);
      }
    };
  }

  public static XUiObjectCondition<Boolean> textMatches(final String regex) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return Pattern.matches(regex, arg.getText());
      }
    };
  }

  public static XUiObjectCondition<Boolean> textNotEquals(final String text) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return !arg.getText().toString().equals(text);
      }
    };
  }

  public static XUiObjectCondition<Boolean> textEquals(final String text) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return arg.getText().toString().equals(text);
      }
    };
  }

  public static XUiObjectCondition<Boolean> textContains(final String substring) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return arg.getText().toString().contains(substring);
      }
    };
  }

  public static XUiObjectCondition<Boolean> textStartsWith(final String substring) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return arg.getText().toString().startsWith(substring);
      }
    };
  }

  public static XUiObjectCondition<Boolean> textEndsWith(final String substring) {
    return new XUiObjectCondition<Boolean>() {
      @Override public Boolean apply(XUiObject arg) {
        return arg.getText().toString().endsWith(substring);
      }
    };
  }
}

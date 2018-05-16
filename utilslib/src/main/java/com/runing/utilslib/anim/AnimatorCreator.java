package com.runing.utilslib.anim;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static android.animation.ValueAnimator.RESTART;
import static android.animation.ValueAnimator.REVERSE;

/**
 * <p> 快速动画创建器，链式调用使得创建动画的过程更加流畅，直观。</p>
 * <p>
 * Created by DSI on 2017/9/2.
 */

public final class AnimatorCreator {

  private static final int ANIM_TYPE_OBJECT = 0;
  private static final int ANIM_TYPE_VALUE = 1;

  private List<ValueAnimator.AnimatorUpdateListener> updateListeners = new LinkedList<>();
  private List<Animator.AnimatorListener> listeners = new LinkedList<>();
  private ArrayList<PropertyValuesHolder> propertyValues;
  private TimeInterpolator interpolator;
  private View target;
  private String propertyType;
  private long duration;
  private int repeatMode;
  private int repeatCount;
  private long startDelay;
  private int animType;

  public enum Type {
    Alpha("alpha"),
    ScaleX("scaleX"),
    ScaleY("scaleY"),
    Rotation("rotation"),
    RotationX("rotationY"),
    RotationY("rotationX"),
    TranslationX("translationX"),
    TranslationY("translationY");

    private String animType;

    Type(String animType) { this.animType = animType; }
  }

  @IntDef({RESTART, REVERSE})
  @Retention(RetentionPolicy.SOURCE)
  public @interface RepeatMode {}

  public interface FractionCallback {
    void onFraction(float fraction);
  }

  public interface IntValueCallback {
    void onValue(int value);
  }

  public interface FloatValueCallback {
    void onValue(float value);
  }

  public interface ValueCallback<T> {
    void onValue(T value);
  }

  public static AnimatorCreator objAnim() {
    return new AnimatorCreator(ANIM_TYPE_OBJECT);
  }

  public static AnimatorCreator valAnim() {
    return new AnimatorCreator(ANIM_TYPE_VALUE);
  }

  private AnimatorCreator(int animType) {
    this.animType = animType;
    propertyValues = new ArrayList<>();
  }

  public AnimatorCreator propType(Type type) {
    this.propertyType = type.animType;
    return this;
  }

  public AnimatorCreator propType(String animType) {
    this.propertyType = animType;
    return this;
  }

  public AnimatorCreator target(@NonNull View target) {
    this.target = target;
    return this;
  }

  public AnimatorCreator duration(long duration) {
    this.duration = duration;
    return this;
  }

  public AnimatorCreator startDelay(long startDelay) {
    this.startDelay = startDelay;
    return this;
  }

  public AnimatorCreator interpolator(TimeInterpolator interpolator) {
    this.interpolator = interpolator;
    return this;
  }

  public AnimatorCreator listener(Animator.AnimatorListener listener) {
    this.listeners.add(listener);
    return this;
  }

  public AnimatorCreator updateListener(ValueAnimator.AnimatorUpdateListener updateListener) {
    this.updateListeners.add(updateListener);
    return this;
  }

  public AnimatorCreator fractionListener(final FloatValueCallback callback) {
    this.updateListeners.add(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        callback.onValue(animation.getAnimatedFraction());
      }
    });
    return this;
  }

  public AnimatorCreator valueListener(final IntValueCallback callback) {
    this.updateListeners.add(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        callback.onValue((int)animation.getAnimatedValue());
      }
    });
    return this;
  }

  public AnimatorCreator valueListener(final FloatValueCallback callback) {
    this.updateListeners.add(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        callback.onValue((float)animation.getAnimatedValue());
      }
    });
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> AnimatorCreator valueListener(final ValueCallback<T> callback) {
    this.updateListeners.add(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        callback.onValue((T)animation.getAnimatedValue());
      }
    });
    return this;
  }

  public AnimatorCreator infinite() {
    this.repeatCount = -1;
    this.repeatMode = RESTART;
    return this;
  }

  public AnimatorCreator repeatMode(@RepeatMode int repeatMode) {
    this.repeatMode = repeatMode;
    return this;
  }

  public AnimatorCreator repeat(int repeatCount) {
    if (repeatMode == 0) {
      this.repeatMode = RESTART;
    }
    this.repeatCount = repeatCount;
    return this;
  }

  public AnimatorCreator addFloat(Type type, float... value) {
    propertyValues.add(PropertyValuesHolder.ofFloat(type.animType, value));
    return this;
  }

  public AnimatorCreator addInt(Type type, int... value) {
    propertyValues.add(PropertyValuesHolder.ofInt(type.animType, value));
    return this;
  }

  public AnimatorCreator addFloat(String type, float... value) {
    propertyValues.add(PropertyValuesHolder.ofFloat(type, value));
    return this;
  }

  public AnimatorCreator addInt(String type, int... value) {
    propertyValues.add(PropertyValuesHolder.ofInt(type, value));
    return this;
  }

  public AnimatorCreator addObject(String type, TypeEvaluator evaluator, Object... values) {
    propertyValues.add(PropertyValuesHolder.ofObject(type, evaluator, values));
    return this;
  }

  public Animator ofFloat(float... values) {
    final Animator animator = animType == ANIM_TYPE_OBJECT ?
        ObjectAnimator.ofFloat(target, propertyType, values) :
        ValueAnimator.ofFloat(values);
    if (animType == ANIM_TYPE_OBJECT) {
      addConfig((ObjectAnimator)animator);
    } else {
      addConfig((ValueAnimator)animator);
    }
    return animator;
  }

  public Animator ofInt(int... values) {
    final Animator animator = animType == ANIM_TYPE_OBJECT ?
        ObjectAnimator.ofInt(target, propertyType, values) :
        ValueAnimator.ofInt(values);
    if (animType == ANIM_TYPE_OBJECT) {
      addConfig((ObjectAnimator)animator);
    } else {
      addConfig((ValueAnimator)animator);
    }
    return animator;
  }

  public Animator ofTypeEvaluator(TypeEvaluator<?> evaluator, Object... values) {
    final Animator animator = animType == ANIM_TYPE_OBJECT ?
        ObjectAnimator.ofObject(evaluator, values) :
        ValueAnimator.ofObject(evaluator, values);
    if (animType == ANIM_TYPE_OBJECT) {
      addConfig((ObjectAnimator)animator);
    } else {
      addConfig((ValueAnimator)animator);
    }
    return animator;
  }

  @SuppressWarnings("unchecked")
  public Animator create() {
    if (animType == ANIM_TYPE_VALUE || propertyValues.isEmpty()) {
      return ValueAnimator.ofInt(0);
    }
    PropertyValuesHolder[] propertyValueArray = new PropertyValuesHolder[propertyValues.size()];
    propertyValues.toArray(propertyValueArray);
    final ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(target, propertyValueArray);
    addConfig(animator);
    return animator;
  }

  private void addConfig(ValueAnimator animator) {
    if (interpolator != null) {
      animator.setInterpolator(interpolator);
    }
    if (repeatMode != 0L) {
      animator.setRepeatMode(repeatMode);
    }
    if (repeatCount != 0L) {
      animator.setRepeatCount(repeatCount);
    }
    if (duration != 0L) {
      animator.setDuration(duration);
    }
    if (startDelay != 0L) {
      animator.setStartDelay(startDelay);
    }
    if (!listeners.isEmpty()) {
      for (Animator.AnimatorListener listener : listeners) {
        animator.addListener(listener);
      }
    }
    if (!updateListeners.isEmpty()) {
      for (ValueAnimator.AnimatorUpdateListener updateListener : updateListeners) {
        animator.addUpdateListener(updateListener);
      }
    }
  }

  private void addConfig(ObjectAnimator animator) {
    if (interpolator != null) {
      animator.setInterpolator(interpolator);
    }
    if (repeatMode != 0L) {
      animator.setRepeatMode(repeatMode);
    }
    if (repeatCount != 0L) {
      animator.setRepeatCount(repeatCount);
    }
    if (duration != 0L) {
      animator.setDuration(duration);
    }
    if (startDelay != 0L) {
      animator.setStartDelay(startDelay);
    }
    if (!listeners.isEmpty()) {
      for (Animator.AnimatorListener listener : listeners) {
        animator.addListener(listener);
      }
    }
    if (!updateListeners.isEmpty()) {
      for (ValueAnimator.AnimatorUpdateListener updateListener : updateListeners) {
        animator.addUpdateListener(updateListener);
      }
    }
  }
}

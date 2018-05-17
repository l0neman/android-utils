# AnimatorCreator

快速动画创建器，链式调用使得创建动画的过程更加流畅，直观。

使用示例：

- 创建 ValueAnimator 实例

```java
final Animator valueAnimator = AnimatorCreator.valAnim()
    .duration(1000L)
    .valueListener(new AnimatorCreator.FloatValueCallback() {
        @Override public void onValue(float value) {
            // animator value
        }
    })
    .ofFloat(0F, 1F);
```

- 创建 ObjectAnimator 动画有两种情况，单属性和多属性。

```java
/* 从 float 值创建单个属性的 ObjectAnimator */
final Animator objAnimator = AnimatorCreator.objAnim()
    .target(targetView)
    .duration(1000)
    .propType(AnimatorCreator.Type.TranslationX)
    .ofFloat(0F, 1F);

/* 从 TypeEvaluator 创建单个属性的 ObjectAnimator */
final Animator objAnimator2 = AnimatorCreator.objAnim()
    .target(targetView)
    .duration(1000)
    .propType(AnimatorCreator.Type.TranslationX)
    .ofTypeEvaluator(new ArgbEvaluator(), 0xff00, 0xff11);
```

- 下面是完整方法，这里只是全部列出，有些方法不能共用，以下和Animator的方法含义相同。

```java
AnimatorCreator.objAnim()
    /* 指定动画作用对象 */
    .target(targetView)
    /* 指定时间 */
    .duration(1000)
    /* 指定动画属性 */
    .propType(AnimatorCreator.Type.Alpha)
    .propType("myProp")
    /* 开启无限执行 */
    .infinite()
    /* 重复次数 */
    .repeat(2)
    /* 重复模式 */
    .repeatMode(ValueAnimator.RESTART)
    /* 添加多属性 */
    .addFloat(AnimatorCreator.Type.TranslationX, 0F, 1F)
    .addInt("intProp", 1, 2)
    .addObject("type", new ArgbEvaluator(), 0xff00, 0xff11)
    /* 动画过程监听 */
    .updateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
      }
    })
    /* 动画生命周期监听 */
    .listener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) { }
    })
    /* 动画过程的数值监听（updateListener 的包装） */
    .valueListener(new AnimatorCreator.ValueCallback<Integer>() {
      @Override public void onValue(Integer value) {
        // animator value
      }
    })
    /* 动画执行百分数的监听（updateListener 的包装） */
    .fractionListener(new AnimatorCreator.FloatValueCallback() {
      @Override public void onValue(float value) {
        // animator fraction
      }
    })
    .interpolator(new LinearInterpolator())
    .startDelay(1000)
    .ofFloat(0F, 1F);
```

- 不断改善 . . .
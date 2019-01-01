# HttpUtils

[源码 - HttpUtils.java](./HttpUtils.java) => 接口类
[源码 - OkHttpUtils.java](./OkHttpUtils.java) => OkHttp 实现类

简单封装的 HTTP 请求工具，根据需求进一步扩展功能。

## 构建 HttpUtils 对象。

```java
HttpUtils httpUtils = new OkHttpUtils.Builder()
     // OkHttp 支持的 cookie 方法。
    .cookieJar(new CookieJar() {
      @Override public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {}

      @Override public List<Cookie> loadForRequest(HttpUrl url) { return null; }
    })
    // 设置连接超时。
    .connTimeOut(10, TimeUnit.SECONDS)
    // 设置读取超时。
    .readTimeOut(10, TimeUnit.SECONDS)
    // 设置缓存文件对象。
    .cacheDir(new File("cache"))
    // 设置缓存大小。
    .cacheSize(50 * 1024)
    .build();
```

## Task 对象

请求发出后，会产生 Task 对象，它包含取消操作，同时也包含包含请求完毕后的结果。

```java
// 请求产生 Task 对象。
HttpUtils.Task task = httpUtils.download(...);
//     = httpUtils.get();
//     = httpUtils.post();

/* 以下操作都是线程安全的操作 */
// 获取网络结果码。
final int code = task.code();
// 下载操作完成产生的文件。
final File file = task.file();
// 下载实时进度。
final int progress = task.progress();
// 请求结果字符串。
final String result = task.result();
// 使用转换器转换结果字符串为对象（其实没用，只是为了规范调用）。
try {
  task.result(new HttpUtils.Converter<Integer>() {
    @Override public Integer convert(String result) throws Exception {
      try {
        return Integer.valueOf(result);
      }
      catch (Exception e) {
        throw new Exception(e);
      }
    }
  });
}
catch (Exception ignore) {}
```

## Download

```java
// 同步方法。
try {
  final HttpUtils.Task task = httpUtils.url("http://www.example.com/download")
      .download(new File("target"));
  // 下载成功。
  final File file = task.file();
}
catch (HttpUtils.HttpException e) {
  // 下载出错。
}

// 异步方法。
httpUtils.url("http://www.example.com/download")
    .download(new File("target"), new HttpUtils.DownloadListener() {
      @Override public void onProgress(int progress) {
        // 下载进度。
      }

      @Override public void onSucceed(String result) {
        // 下载成功，返回 file.getPath().
      }

      @Override public void onFailed(HttpUtils.HttpException e) {
        // 下载出错。
      }
    });
```

## Get

```java
// 同步方法
try {
  final HttpUtils.Task task = httpUtils.url("http://www.example.com/get")
      .get();
  // 获取结果。
  final String result = task.result();
}
catch (HttpUtils.HttpException e) {
  //  请求出错。
}

// 同步方法，指定 url 参数。("http://www.example.com/get?name=test&gender=boy")
httpUtils.url("http://www.example.com/get", new String[]{
    "name", "test",
    "gender", "boy"
});

// 异步方法。
HttpUtils.Task task = httpUtils.url("http://www.example.com/get")
    .get(new HttpUtils.Listener() {
      @Override public void onSucceed(String result) {
        // 返回请求结果。
      }

      @Override public void onFailed(HttpUtils.HttpException e) {
        // 请求出错。
      }
    });
```

## Post

post 方法也都包含异步方法，和 get 形式相同，以下示例不再给出。

- post json

```java
// post json 支持三种参数模式。
// 1. 直接使用 json 字符串的形式。
try {
  final HttpUtils.Task task = httpUtils.url("http://www.example.com/postJson")
      .json()
      .params("{\"name\":\"test\",\"gender\":\"boy\"}")
      .post();
  final String result = task.result();
}
catch (HttpUtils.HttpException e) {
  // 请求出错。
}

// 2. 使用键值对的形式。
try {
  httpUtils.url("http://www.example.com/postJson")
      .json()
      .params(new Object[]{
          "name", "test",
          "gender", "boy",
          "age", 24
      })
      // .params(new String[]{})
      .post();
}
catch (HttpUtils.HttpException ignore) {}

// 3. 直接使用 JSONObject 对象。
try {
  final JSONObject param = new JSONObject();
  try {
    param.put("name", "test");
    param.put("gender", "boy");
  }
  catch (JSONException ignore) {}

  httpUtils.url("http://www.example.com/postJson")
      .json()
      .params(param)
      .post();
}
catch (HttpUtils.HttpException ignore) {}
```

- post urlencoded

```java
try {
  final HttpUtils.Task task = httpUtils.url("http://www.example.com/postUrlencoded")
      .urlencoded()
      .params(new String[]{
          "name", "test",
          "gender", "boy",
      })
      .post();
  final String result = task.result();
}
catch (HttpUtils.HttpException e) {
  // 请求出错。
}
```

- post multi-part

上传文件传入 File 对象即可。

```java
try {
  final HttpUtils.Task task = httpUtils.url("http://www.example.com/postUrlencoded")
      .urlencoded()
      .params(new Object[]{
          "name", "test",
          "gender", "boy",
          // 传递文件。
          "icon", new File("header")
      })
      .post();
  final String result = task.result();
}
catch (HttpUtils.HttpException e) {
  // 请求出错。
}
```
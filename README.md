# NetBird

用于网络请求，架构和接口设计~~参考~~（抄袭）了[okhttp](https://github.com/square/okhttp).

项目之初主要面向 Android 开发，几经重构将 Android 支持、日志、自定义缓存等模块分离，但整个项目仍以面向 Android 开发为主，结合实际需求逐步完善。项目本身提供了便利的同时也带来一些局限，主要在于 POST 类请求的参数必须以 Name-Value 对的形式提供，而无法像 [okhttp](https://github.com/square/okhttp) 那样可只发送 Value，尽管这个问题在移动开发上几乎不会遇到，但有必要在此注明，后续如有需要再考虑提供支持。

## 1. 特性

* 支持同步和异步请求。
* 支持 Android 平台，相关回调均在主线程进行。
* 支持日志、自定义缓存等特性。
* 支持数据解析且可自定义，其中数据解析均在后台线程中执行。
* 支持文件上传、下载及进度的监听。
* 支持对请求和响应进行拦截。

## 2. 模块简介

| 模块名                  | 功能说明                                 |
| ----------------------- | ---------------------------------------- |
| netbird                 | 核心模块，必须。                         |
| netbird-android-support | 提供 Android 平台支持。                  |
| netbird-logging-support | 提供网络请求的日志支持，包括请求和响应。 |
| netbird-cache-support   | 提供自定义缓存支持。                     |
| netbird-gson-parser     | 提供 Gson 数据解析支持。                 |
| netbird-jackson-parser  | 提供 Jackson 数据解析支持。              |
| netbird-jackson-parser  | 提供 fastjson 数据解析支持。             |

## 3. 用法举例

（1）初始化 NetBird

```java
NetBird netBird = new NetBird.Builder("http://www.imooc.com/")
    .addTailInterceptor(new LoggingTailInterceptor(true))
    .build();
```

（2）同步请求

```java
Request request = new Request.Builder()
    .path("api/teacher")
    .add("type", "4")
    .add("num", "30")
    .get()
    .build();
Response response = netBird.newCall(request).execute();
if (response.responseCode() == 200 && response.responseBody() != null) {
    String content = response.responseBody().string();
    System.out.println(content);
}
```

（3）异步请求

```java
Request request = new Request.Builder()
    .path("api/teacher")
    .add("type", "4")
    .add("num", "30")
    .get()
    .build();
netBird.newCall(request).enqueue(new Callback() {
    @Override
    public void onStart() {
    }
    
    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.responseCode() == 200 && response.responseBody() != null) {
            String content = response.responseBody().string();
            System.out.println(content);
        }    
    }
    
    @Override
    public void onFailure(Call call, StateIOException cause) {
    }

    @Override
    public void onFinish() {
    }
});
```

（4）含数据解析的异步请求

```java
Parser<Result<List<Course>>> parser = new JacksonParser<Result<List<Course>>>() {};
// Parser<Result<List<Course>>> parser = new GsonParser<Result<List<Course>>>() {};
MRequest<Result<List<Course>>> request = new MRequest.Builder<>(parser)
    .path("api/teacher")
    .add("type", "4")
    .add("num", "30")
    .get()
    .listener(new MRequest.SimpleListener<Result<List<Course>>>() {
        @Override
        public void onSuccess(Result<List<Course>> result) {
            super.onSuccess(result);
            System.out.println(result);
        }
    }).build();
netBird.send(request);
```

（5）文件上传

```java
Parser<Result<String>> parser = new GsonParser<Result<String>>() {};
MRequest<Result<String>> request = new MRequest.Builder<>(parser)
    .path("api/avatar")
    .add("id", "xxxxx")
    .addFile("avatar", "image/png", new File("avatarPath"), new UploadListener() {
        @Override
        public void onChanged(long finished, long total, int percent) {
        }
    })
    .post()
    .listener(new MRequest.SimpleListener<Result<String>>() {
        @Override
        public void onSuccess(Result<String> result) {
            super.onSuccess(result);
        }
    }).build();
netBird.send(request);
```

（6）文件下载

```java
FileParser parser = FileParser.create("/Users/colorcat/Downloads/QQBrowser.dmg");
MRequest<File> request = new MRequest.Builder<>(parser)
    .url("https://dldir1.qq.com/invc/tt/QQBrowser_for_Mac.dmg")
    .downloadListener(new DownloadListener() {
        @Override
        public void onChanged(long finished, long total, int percent) {
            System.out.printf("finished=%d, total=%d, percent=%d\n", finished, total, percent);
        }
    })
    .listener(new MRequest.SimpleListener<File>() {
        @Override
        public void onSuccess(File result) {
            super.onSuccess(result);
            System.out.println(result.getAbsolutePath());
        }
    })
    .get()
    .build();
netBird.send(request);
```

（7）其它

对于 Android 开发，常见数据封装如下：

```java
public class Result<T> {
    public static final int STATUS_OK = 1;

    @SerializedName("status")
    private int status;
    @SerializedName("msg")
    private String msg;
    @SerializedName("data")
    private T data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
```

但真正需要的是其中 data 部分，针对此种情况可自定义 Parser 统一过滤 Result 中的失败结果，避免在每一个回调中都要判断状态和数据。

```java
public class ResultParser<T> implements Parser<T> {
    private static final Gson GSON = new GsonBuilder().create();

    public static <T> ResultParser<T> create(TypeToken<Result<T>> token) {
        if (token == null) throw new NullPointerException("token == null");
        return new ResultParser<>(token);
    }

    private final TypeToken<Result<T>> mToken;

    private ResultParser(TypeToken<Result<T>> token) {
        mToken = token;
    }

    @Override
    public NetworkData<? extends T> parse(Response response) throws IOException {
        try {
            Reader reader = response.responseBody().reader();
            Result<T> result = GSON.fromJson(reader, mToken.getType());
            if (result != null) {
                T data = result.getData();
                if (result.getStatus() == Result.STATUS_OK && data != null) {
                    return NetworkData.newSuccess(data);
                }
                return NetworkData.newFailure(result.getStatus(), result.getMsg());
            }
            return NetworkData.newFailure(response.responseCode(), response.responseMsg());
        } catch (Exception e) {
            throw new StateIOException(response.responseCode(), response.responseMsg(), e);
        }
    }
}
```

以上的 Parser 采用 gson，需要针对性的传 TypeToken，实际可做到自动生成 Parser，可参考 [sample](https://github.com/ccolorcat/Sample).

## 4. 使用方法

(1) 在项目的 build.gradle 中配置仓库地址：

```groovy
allprojects {
    epositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

(2) 添加项目依赖：

```groovy
dependencies {
    implementation 'com.github.ccolorcat.NetBird:netbird:v4.3.0'
    implementation 'com.github.ccolorcat.NetBird:netbird-android-support:v4.3.0'
    implementation 'com.github.ccolorcat.NetBird:netbird-logging-support:v4.3.0'
    implementation 'com.github.ccolorcat.NetBird:netbird-cache-support:v4.3.0'
    implementation 'com.github.ccolorcat.NetBird:netbird-gson-parser:v4.3.0'
    implementation 'com.github.ccolorcat.NetBird:netbird-jackson-parser:v4.3.0'
    implementation 'com.github.ccolorcat.NetBird:netbird-fastjson-parser:v4.3.0'
}
```

## 5. 版本历史

v4.3.0

> 1. JsonPaser 添加获取 Type[] 的方法。
> 2. 添加 fastjson 解析的支持。

v4.2.1

> 1. 优化 Headers 和 Parameters，使其结构更加合理。
> 2. Parameters 添加两个静态方法，便于直接创建 Parameters 实例。

v4.2.0

> 1. 添加 netbird-gson-parser
> 2. 添加 netbird-jackson-parser

v4.1.0

> 首次公开发布。
>
> 为什么一上来就是 v4.1.0, 因为从开发到重构已有 4 个版本，这次使用新的 github 帐号正式发布。
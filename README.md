# NetBird

用于网络请求，架构和接口设计~~参考~~（抄袭了）[okhttp](https://github.com/square/okhttp). 

项目之初主要面向 Android 开发，后重构数次将 Android 支持、日志、数据解析等模块分离，但整个项目仍以面向 Android 开发为主。项目本身提供了便利的同时也带来一些局限，主要在于 POST 类请求的参数必须以 Name-Value 对的形式提供，而无法像 [okhttp](https://github.com/square/okhttp) 那样可以只发送 Value，尽管这个问题在移动开发上几乎不会遇到，但有必要在此注明，后续如有需要再考虑提供支持。

## 1. 特性

* 支持同步和异步请求。
* 支持 Android 平台，相关回调均在主线程进行。
* 支持日志、自定义缓存等特性。
* 支持数据解析且可自定义，其中数据解析均在后台线程中执行。
* 支持文件上传、下载及进度的监听。

## 2. 模块简介

| 模块名                  | 功能说明                                 |
| ----------------------- | ---------------------------------------- |
| netbird                 | 核心模块，必须。                         |
| netbird-android-support | 提供 Android 平台支持。                  |
| netbird-logging-support | 提供网络请求的日志支持，包括请求和响应。 |
| netbird-cache-support   | 提供自定义缓存支持。                     |
| netbird-gson-parser     | 提供 Gson 数据解析支持。                 |
| netbird-jackson-parser  | 提供 Jackson 数据解析支持。              |

## 3. 用法举例



## 4. 使用方法

(1) 在项目的 build.gradle 中配置仓库地址：

```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

(2) 添加项目依赖：

```groovy
dependencies {
    implementation 'com.github.ccolorcat.NetBird:netbird:v4.2.0'
    implementation 'com.github.ccolorcat.NetBird:netbird-android-support:v4.2.0'
    implementation 'com.github.ccolorcat.NetBird:netbird-logging-support:v4.2.0'
    implementation 'com.github.ccolorcat.NetBird:netbird-cache-support:v4.2.0'
    implementation 'com.github.ccolorcat.NetBird:netbird-gson-parser:v4.2.0'
    implementation 'com.github.ccolorcat.NetBird:netbird-jackson-parser:v4.2.0'
}
```

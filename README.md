# NetBird

用于网络请求，提供 Android 支持。

## 1. 特性

* 支持 Android 平台，使用 MRequest, 相关回调均在主线程进行。
* 支持日志、自定义缓存等特性。

## 2. 用法举例

todo

## 3. 使用方法

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
    implementation 'com.github.ccolorcat:netbird:v4.2.0' // 基础模块，必须。
    implementation 'com.github.ccolorcat:netbird-android-support:v4.2.0' // Android 支持
    implementation 'com.github.ccolorcat:netbird-cache-support:v4.2.0' // 缓存支持
    implementation 'com.github.ccolorcat:netbird-logging-support:v4.2.0' // 日志支持
    implementation 'com.github.ccolorcat:netbird-gson-parser:v4.2.0' // gson 数据解析支持
    implementation 'com.github.ccolorcat:netbird-jackson-parser:v4.2.0' // jackson 数据分析支持
}
```

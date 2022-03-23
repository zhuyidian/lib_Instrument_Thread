# lib_Instrument_Thread

## 1.使用
* 1，初始化
```xml
ThreadHelp.monitorThreadInit();
```
* 2，测试native崩溃
```xml
合适的位置去监控
ThreadHelp.monitorAllThread();
```
## 2.项目引用
* 1，root build.gradle中
```groovy
classpath 'com.hujiang.aspectjx:gradle-android-plugin-aspectjx:2.0.8'
```
* 2，module build.gradle中
```groovy
apply plugin: 'android-aspectjx'
implementation 'com.github.zhuyidian.lib_Instrument:excel:V1.1.8'
```
* 3，module build.gradle中
```groovy
在defaultConfig闭包中
ndk{
    abiFilters "armeabi-v7a"
}
```
## 3.版本更新
* V1.0.5
```
首次成功运行版本
```

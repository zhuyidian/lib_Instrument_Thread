# lib_Instrument_Thread

## 1.使用
* 1，初始化
```xml
ThreadHelp.monitorThreadInit();
```
* 2，开始一次监控
```xml
合适的位置去监控
ThreadHelp.monitorAllThread();
```
* 3，开始线程创建销毁监控
```xml
ThreadHookHelp.monitorThreadCreate();
```
* 4，TAG过滤
```xml
ThreadHookHelp ThreadHelp ThreadMethodHook ThreadAlive
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
## 3. 项目说明
```
1，监控线程是否死锁
2，线程CPU占用率监控
3，监控线程创建和销毁
```
## 4.版本更新
* V1.0.5
```
首次成功运行版本
```
* V1.0.6
```
线程cpu占用率监控
```

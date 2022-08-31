# Thread

## 1.使用
* 1，初始化，建议在APP启动的时候就去初始化
```xml
ThreadHelp.monitorThreadInit();
```
* 2，开始一次监控，建议在用了一会之后，去获取一次。然后用一会儿再去获取一次。
```xml
合适的位置去监控
ThreadHelp.monitorAllThread();
```
* 3，开始线程创建销毁监控，建议在APP启动的时候就去监听
```xml
ThreadHookHelp.monitorThreadCreate();
```
* 4，TAG过滤
```xml
线程死锁：ThreadHelp
线程CPU占用监控：ThreadAlive
线程创建销毁监控：直接使用远程依赖 ThreadHookHelp  ThreadMethodHook
线程创建销毁监控：源码编译 ThreadHookSrcHelp  ThreadHookSrcMethod
```
## 2. 功能说明
```
1，监控线程是否死锁
2，线程CPU占用率监控
3，监控线程创建和销毁
```
## 3.项目引用
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
## 4.版本更新
* V1.0.5
```
首次成功运行版本
```
* V1.0.6
```
线程cpu占用率监控
```

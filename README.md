# lib_Instrument_CrashLocal

## 1.使用
* 1，初始化
```xml
//将native和java层的异常信息全部抛到java层的onCrash方法
NativeCrashMonitor nativeCrashMonitor = new NativeCrashMonitor();
  nativeCrashMonitor.init(new CrashHandlerListener() {
      @Override
      public void onCrash(String threadName, Error error) {

      }
});
```
* 2，测试native崩溃
```xml
NativeCrashMonitor.nativeCrash();
```

# 高德地图定位 cordova 插件

> 支持ios, android
开通服务: [http://lbs.amap.com](http://lbs.amap.com)

## 安装

```
cordova plugin add cordova-plugin-amap-location --variable IOS_KEY=你的KEY --variable ANDROID_KEY=你的KEY --save
```
或
```
ionic cordova plugin add cordova-plugin-amap-location --variable IOS_KEY=你的KEY --variable ANDROID_KEY=你的KEY
```

> 相关依赖
[cordova-plugin-cocoapod-support](https://www.npmjs.com/package/cordova-plugin-cocoapod-support)
```
cordova plugin add cordova-plugin-cocoapod-support --save
```
或
```
ionic cordova plugin add cordova-plugin-cocoapod-support
```

## 使用方法
> 配置
```js
var config = {
  // ios配置
  locationTimeout: 10, // 定位Timeout(s)
  reGeocodeTimeout: 10, // 地址信息Timeout(s)
  iosAccuracy: 1000, // 精确度(m)
  distanceFilter: 10, // 连续定位最小位移(m)
  watchWithReGeocode: false, // 连续定位是否返回地址信息
  iosBackground: true, // 后台定位

  // android配置
  interval: 2000, // 连续定位时间间隔（ms）
  androidAccuracy: 1, // 精确度 0. Battery_Saving 1. Hight_Accuracy, 2. Device_Sensors
  needAddress: true // 是否返回地址信息

}
```
> 单次定位
```js
window.AmapLocation.getCurrentPosition(
  config
  ,
  x => {
    console.log(x);
  },
  e => console.error(e)
);

```
> 连续定位
```js
window.AmapLocation.watchPosition(
  config
  ,
  x => {
    console.log(x);
  },
  e => console.error(e)
);

```
> 清除连续定位
```js
window.AmapLocation.clearWatch(
  x => {
    console.log(x);
  },
  e => console.error(e)
);
```
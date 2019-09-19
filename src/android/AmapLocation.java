package news.chen.yu.ionic;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;

public class AmapLocation extends CordovaPlugin implements AMapLocationListener{

    String TAG = "GeolocationPlugin";
    String [] permissions = { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION };

    public static AMapLocationClient keepLocationInstance = null;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    boolean keepSendBack = false;
    CallbackContext callback;

    private AMapLocationMode getAmapLocationMode(int mode) {
        switch (mode) {
            case 1:
                return AMapLocationMode.Hight_Accuracy;
            case 2:
                return AMapLocationMode.Device_Sensors;
            default:
                return AMapLocationMode.Battery_Saving;
        }
    }

    private void clearKeepLocationInstance() {
        if (keepLocationInstance != null) {
            keepLocationInstance.stopLocation();
            keepLocationInstance.onDestroy();
            keepLocationInstance = null;
            keepSendBack = false;
        }
    }

    private void config(JSONArray args) throws JSONException {
        this.clearKeepLocationInstance();
        if(locationClient == null) {
            locationClient = new AMapLocationClient(this.cordova.getActivity().getApplicationContext());
        }
        if(locationOption == null) {
            locationOption = new AMapLocationClientOption();
        }

        JSONObject params = new JSONObject(args.optString(0, "{}"));
        AMapLocationMode accuracy = this.getAmapLocationMode(params.optInt("androidAccuraty", 0));
        int interval = params.optInt("interval", 2000);
        boolean needAddress = params.optBoolean("needAddress", true);

        locationOption.setLocationMode(accuracy);
        locationClient.setLocationListener(this);
        locationOption.setNeedAddress(needAddress);
        locationOption.setInterval(interval);
        locationClient.setLocationOption(locationOption);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        callback = callbackContext;
        if (action.equals("getCurrentPosition")) {
            this.config(args);
            //设置为单次定位
            locationOption.setOnceLocation(true);
            // 启动定位
            locationClient.startLocation();
            return true;
        } else if (action.equals("watchPosition")) {
            this.config(args);
            //设置为多次定位
            locationOption.setOnceLocation(false);
            // 启动定位
            locationClient.startLocation();
            keepSendBack = true;
            // 存储持续定位对象用于关闭
            keepLocationInstance = locationClient;
            return true;
        } else if (action.equals("clearWatch")) {
            this.clearKeepLocationInstance();
            callback.success();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(aMapLocation.getTime());
                df.format(date);//定位时间

                JSONObject locationInfo = new JSONObject();
                try {
                    locationInfo.put("locationType", aMapLocation.getLocationType()); //获取当前定位结果来源，如网络定位结果，详见定位类型表
                    locationInfo.put("latitude", aMapLocation.getLatitude()); //获取纬度
                    locationInfo.put("longitude", aMapLocation.getLongitude()); //获取经度
                    locationInfo.put("accuracy", aMapLocation.getAccuracy()); //获取精度信息
                    locationInfo.put("speed", aMapLocation.getSpeed()); //获取速度信息
                    locationInfo.put("bearing", aMapLocation.getBearing()); //获取方向信息
                    locationInfo.put("date", date); //定位时间
                    locationInfo.put("address", aMapLocation.getAddress()); //地址，如果option中设置isNeedAddress为false，则没有此结果
                    locationInfo.put("country", aMapLocation.getCountry()); //国家信息
                    locationInfo.put("province", aMapLocation.getProvince()); //省信息
                    locationInfo.put("city", aMapLocation.getCity()); //城市信息
                    locationInfo.put("district", aMapLocation.getDistrict()); //城区信息
                    locationInfo.put("street", aMapLocation.getStreet()); //街道信息
                    locationInfo.put("streetNum", aMapLocation.getStreetNum()); //街道门牌号
                    locationInfo.put("cityCode", aMapLocation.getCityCode()); //城市编码
                    locationInfo.put("adCode", aMapLocation.getAdCode()); //地区编码
                    locationInfo.put("poiName", aMapLocation.getPoiName());
                    locationInfo.put("aoiName", aMapLocation.getAoiName());
                } catch (JSONException e) {
                    Log.e(TAG, "Locatioin json error:" + e);
                }
                PluginResult result = new PluginResult(PluginResult.Status.OK, locationInfo);
                if (!keepSendBack) { //不持续传回定位信息
                    locationClient.stopLocation(); //只获取一次的停止定位
                } else {
                    result.setKeepCallback(true);
                }
                callback.sendPluginResult(result);
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e(TAG, "Locatioin error:" + aMapLocation.getErrorCode());
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, aMapLocation.getErrorCode());
                callback.sendPluginResult(result);
            }
        }
    }


    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException
    {
        PluginResult result;
        //This is important if we're using Cordova without using Cordova, but we have the geolocation plugin installed
        if(callback != null) {
            for (int r : grantResults) {
                if (r == PackageManager.PERMISSION_DENIED) {
                    LOG.d(TAG, "Permission Denied!");
                    result = new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION);
                    callbcallbackack.sendPluginResult(result);
                    return;
                }

            }
            result = new PluginResult(PluginResult.Status.OK);
            callback.sendPluginResult(result);
        }
    }

    public boolean hasPermisssion() {
        for(String p : permissions)
        {
            if(!PermissionHelper.hasPermission(this, p))
            {
                return false;
            }
        }
        return true;
    }

    /*
     * We override this so that we can access the permissions variable, which no longer exists in
     * the parent class, since we can't initialize it reliably in the constructor!
     */

    public void requestPermissions(int requestCode)
    {
        PermissionHelper.requestPermissions(this, requestCode, permissions);
    }
}
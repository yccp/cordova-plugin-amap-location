#import <AMapFoundationKit/AMapFoundationKit.h>
#import "AmapLocation.h"

@implementation AmapLocation

- (AMapLocationManager *)locationManager {
    NSLog(@"Init location manager");
    if (!_locationManager) {
        NSString *iosKey = [[self.commandDelegate settings] objectForKey:@"ios_key"];
        [AMapServices sharedServices].apiKey = iosKey;
        _locationManager = [[AMapLocationManager alloc]init];
        _locationManager.delegate = self;
    }
    return _locationManager;
}

- (void)config:(CDVInvokedUrlCommand *)command {
    float desiredAccuracy = 2000; // 精确度
    int locationTimeout = 5; // 定位请求超时时间
    int reGeocodeTimeout = 5; // 逆地理请求超时时间，最低2s，此处设置为5s
    int distanceFilter = 200; // 单位米。当两次定位距离满足设置的最小更新距离时，SDK会返回符合要求的定位结果。
    bool watchWithReGeocode = NO; // 持续定位是否返回逆地理信息
    bool iosBackground = NO; // 设置是否允许后台定位
    
    NSError* jsonError;
    NSString* arguments = [command argumentAtIndex:0];
    NSLog(@"arguments: %@", arguments);
    if(arguments != nil) {
        NSData* objectData = [arguments dataUsingEncoding:NSUTF8StringEncoding];
        NSDictionary* options = [NSJSONSerialization JSONObjectWithData:objectData options:NSJSONReadingMutableContainers error:&jsonError];
        if([options objectForKey:@"iosAccuracy"]) {
            desiredAccuracy = [[options valueForKey:@"iosAccuracy"] floatValue];
        }
        if([options objectForKey:@"locationTimeout"]) {
            locationTimeout = [[options valueForKey:@"locationTimeout"] intValue];
        }
        if([options objectForKey:@"reGeocodeTimeout"]) {
            reGeocodeTimeout = [[options valueForKey:@"reGeocodeTimeout"] intValue];
        }
        if([options objectForKey:@"distanceFilter"]) {
            distanceFilter = [[options valueForKey:@"distanceFilter"] intValue];
        }
        if([options objectForKey:@"watchWithReGeocode"]) {
            watchWithReGeocode = [[options valueForKey:@"watchWithReGeocode"] boolValue];
        }
        if([options objectForKey:@"iosBackground"]) {
            iosBackground = [[options valueForKey:@"iosBackground"] boolValue];
        }
    }
    [self.locationManager setDesiredAccuracy:desiredAccuracy];
    self.locationManager.locationTimeout = locationTimeout;
    self.locationManager.reGeocodeTimeout = reGeocodeTimeout;
    self.locationManager.distanceFilter = distanceFilter;
    self.locationManager.locatingWithReGeocode = watchWithReGeocode;
    if ([[[UIDevice currentDevice] systemVersion] floatValue] >= 9) {
        self.locationManager.allowsBackgroundLocationUpdates = iosBackground;
    }
}

- (NSDictionary*)success:(CLLocation*)location with:(AMapLocationReGeocode*)regeocode {
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    return @{@"latitude": [NSNumber numberWithDouble:location.coordinate.latitude],
             @"longitude": [NSNumber numberWithDouble:location.coordinate.longitude],
             @"speed": [NSNumber numberWithDouble:location.speed],
             @"bearing": [NSNumber numberWithDouble:location.course],
             @"accuracy": [NSNumber numberWithDouble:location.horizontalAccuracy],
             @"date": [dateFormatter stringFromDate:location.timestamp],
             @"address": regeocode.formattedAddress ?: @"",
             @"country": regeocode.country ?: @"",
             @"province": regeocode.province ?: @"",
             @"city": regeocode.city ?: @"",
             @"cityCode": regeocode.citycode ?: @"",
             @"district": regeocode.district ?: @"",
             @"street": regeocode.street ?: @"",
             @"streetNum": regeocode.number ?: @"",
             @"adCode": regeocode.adcode ?: @"",
             @"poiName": regeocode.POIName ?: @"",
             @"aoiName": regeocode.AOIName ?: @""};
}

- (NSDictionary*)error:(NSError*)error {
    NSLog(@"locError:{%ld - %@};", (long)error.code, error.localizedDescription);
    return @{@"code": [NSNumber numberWithInteger:error.code],
             @"message": error.localizedDescription};
}

- (void)getCurrentPosition:(CDVInvokedUrlCommand *)command {
    [self config:command];
    __weak __typeof__(self) weakSelf = self;
    
    [self.locationManager requestLocationWithReGeocode:YES completionBlock:^(CLLocation *location, AMapLocationReGeocode *regeocode, NSError *error) {
        
        if (error) {
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:[self error:error]];
            [weakSelf.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        } else {
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:[self success:location with:regeocode]];
            [weakSelf.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];
}

- (void)watchPosition:(CDVInvokedUrlCommand*)command
{
    self.callback = command.callbackId;
    [self config:command];
    [self.locationManager startUpdatingLocation];
}

- (void)amapLocationManager:(AMapLocationManager *)manager didUpdateLocation:(CLLocation *)location reGeocode:(AMapLocationReGeocode *)reGeocode
{
    NSLog(@"location:{lat:%f; lon:%f; accuracy:%f}", location.coordinate.latitude, location.coordinate.longitude, location.horizontalAccuracy);
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:[self success:location with:reGeocode]];
        
    if (result) {
        [result setKeepCallbackAsBool:YES];
        [[self commandDelegate] sendPluginResult:result callbackId: self.callback];
    }
}

- (void)clearWatch:(CDVInvokedUrlCommand*)command
{
    [self.locationManager stopUpdatingLocation];
    CDVPluginResult* result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK];
    [[self commandDelegate] sendPluginResult:result callbackId: command.callbackId];
}

@end
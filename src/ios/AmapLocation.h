#import <Cordova/CDVPlugin.h>
#import <AMapLocationKit/AMapLocationKit.h>

@interface AmapLocation : CDVPlugin {}

@property (nonatomic, strong) AMapLocationManager *locationManager;
@property (retain, nonatomic) IBOutlet NSString *callback;

- (void)getCurrentPosition:(CDVInvokedUrlCommand*)command;
- (void)watchPosition:(CDVInvokedUrlCommand *)command;
- (void)clearWatch:(CDVInvokedUrlCommand*)command;

@end
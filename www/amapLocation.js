// AlicloudFeedback
// Author: Yu Chen <yu.chen@live.ie>
// License: Apache License 2.0

'use strict';

module.exports = {
  /**
   * @param {object|string} options
   * @param {Function} successCallback ['success']
   * @param {Function} errorCallback ['fail'|'cancel'|'invalid']
   */
  getCurrentPosition: function (param, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "AmapLocation", "getCurrentPosition", [JSON.stringify(param)]);
  },
  watchPosition: function (param, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "AmapLocation", "watchPosition", [JSON.stringify(param)]);
  },
  clearWatch: function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "AmapLocation", "clearWatch", []);
  },
};
/*global cordova, module*/

module.exports = {
  echo: function (name, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Echo", "echo", [name]);
  }
};

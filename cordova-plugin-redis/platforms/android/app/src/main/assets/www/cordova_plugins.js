cordova.define('cordova/plugin_list', function(require, exports, module) {
  module.exports = [
    {
      "id": "cordova-plugin-redis.Redis",
      "file": "plugins/cordova-plugin-redis/www/Redis.js",
      "pluginId": "cordova-plugin-redis",
      "clobbers": [
        "cordova.plugins.Redis"
      ]
    }
  ];
  module.exports.metadata = {
    "cordova-plugin-whitelist": "1.3.3",
    "cordova-plugin-redis": "0.1.0"
  };
});
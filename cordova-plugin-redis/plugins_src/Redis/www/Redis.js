var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'Redis', 'coolMethod', [arg0]);
};

exports.redisMethod = function (arg0, success, error) {
    exec(success, error, 'Redis', 'redisMethod', [arg0]);
};



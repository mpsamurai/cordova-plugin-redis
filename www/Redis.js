var exec = require('cordova/exec');

exports.initialize = function (host, port, success, error) {
    exec(success, error, 'Redis', 'initialize', [host, port]);
};

exports.setStringValue = function (key, value, success, error) {
    exec(success, error, 'Redis', 'setStringValue', [key, value]);
};

exports.setIntegerValue = function (key, value, success, error) {
    exec(success, error, 'Redis', 'setIntegerValue', [key, value]);
};

exports.setJsonValue = function (key, value, success, error) {
    exec(success, error, 'Redis', 'setJsonValue', [key, value]);
};

exports.getStringValue = function (key, success, error) {
    exec(success, error, 'Redis', 'getStringValue', [key]);
};

exports.getIntegerValue = function (key, success, error) {
    exec(success, error, 'Redis', 'getIntegerValue', [key]);
};

exports.getJsonValue = function (key, success, error) {
    exec(success, error, 'Redis', 'getJsonValue', [key]);
};

exports.publish = function (channel, message, success, error) {
    exec(success, error, 'Redis', 'publish', [channel, message]);
};

exports.subscribe = function (channels, success, error) {
    exec(success, error, 'Redis', 'subscribe', [channels]);
};

exports.finalize = function (success, error) {
    exec(success, error, 'Redis', 'finalize', []);
};

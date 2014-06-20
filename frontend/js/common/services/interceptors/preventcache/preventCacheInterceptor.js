angular.module('nav.services.interceptor.cache', [])
    .factory('httpRequestInterceptorPreventCache', [function() {
        return {
            'request': function(config) {
                if (getIEVersion > 0) {
                    if (config.method === "GET" && config.url.indexOf('.html') < 0) {
                        config.url = config.url + '?rand=' + new Date().getTime();
                    }
                }
                return config;
            }
        };
    }]);
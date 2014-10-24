/* global getIEVersion, TimeoutBox, window*/

(function () {
    'use strict';
    angular.module('gjenopptak.services', [
            'ngResource',
            'nav.services.interceptor.cache',
            'nav.services.interceptor.timeout',
            'nav.services.interceptor.feilhandtering',
            'nav.services.resolvers',
            'nav.services.soknad'
        ])

        .config(['$httpProvider', function ($httpProvider) {
            $httpProvider.interceptors.push('resetTimeoutInterceptor');
            $httpProvider.interceptors.push('feilhandteringInterceptor');

            if (getIEVersion() < 10) {
                $httpProvider.interceptors.push('httpRequestInterceptorPreventCache');
            }
        }]);
}());
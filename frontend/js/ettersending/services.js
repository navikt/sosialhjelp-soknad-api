/* global getIEVersion, TimeoutBox, window*/

(function () {
    'use strict';
    angular.module('ettersending.services', [
            'ngResource',
            'nav.services.ettersending',
            'nav.services.interceptor.cache',
            'nav.services.interceptor.timeout',
            'nav.services.interceptor.xsrf',
            'nav.services.fortsettsenere',
            'nav.services.vedlegg'
        ])

        .config(['$httpProvider', function ($httpProvider) {
            $httpProvider.interceptors.push('resetTimeoutInterceptor');
            $httpProvider.interceptors.push('xsrfRelast');

            if (getIEVersion() < 10) {
                $httpProvider.interceptors.push('httpRequestInterceptorPreventCache');
            }
        }]);
}());
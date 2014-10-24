/* global getIEVersion, TimeoutBox, window*/

(function () {
    'use strict';
    angular.module('utslagskriterierDagpenger.services', [
            'ngResource',
            'nav.services.faktum',
            'nav.services.soknad',
            'nav.services.personalia',
            'nav.services.interceptor.cache',
            'nav.services.interceptor.timeout',
            'nav.services.interceptor.feilhandtering',
            'nav.services.resolvers'
        ])

        .config(['$httpProvider', function ($httpProvider) {
            $httpProvider.interceptors.push('resetTimeoutInterceptor');
            $httpProvider.interceptors.push('feilhandteringInterceptor');

            if (getIEVersion() < 10) {
                $httpProvider.interceptors.push('httpRequestInterceptorPreventCache');
            }
        }]);
}());
/* global getIEVersion, TimeoutBox, window*/

(function () {
    'use strict';
    angular.module('ettersending.services', [
            'ngResource',
            'nav.services.ettersending',
            'nav.services.interceptor.cache',
            'nav.services.interceptor.timeout',
            'nav.services.interceptor.feilhandtering',
            'nav.services.vedlegg',
            'nav.services.faktum',
            'nav.services.personalia',
            'nav.services.resolvers',
            'nav.services.bekreftelse'
        ])

        .config(['$httpProvider', function ($httpProvider) {
            $httpProvider.interceptors.push('resetTimeoutInterceptor');
            $httpProvider.interceptors.push('feilhandteringInterceptor');

            if (getIEVersion() < 10) {
                $httpProvider.interceptors.push('httpRequestInterceptorPreventCache');
            }
        }]);
}());
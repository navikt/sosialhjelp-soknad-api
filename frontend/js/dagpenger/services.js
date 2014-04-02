/* global getIEVersion, TimeoutBox, window*/

(function () {
    'use strict';

    angular.module('sendsoknad.services', [
            'ngResource',
            'nav.services.land',
            'nav.services.vedlegg',
            'nav.services.faktum',
            'nav.services.soknad',
            'nav.services.interceptor.delsteg',
            'nav.services.interceptor.cache',
            'nav.services.interceptor.timeout',
            'nav.services.interceptor.xsrf',
            'nav.services.fortsettsenere'
        ])

        .config(['$httpProvider', function ($httpProvider) {
            $httpProvider.interceptors.push('resetTimeoutInterceptor');
            $httpProvider.interceptors.push('settDelstegStatusEtterKallMotServer');
            $httpProvider.interceptors.push('xsrfRelast');

            if (getIEVersion() < 10) {
                $httpProvider.interceptors.push('httpRequestInterceptorPreventCache');
            }
        }]);
}());
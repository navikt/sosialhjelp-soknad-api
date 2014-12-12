/* global getIEVersion, TimeoutBox, window*/

(function () {
    'use strict';

    angular.module('sendsoknad.services', [
            'ngResource',
            'nav.services.vedlegg',
            'nav.services.faktum',
            'nav.services.soknad',
            'nav.services.interceptor.delsteg',
            'nav.services.interceptor.cache',
            'nav.services.interceptor.timeout',
            'nav.services.interceptor.feilhandtering',
            'nav.services.fortsettsenere',
            'nav.services.resolvers',
            'nav.services.bekreftelse'
        ])

        .config(['$httpProvider', function ($httpProvider) {
            $httpProvider.interceptors.push('resetTimeoutInterceptor');
            $httpProvider.interceptors.push('settDelstegStatusEtterKallMotServer');
            $httpProvider.interceptors.push('feilhandteringInterceptor');

            if (getIEVersion() < 10) {
                $httpProvider.interceptors.push('httpRequestInterceptorPreventCache');
            }
        }]);
}());
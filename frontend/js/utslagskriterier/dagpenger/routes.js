angular.module('utslagskriterierDagpenger.routes', ['ngRoute', 'nav.common.routes'])
    .config(function ($routeProvider) {
        $routeProvider
            .when('/cmstekster', {
                redirectTo: '/',
                resolve: {
                    notUsedButRequiredProperty: function ($rootScope) {
                        $rootScope.visCmsnokkler = true;
                        return true;
                    }
                }
            })
            .when('/', {
                templateUrl: '../views/utslagskriterier/dagpenger/utslagskritererDagpenger.html',
                controller: 'utslagskritererDagpengerCtrl',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    config: function (ConfigResolver) {
                        return ConfigResolver;
                    },
                    utslagskriterier: function (UtslagskriterierResolver) {
                        return UtslagskriterierResolver;
                    },
                    soknad: function() {
                        return {};
                    }
                }
            })
            .when('/:behandlingsId', {
                templateUrl: '../views/utslagskriterier/dagpenger/utslagskritererDagpenger.html',
                controller: 'utslagskritererDagpengerCtrl',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    config: function (ConfigResolver) {
                        return ConfigResolver;
                    },
                    utslagskriterier: function (UtslagskriterierResolver) {
                        return UtslagskriterierResolver;
                    },
                    soknad: function() {
                        return {};
                    }
                }
            })
            .when('/routing/dagpenger', {
                templateUrl: '../views/utslagskriterier/dagpenger/routingForGjenopptak.html',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    config: function (ConfigResolver) {
                        return ConfigResolver;
                    }
                }
            });
    });
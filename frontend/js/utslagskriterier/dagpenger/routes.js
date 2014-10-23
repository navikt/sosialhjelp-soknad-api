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
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    config: function (ConfigResolver) {
                        return ConfigResolver;
                    },
                    utslagskriterier: function(UtslagskriterierResolver) {
                        return UtslagskriterierResolver;
                    },
                    soknadMetadata: function(SoknadMetadataResolver) {
                        return SoknadMetadataResolver;
                    }
                }
            })
            .when('/routing', {
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
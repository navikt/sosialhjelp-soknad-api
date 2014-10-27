angular.module('gjenopptak.routes', ['ngRoute', 'nav.common.routes'])
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when('/cmstekster', {
                redirectTo: '/',
                resolve: {
                    notUsedButRequiredProperty: ['$rootScope', function ($rootScope) {
                        $rootScope.visCmsnokkler = true;
                        return true;
                    }]
                }
            })
            .when('/informasjonsside', {
                templateUrl: '../views/templates/informasjonsside.html',
                controller: 'InformasjonsSideCtrl',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    config: function (ConfigResolver) {
                        return ConfigResolver;
                    }
                }
            })
            .when('/soknad', {
                templateUrl: '../views/gjenopptak/gjenopptak-skjema.html',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    land: function (LandResolver) {
                        return LandResolver;
                    },
                    soknad: function(SoknadResolver) {
                        return SoknadResolver;
                    },
                    fakta: function(FaktaResolver) {
                        return FaktaResolver;
                    },
                    soknadOppsett: function(SoknadOppsettResolver) {
                        return SoknadOppsettResolver;
                    },
                    config: function(ConfigForSoknadResolver) {
                        return ConfigForSoknadResolver;
                    },
                    behandlingsId: function(BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    }
                }
            })
            .when('/', {
                redirectTo: '/informasjonsside'
            })
            .otherwise({
                redirectTo: '/404'
            });
    }]);
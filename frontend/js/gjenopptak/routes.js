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
            .when('/soknad/:behandlingId', {
                templateUrl: '../views/dagpenger/dagpenger-skjema.html',
                controller: 'GjenopptakCtrl',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    land: function (LandResolver) {
                        return LandResolver;
                    },
                    soknad: function (SoknadResolver) {
                        return SoknadResolver;
                    },
                    soknadOppsett: function (SoknadOppsettResolver) {
                        return SoknadOppsettResolver;
                    },
                    config: function (ConfigForSoknadResolver) {
                        return ConfigForSoknadResolver;
                    },
                    behandlingsId: function (BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    },
                    fakta: function (FaktaResolver) {
                        return FaktaResolver;
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
angular.module('nav.common.routes', ['ngRoute'])

    .config(function ($routeProvider) {
        $routeProvider
            .when('/feilside', {
                templateUrl: '../views/common/feilsider/feilsideBaksystem.html',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    config: function (ConfigResolver) {
                        return ConfigResolver;
                    }
                }
            })
            .when('/feilside/soknadikkefunnet', {
                templateUrl: '../views/common/feilsider/soknadIkkeFunnetFeilside.html',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    config: function (ConfigResolver) {
                        return ConfigResolver;
                    }
                }
            })
            .when('/404', {
                templateUrl: '../views/common/feilsider/feilside404.html',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    config: function (ConfigResolver) {
                        return ConfigResolver;
                    }
                }
            })
            .when('/bekreftelse/:behandlingsId', {
                templateUrl: '../views/common/innsendingbekreftelse/bekreftelse.html',
                controller: 'BekreftelsesCtrl',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    config: function(ConfigResolver) {
                        return ConfigResolver;
                    },
                    soknad: function(SoknadResolver) {
                        return SoknadResolver;
                    }
                }
            })
            .otherwise({redirectTo: '/404'});
    })
    .run(function ($rootScope, $location, $anchorScroll, $routeParams) {
        $rootScope.$on('$routeChangeSuccess', function () {
            if (_gaq) {
                var trackPage = "startSoknad";
                _gaq.push(['_trackPageview', '/sendsoknad/' + trackPage]);
            }
            $location.hash($routeParams.scrollTo);
            $anchorScroll();
        });
    });
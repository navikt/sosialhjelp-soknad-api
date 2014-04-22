angular.module('nav.common.routes', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
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
                    }
                }
            })
            .otherwise({redirectTo: '/404'});
    }]);
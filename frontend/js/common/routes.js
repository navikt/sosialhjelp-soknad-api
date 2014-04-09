angular.module('nav.feilsider.routes', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when('/feilside', {
                templateUrl: '../views/common/feilsider/feilsideBaksystem.html',
                resolve: {
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }],
                    config: ['ConfigResolver', function (ConfigResolver) {
                        return ConfigResolver;
                    }]
                }
            })
            .when('/404', {
                templateUrl: '../views/common/feilsider/feilside404.html',
                resolve: {
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }],
                    config: ['ConfigResolver', function (ConfigResolver) {
                        return ConfigResolver;
                    }]
                }
            })
            .otherwise({redirectTo: '/404'});
    }]);
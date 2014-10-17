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
            .when('/gjenopptak', {
                templateUrl: '../views/gjenopptak.html',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    }
                }
            })
    }]);
angular.module('utslagskriterierDagpenger.routes', ['ngRoute', 'nav.common.routes'])
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
            .when('/', {
                templateUrl: '../views/utslagskriterier/dagpenger/dagpenger.html',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    }
                }
            });
    }]);
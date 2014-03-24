angular.module('ettersending.routes', ['ngRoute'])

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
            .when('/feilside', {
                templateUrl: '../views/templates/feilsider/feilsideBaksystem.html',
                resolve: {
                    notUsedButRequiredProperty: ['FeilsideService', function (FeilsideService) {
                        return FeilsideService;
                    }]
                }
            })
            .when('/404', {
                templateUrl: '../views/templates/feilsider/feilside404.html',
                resolve: {
                    notUsedButRequiredProperty: ['FeilsideService', function (FeilsideService) {
                        return FeilsideService;
                    }]
                }
            })
            .when('/', {
                templateUrl: '../views/ettersending/startEttersending.html',
                resolve: {
                    notUsedButRequiredProperty: ['StartEttersendingService', function (StartEttersendingService) {
                        return StartEttersendingService;
                    }]
                }
            })
            .when('/vedlegg', {
                templateUrl: '../views/ettersending/ettersending.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentEttersendingsService', function (HentEttersendingsService) {
                        return HentEttersendingsService;
                    }]
                }
            })
            .when('/opplasting/:vedleggId', {
                templateUrl: '../views/templates/vedlegg/opplasting.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentEttersendingsService', function (HentEttersendingsService) {
                        return HentEttersendingsService;
                    }]
                }
            })
            .when('/visVedlegg/:vedleggId', {
                templateUrl: '../views/templates/vedlegg/visvedlegg.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentEttersendingsService', function (HentEttersendingsService) {
                        return HentEttersendingsService;
                    }]
                }
            })
            .otherwise({redirectTo: '/404'});

    }]).run(['$rootScope', '$location', '$anchorScroll', '$routeParams', function ($rootScope, $location, $anchorScroll, $routeParams) {
        $rootScope.$on('$routeChangeSuccess', function (newRoute, oldRoute) {
            if (_gaq) {
                var trackPage = "startSoknad";
                if (erSoknadStartet()) {
                    trackPage = $location.path().split("/")[1];
                }
                _gaq.push(['_trackPageview', '/sendsoknad/' + trackPage]);
            }
            $location.hash($routeParams.scrollTo);
            $anchorScroll();
        });
    }]);

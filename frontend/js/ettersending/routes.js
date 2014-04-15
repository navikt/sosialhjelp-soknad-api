angular.module('ettersending.routes', ['ngRoute', 'nav.feilsider.routes'])

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
                templateUrl: '../views/ettersending/startEttersending.html',
                resolve: {
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }]
                }
            })
            .when('/vedlegg', {
                templateUrl: '../views/ettersending/ettersending.html',
                controller: 'EttersendingCtrl',
                resolve: {
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }],
                    behandlingskjedeId: function (BehandlingskjedeIdResolver) {
                        return BehandlingskjedeIdResolver;
                    },
                    ettersending: function (EttersendingResolver) {
                        return EttersendingResolver;
                    },
                    vedlegg: function (EttersendingVedleggResolver) {
                        return EttersendingVedleggResolver;
                    }
                }
            })
            .when('/vedlegg/nytt', {
                templateUrl: '../views/ettersending/nytt-vedlegg.html',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    ettersending: function (EttersendingResolver) {
                        return EttersendingResolver;
                    },
                    vedlegg: function (EttersendingVedleggResolver) {
                        return EttersendingVedleggResolver;
                    }
                }
            })
            .when('/opplasting/:vedleggId', {
                templateUrl: '../views/ettersending/opplastingEttersending.html',
                controller: 'EttersendingOpplastingCtrl',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    ettersending: function (EttersendingResolver) {
                        return EttersendingResolver;
                    },
                    vedlegg: function (EttersendingVedleggResolver) {
                        return EttersendingVedleggResolver;
                    }
                }
            })
            .when('/avbryt', {
                templateUrl: '../views/ettersending/avbryt.html',
                resolve: {
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }],
                    ettersending: ['EttersendingResolver', function (EttersendingResolver) {
                        return EttersendingResolver;
                    }]
                }
            })
            .when('/visVedlegg/:vedleggId', {
                templateUrl: '../views/templates/vedlegg/visvedlegg.html',
                resolve: {
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }],
                    ettersending: ['EttersendingResolver', function (EttersendingResolver) {
                        return EttersendingResolver;
                    }]
                }
            });
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

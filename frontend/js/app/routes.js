//TODO: Fjern sider som ikke lengre skal være side...som reell arebeidssøker, arbeidsforhold etc

angular.module('app.routes', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when('/cmstekster', {
                redirectTo: '/informasjonsside',
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
                    notUsedButRequiredProperty: ['InformasjonsSideResolver', function (InformasjonsSideResolver) {
                        return InformasjonsSideResolver;
                    }]
                }
            })
            .when('/behandling/:behandlingId', {
                templateUrl: '../views/templates/informasjonsside.html',
                controller: 'BehandlingCtrl',
                resolve: {
                    notUsedButRequiredProperty: ['BehandlingSideResolver', function (BehandlingSideResolver) {
                        return BehandlingSideResolver;
                    }]
                }
            })
            .when('/vedlegg', {
                templateUrl: '../views/templates/vedlegg/vedlegg.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentSoknadService', function (HentSoknadService) {
                        return HentSoknadService;
                    }]
                }
            })
            .when('/opplasting/:vedleggId', {
                templateUrl: '../views/templates/vedlegg/opplasting.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentSoknadService', function (HentSoknadService) {
                        return HentSoknadService;
                    }]
                }
            })
            .when('/visVedlegg/:vedleggId', {
                templateUrl: '../views/templates/vedlegg/visvedlegg.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentSoknadService', function (HentSoknadService) {
                        return HentSoknadService;
                    }]
                }
            })
            .when('/soknad', {
                templateUrl: '../views/dagpenger-singlepage.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentSoknadService', function (HentSoknadService) {
                        return HentSoknadService;
                    }]
                }
            })
            .when('/fortsettsenere', {
                templateUrl: '../views/templates/fortsettSenere.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentSoknadService', function (HentSoknadService) {
                        return HentSoknadService;
                    }]
                }
            })
            .when('/kvittering-fortsettsenere', {
                templateUrl: '../views/templates/kvittering-fortsettsenere.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentSoknadService', function (HentSoknadService) {
                        return HentSoknadService;
                    }]
                }
            })
            .when('/nyttbarn', {
                templateUrl: '../views/templates/barnetillegg/barnetillegg-nyttbarn.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentSoknadService', function (HentSoknadService) {
                        return HentSoknadService;
                    }]
                }
            })
            .when('/endrebarn/:faktumId', {
                templateUrl: '../views/templates/barnetillegg/barnetillegg-nyttbarn.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentSoknadService', function (HentSoknadService) {
                        return HentSoknadService;
                    }]
                }
            })
            .when('/sokbarnetillegg/:faktumId', {
                templateUrl: '../views/templates/barnetillegg/endreSystembarnTemplate.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentSoknadService', function (HentSoknadService) {
                        return HentSoknadService;
                    }]
                }
            })
            .when('/nyttarbeidsforhold', {
                templateUrl: '../views/templates/arbeidsforhold-nytt.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentSoknadService', function (HentSoknadService) {
                        return HentSoknadService;
                    }]
                }
            })
            .when('/endrearbeidsforhold/:faktumId', {
                templateUrl: '../views/templates/arbeidsforhold-nytt.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentSoknadService', function (HentSoknadService) {
                        return HentSoknadService;
                    }]
                }
            })
            .when('/oppsummering', {
                templateUrl: '../views/templates/oppsummering.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentSoknadService', function (HentSoknadService) {
                        return HentSoknadService;
                    }]
                }
            })
            .when('/bekreftelse', {
                templateUrl: '../views/templates/bekreftelse.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentSoknadService', function (HentSoknadService) {
                        return HentSoknadService;
                    }]
                }
            })
            .when('/avbryt', {
                templateUrl: '../views/templates/avbryt.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentSoknadService', function (HentSoknadService) {
                        return HentSoknadService;
                    }]
                }
            })
            .when('/slettet', {
                templateUrl: '../views/templates/soknadSlettet.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentSoknadService', function (HentSoknadService) {
                        return HentSoknadService;
                    }]
                }
            })
            .when('/ferdigstilt', {
                templateUrl: '../views/templates/ferdigstilt.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentSoknadService', function (HentSoknadService) {
                        return HentSoknadService;
                    }]
                }
            })
            .when('/feilside', {
                templateUrl: '../views/templates/feilsider/feilsideBaksystem.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentSoknadService', function (HentSoknadService) {
                        return HentSoknadService;
                    }]
                }
            })
            .when('/404', {
                templateUrl: '../views/templates/feilsider/feilside404.html',
                resolve: {
                    notUsedButRequiredProperty: ['HentSoknadService', function (HentSoknadService) {
                        return HentSoknadService;
                    }]
                }
            })
            .when('/',
                {redirectTo: '/informasjonsside'}
            )
            .when('/soknadliste', {templateUrl: '../views/templates/soknadliste.html'})
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

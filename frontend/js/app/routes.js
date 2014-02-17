//TODO: Fjern sider som ikke lengre skal være side...som reell arebeidssøker, arbeidsforhold etc

angular.module('app.routes', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when('/informasjonsside', {
                templateUrl: '../views/templates/informasjonsside.html',
                controller: 'InformasjonsSideCtrl',
                resolve: {
                    notUsedButRequiredProperty: function (InformasjonsSideResolver) {
                        return InformasjonsSideResolver;
                    }
                }
            })
            .when('/behandling/:behandlingId', {
                templateUrl: '../views/templates/informasjonsside.html',
                controller: 'BehandlingCtrl',
                resolve: {
                    notUsedButRequiredProperty: function (BehandlingSideResolver) {
                        return BehandlingSideResolver;
                    }
                }
            })
            .when('/vedlegg', {
                templateUrl: '../views/templates/vedlegg.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/opplasting/:vedleggId', {
                templateUrl: '../views/templates/opplasting.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/visVedlegg/:vedleggId', {
                templateUrl: '../views/templates/visvedlegg.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/soknad', {
                templateUrl: '../views/dagpenger-singlepage.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/fortsettsenere', {
                templateUrl: '../views/templates/fortsettSenere.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/kvittering-fortsettsenere', {
                templateUrl: '../views/templates/kvittering-fortsettsenere.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/reell-arbeidssoker', {
                templateUrl: '../views/templates/reell-arbeidssoker.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/egennaering', {
                templateUrl: '../views/templates/egen-naering.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/verneplikt', {
                templateUrl: '../views/templates/verneplikt.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/personalia', {
                templateUrl: '../views/templates/personalia.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/barnetillegg', {
                templateUrl: '../views/templates/barnetillegg.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/nyttbarn', {
                templateUrl: '../views/templates/barnetillegg-nyttbarn.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/endrebarn/:faktumId', {
                templateUrl: '../views/templates/barnetillegg-nyttbarn.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/sokbarnetillegg/:faktumId', {
                templateUrl: '../views/templates/barnetillegg-nyttbarn.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/fritekst', {
                templateUrl: '../views/templates/fritekst.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/arbeidsforhold', {
                templateUrl: '../views/templates/arbeidsforhold.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/nyttarbeidsforhold', {
                templateUrl: '../views/templates/arbeidsforhold-nytt.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/endrearbeidsforhold/:faktumId', {
                templateUrl: '../views/templates/arbeidsforhold-nytt.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/ytelser', {
                templateUrl: '../views/templates/ytelser.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/oppsummering', {
                templateUrl: '../views/templates/oppsummering.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/bekreftelse', {
                templateUrl: '../views/templates/bekreftelse.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/utdanning', {
                templateUrl: '../views/templates/utdanning.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/avbryt', {
                templateUrl: '../views/templates/avbryt.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/slettet', {
                templateUrl: '../views/templates/soknadSlettet.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/ferdigstilt', {
                templateUrl: '../views/templates/ferdigstilt.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/feilside', {
                templateUrl: '../views/templates/feilside.html',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/soknadliste', {templateUrl: '../views/templates/soknadliste.html'})
            .otherwise({redirectTo: '/informasjonsside'});

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

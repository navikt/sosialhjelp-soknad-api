angular.module('app.routes', ['ngRoute'])

    .config(function ($routeProvider, $locationProvider) {
        $routeProvider
            .when('/informasjonsside', {
                templateUrl: '../html/templates/informasjonsside.html',
                resolve: {
                    notUsedButRequiredProperty: function (TekstService) {
                        return TekstService;
                    }
                }
            })
            .when('/opplasting/:soknadId', {
                templateUrl: '../html/templates/opplasting.html',
                controller: 'SoknadDataCtrl',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/dagpenger/:soknadId', {
                templateUrl: '../html/dagpenger-singlepage.html',
                controller: 'SoknadDataCtrl',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/fortsettsenere/:soknadId', {
                templateUrl: '../html/templates/fortsettSenere.html',
                controller: 'SoknadDataCtrl',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })

            .when('/reell-arbeidssoker/:soknadId', {
                templateUrl: '../html/templates/reell-arbeidssoker.html',
                controller: 'SoknadDataCtrl',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/egennaering/:soknadId', {
                templateUrl: '../html/templates/egen-naering.html',
                controller: 'SoknadDataCtrl',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/verneplikt/:soknadId', {
                templateUrl: '../html/templates/verneplikt.html',
                controller: 'SoknadDataCtrl',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/personalia/:soknadId', {
                templateUrl: '../html/templates/personalia.html', 
                controller: 'SoknadDataCtrl',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
             })
            .when('/arbeidsforhold/:soknadId', {
                templateUrl: '../html/templates/arbeidsforhold.html',
                controller: 'SoknadDataCtrl',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/ytelser/:soknadId', {
                templateUrl: '../html/templates/ytelser.html',
                controller: 'SoknadDataCtrl',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/oppsummering/:soknadId', {
                templateUrl: '../html/templates/oppsummering.html',
                controller: 'SoknadDataCtrl',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
             })

            .when('/utdanning/:soknadId', {
                templateUrl: '../html/templates/utdanning.html',
                controller: 'SoknadDataCtrl',
                resolve: {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
            .when('/kvittering', {
                templateUrl: '../html/templates/kvittering-innsendt.html',
                resolve: {
                    notUsedButRequiredProperty: function (TekstService) {
                        return TekstService;
                    }
                }
            })

            .when('/soknadliste', {templateUrl: '../html/templates/soknadliste.html'})
            .when('/avbryt/:soknadId', {templateUrl: '../html/templates/avbryt.html', controller: 'AvbrytCtrl'})
            .when('/slettet', {templateUrl: '../html/templates/soknadSlettet.html', controller: 'SlettetSoknadDataCtrl'})

            .otherwise({redirectTo: '/informasjonsside'});

//    $locationProvider.html5Mode(true);
    });

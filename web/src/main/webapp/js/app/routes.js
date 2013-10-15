angular.module('app.routes', ['ngRoute'])

    .config(function ($routeProvider, $locationProvider) {
        $routeProvider
            .when('/utslagskriterier', {
                templateUrl: '../html/templates/utslagskriterier.html',
                controller: 'GrunnlagsdataCtrl',
                resolve: {
                    notUsedButRequiredProperty: function (TekstService) {
                        return TekstService;
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
            .when('/soknadliste', {templateUrl: '../html/templates/soknadliste.html'})
            .when('/arbeidsforhold/:soknadId', {templateUrl: '../html/templates/arbeidsforhold.html', controller: 'SoknadDataCtrl'})
            .when('/informasjonsside', {templateUrl: '../html/templates/informasjonsside.html', controller: 'StartSoknadCtrl'})
            .when('/avbryt/:soknadId', {templateUrl: '../html/templates/avbryt.html', controller: 'AvbrytCtrl'})
            .when('/fortsettsenere', {templateUrl: '../html/templates/fortsettSenere.html', controller: 'SoknadDataCtrl'})
            .when('/slettet', {templateUrl: '../html/templates/soknadSlettet.html', controller: 'SlettetSoknadDataCtrl'})
            .when('/kvittering', {templateUrl: '../html/templates/kvittering-innsendt.html'})

            .when('/oppsummering/:soknadId', {templateUrl: '../html/templates/oppsummering.html', controller: 'SoknadDataCtrl'})

            .when('/utdanning/:soknadId', {templateUrl: '../html/templates/utdanning.html', controller: 'SoknadDataCtrl'})
            .otherwise({redirectTo: '/utslagskriterier'});

//    $locationProvider.html5Mode(true);
    });

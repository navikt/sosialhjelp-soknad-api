angular.module('app.routes', ['ngRoute'])

	.config(function ($routeProvider, $locationProvider) {
		$routeProvider
			.when('/informasjonsside', {
				templateUrl: '../html/templates/informasjonsside.html',
				controller : 'InformasjonsSideCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (InformasjonsSideResolver) {
						return InformasjonsSideResolver;
					}
				}
			})
			.when('/behandling/:behandlingId', {
				templateUrl: '../html/templates/informasjonsside.html',
				controller: 'BehandlingCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (BehandlingSideResolver) {
						return BehandlingSideResolver;
					}
				}
			})
			.when('/vedlegg', {
				templateUrl: '../html/templates/vedlegg.html',
				controller : 'SoknadDataCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/opplasting/:vedleggId', {
				templateUrl: '../html/templates/opplasting.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/visVedlegg/:vedleggId', {
				templateUrl: '../html/templates/visvedlegg.html',
				controller : 'SoknadDataCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/soknad', {
				templateUrl: '../html/dagpenger-singlepage.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/fortsettsenere', {
				templateUrl: '../html/templates/fortsettSenere.html',
				resolve    : {
					notUsedButRequiredProperty: function (EpostResolver) {
						return EpostResolver;
					}
				}
			})
			.when('/kvittering-fortsettsenere', {
				templateUrl: '../html/templates/kvittering-fortsettsenere.html',
				controller : 'SoknadDataCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/reell-arbeidssoker', {
				templateUrl: '../html/templates/reell-arbeidssoker.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/egennaering', {
				templateUrl: '../html/templates/egen-naering.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/verneplikt', {
				templateUrl: '../html/templates/verneplikt.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/personalia', {
				templateUrl: '../html/templates/personalia.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/barnetillegg', {
				templateUrl: '../html/templates/barnetillegg.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/nyttbarn', {
				templateUrl: '../html/templates/barnetillegg-nyttbarn.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/endrebarn/:faktumId', {
				templateUrl: '../html/templates/barnetillegg-nyttbarn.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/sokbarnetillegg/:faktumId', {
				templateUrl: '../html/templates/barnetillegg-nyttbarn.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/arbeidsforhold', {
				templateUrl: '../html/templates/arbeidsforhold.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/nyttarbeidsforhold', {
				templateUrl: '../html/templates/arbeidsforhold-nytt.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/endrearbeidsforhold/:faktumId', {
				templateUrl: '../html/templates/arbeidsforhold-nytt.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/ytelser', {
				templateUrl: '../html/templates/ytelser.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/oppsummering', {
				templateUrl: '../html/templates/oppsummering.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
            .when('/bekreftelse', {
                templateUrl: '../html/templates/bekreftelse.html',
                resolve    : {
                    notUsedButRequiredProperty: function (HentSoknadService) {
                        return HentSoknadService;
                    }
                }
            })
			.when('/utdanning', {
				templateUrl: '../html/templates/utdanning.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/kvittering', {
				templateUrl: '../html/templates/kvittering-innsendt.html',
				resolve    : {
					notUsedButRequiredProperty: function (TekstService) {
						return TekstService;
					}
				}
			})
			.when('/avbryt', {
				templateUrl: '../html/templates/avbryt.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/slettet', {
				templateUrl: '../html/templates/soknadSlettet.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/soknadliste', {templateUrl: '../html/templates/soknadliste.html'})
			.otherwise({redirectTo: '/informasjonsside'});

//    $locationProvider.html5Mode(true);
	}).run(function ($rootScope, $location, $anchorScroll, $routeParams) {
		$rootScope.$on('$routeChangeSuccess', function (newRoute, oldRoute) {
			$location.hash($routeParams.scrollTo);
			$anchorScroll();
		});
	});

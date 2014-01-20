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
			.when('/:behandlingId', {
				templateUrl: '../html/templates/informasjonsside.html',
				controller: 'BehandlingCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (BehandlingSideResolver) {
						return BehandlingSideResolver;
					}
				}
			})
			.when('/vedlegg/:soknadId', {
				templateUrl: '../html/templates/vedlegg.html',
				controller : 'SoknadDataCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/opplasting/:soknadId/:faktumId/:gosysId', {
				templateUrl: '../html/templates/opplasting.html',
				controller : 'SoknadDataCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/visVedlegg/:soknadId/:faktumId/:vedleggId', {
				templateUrl: '../html/templates/visvedlegg.html',
				controller : 'SoknadDataCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/dagpenger/:soknadId', {
				templateUrl: '../html/dagpenger-singlepage.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/fortsettsenere/:soknadId', {
				templateUrl: '../html/templates/fortsettSenere.html',
				resolve    : {
					notUsedButRequiredProperty: function (EpostResolver) {
						return EpostResolver;
					}
				}
			})
			.when('/kvittering-fortsettsenere/:soknadId', {
				templateUrl: '../html/templates/kvittering-fortsettsenere.html',
				controller : 'SoknadDataCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/gjenoppta/:soknadId', {
				templateUrl: '../html/templates/gjenoppta.html',
				controller : 'SoknadDataCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/reell-arbeidssoker/:soknadId', {
				templateUrl: '../html/templates/reell-arbeidssoker.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/egennaering/:soknadId', {
				templateUrl: '../html/templates/egen-naering.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/verneplikt/:soknadId', {
				templateUrl: '../html/templates/verneplikt.html',
				controller : 'SoknadDataCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/personalia/:soknadId', {
				templateUrl: '../html/templates/personalia.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/barnetillegg/:soknadId', {
				templateUrl: '../html/templates/barnetillegg.html',
				controller : 'SoknadDataCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/nyttbarn/:soknadId', {
				templateUrl: '../html/templates/barnetillegg-nyttbarn.html',
				controller : 'SoknadDataCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (NyttBarnSideResolver) {
						return NyttBarnSideResolver;
					}
				}
			})
			.when('/endrebarn/:soknadId/:faktumId', {
				templateUrl: '../html/templates/barnetillegg-nyttbarn.html',
				controller : 'SoknadDataCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (NyttBarnSideResolver) {
						return NyttBarnSideResolver;
					}
				}
			})
			.when('/sokbarnetillegg/:soknadId/:faktumId', {
				templateUrl: '../html/templates/barnetillegg-nyttbarn.html',
				controller : 'SoknadDataCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (NyttBarnSideResolver) {
						return NyttBarnSideResolver;
					}
				}
			})
			.when('/arbeidsforhold/:soknadId', {
				templateUrl: '../html/templates/arbeidsforhold.html',
				controller : 'SoknadDataCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/nyttarbeidsforhold/:soknadId', {
				templateUrl: '../html/templates/arbeidsforhold-nytt.html',
				controller : 'SoknadDataCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/endrearbeidsforhold/:soknadId/:faktumId', {
				templateUrl: '../html/templates/arbeidsforhold-nytt.html',
				controller : 'SoknadDataCtrl',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/ytelser/:soknadId', {
				templateUrl: '../html/templates/ytelser.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/oppsummering/:soknadId', {
				templateUrl: '../html/templates/oppsummering.html',
				resolve    : {
					notUsedButRequiredProperty: function (HentSoknadService) {
						return HentSoknadService;
					}
				}
			})
			.when('/utdanning/:soknadId', {
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
			.when('/avbryt/:soknadId', {
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

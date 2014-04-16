angular.module('sendsoknad.routes', ['ngRoute', 'nav.common.routes'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when('/cmstekster', {
                redirectTo: '/informasjonsside',
                resolve: {
                    settCmsTekster: ['$rootScope', function ($rootScope) {
                        $rootScope.visCmsnokkler = true;
                        return true;
                    }]
                }
            })
            .when('/informasjonsside', {
                templateUrl: '../views/templates/informasjonsside.html',
                controller: 'InformasjonsSideCtrl',
                resolve: {
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }],
                    config: ['ConfigResolver', function (ConfigResolver) {
                        return ConfigResolver;
                    }],
                    utslagskriterier: ['UtslagskriterierResolver', function(UtslagskriterierResolver) {
                        return UtslagskriterierResolver;
                    }],
                    soknadMetadata: ['SoknadMetadataResolver', function(SoknadMetadataResolver) {
                        return SoknadMetadataResolver;
                    }]
                }
            })
            .when('/behandling/:behandlingId', {
                templateUrl: '../views/templates/informasjonsside.html',
                controller: 'BehandlingCtrl',
                resolve: {
                    behandlingSide: ['BehandlingIdResolver', function (BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    }]
                }
            })
            .when('/vedlegg', {
                templateUrl: '../views/templates/vedlegg/vedlegg.html',
                resolve: {
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }],
                    land: ['LandResolver', function (LandResolver) {
                        return LandResolver;
                    }],
                    soknad: ['SoknadResolver', function(SoknadResolver) {
                        return SoknadResolver;
                    }],
                    fakta: ['FaktaResolver', function(FaktaResolver) {
                        return FaktaResolver;
                    }],
                    soknadOppsett: ['SoknadOppsettResolver', function(SoknadOppsettResolver) {
                        return SoknadOppsettResolver;
                    }],
                    config: ['ConfigForSoknadResolver', function(ConfigForSoknadResolver) {
                        return ConfigForSoknadResolver;
                    }],
                    behandlingsId: ['BehandlingIdResolver', function(BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    }]
                }
            })
            .when('/opplasting/:vedleggId', {
                templateUrl: '../views/templates/vedlegg/opplasting.html',
                controller: 'OpplastingVedleggCtrl',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    land: function (LandResolver) {
                        return LandResolver;
                    },
                    soknad: function(SoknadResolver) {
                        return SoknadResolver;
                    },
                    fakta: function(FaktaResolver) {
                        return FaktaResolver;
                    },
                    soknadOppsett: function(SoknadOppsettResolver) {
                        return SoknadOppsettResolver;
                    },
                    config: function(ConfigForSoknadResolver) {
                        return ConfigForSoknadResolver;
                    },
                    behandlingsId: function(BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    },
                    vedleggListe: function (VedleggResolver) {
                        return VedleggResolver;
                    }
                }
            })
            .when('/visVedlegg/:vedleggId', {
                templateUrl: '../views/templates/vedlegg/visvedlegg.html',
                resolve: {
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }],
                    land: ['LandResolver', function (LandResolver) {
                        return LandResolver;
                    }],
                    soknad: ['SoknadResolver', function(SoknadResolver) {
                        return SoknadResolver;
                    }],
                    fakta: ['FaktaResolver', function(FaktaResolver) {
                        return FaktaResolver;
                    }],
                    soknadOppsett: ['SoknadOppsettResolver', function(SoknadOppsettResolver) {
                        return SoknadOppsettResolver;
                    }],
                    config: ['ConfigForSoknadResolver', function(ConfigForSoknadResolver) {
                        return ConfigForSoknadResolver;
                    }],
                    behandlingsId: ['BehandlingIdResolver', function(BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    }]
                }
            })
            .when('/soknad', {
                templateUrl: '../views/dagpenger/dagpenger-skjema.html',
                resolve: {
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }],
                    land: ['LandResolver', function (LandResolver) {
                        return LandResolver;
                    }],
                    soknad: ['SoknadResolver', function(SoknadResolver) {
                        return SoknadResolver;
                    }],
                    fakta: ['FaktaResolver', function(FaktaResolver) {
                        return FaktaResolver;
                    }],
                    soknadOppsett: ['SoknadOppsettResolver', function(SoknadOppsettResolver) {
                        return SoknadOppsettResolver;
                    }],
                    config: ['ConfigForSoknadResolver', function(ConfigForSoknadResolver) {
                        return ConfigForSoknadResolver;
                    }],
                    behandlingsId: ['BehandlingIdResolver', function(BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    }]
                }
            })
            .when('/fortsettsenere', {
                templateUrl: '../views/templates/fortsettSenere.html',
                resolve: {
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }],
                    land: ['LandResolver', function (LandResolver) {
                        return LandResolver;
                    }],
                    soknad: ['SoknadResolver', function(SoknadResolver) {
                        return SoknadResolver;
                    }],
                    fakta: ['FaktaResolver', function(FaktaResolver) {
                        return FaktaResolver;
                    }],
                    soknadOppsett: ['SoknadOppsettResolver', function(SoknadOppsettResolver) {
                        return SoknadOppsettResolver;
                    }],
                    config: ['ConfigForSoknadResolver', function(ConfigForSoknadResolver) {
                        return ConfigForSoknadResolver;
                    }],
                    behandlingsId: ['BehandlingIdResolver', function(BehandlingIdResolver) {
                        return BehandlingIdResolver;
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
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }],
                    land: ['LandResolver', function (LandResolver) {
                        return LandResolver;
                    }],
                    soknad: ['SoknadResolver', function(SoknadResolver) {
                        return SoknadResolver;
                    }],
                    fakta: ['FaktaResolver', function(FaktaResolver) {
                        return FaktaResolver;
                    }],
                    soknadOppsett: ['SoknadOppsettResolver', function(SoknadOppsettResolver) {
                        return SoknadOppsettResolver;
                    }],
                    config: ['ConfigForSoknadResolver', function(ConfigForSoknadResolver) {
                        return ConfigForSoknadResolver;
                    }],
                    behandlingsId: ['BehandlingIdResolver', function(BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    }]
                }
            })
            .when('/endrebarn/:faktumId', {
                templateUrl: '../views/templates/barnetillegg/barnetillegg-nyttbarn.html',
                resolve: {
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }],
                    land: ['LandResolver', function (LandResolver) {
                        return LandResolver;
                    }],
                    soknad: ['SoknadResolver', function(SoknadResolver) {
                        return SoknadResolver;
                    }],
                    fakta: ['FaktaResolver', function(FaktaResolver) {
                        return FaktaResolver;
                    }],
                    soknadOppsett: ['SoknadOppsettResolver', function(SoknadOppsettResolver) {
                        return SoknadOppsettResolver;
                    }],
                    config: ['ConfigForSoknadResolver', function(ConfigForSoknadResolver) {
                        return ConfigForSoknadResolver;
                    }],
                    behandlingsId: ['BehandlingIdResolver', function(BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    }]
                }
            })
            .when('/sokbarnetillegg/:faktumId', {
                templateUrl: '../views/templates/barnetillegg/endreSystembarnTemplate.html',
                resolve: {
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }],
                    land: ['LandResolver', function (LandResolver) {
                        return LandResolver;
                    }],
                    soknad: ['SoknadResolver', function(SoknadResolver) {
                        return SoknadResolver;
                    }],
                    fakta: ['FaktaResolver', function(FaktaResolver) {
                        return FaktaResolver;
                    }],
                    soknadOppsett: ['SoknadOppsettResolver', function(SoknadOppsettResolver) {
                        return SoknadOppsettResolver;
                    }],
                    config: ['ConfigForSoknadResolver', function(ConfigForSoknadResolver) {
                        return ConfigForSoknadResolver;
                    }],
                    behandlingsId: ['BehandlingIdResolver', function(BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    }]
                }
            })
            .when('/nyttarbeidsforhold', {
                templateUrl: '../views/templates/arbeidsforhold-nytt.html',
                resolve: {
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }],
                    land: ['LandResolver', function (LandResolver) {
                        return LandResolver;
                    }],
                    soknad: ['SoknadResolver', function(SoknadResolver) {
                        return SoknadResolver;
                    }],
                    fakta: ['FaktaResolver', function(FaktaResolver) {
                        return FaktaResolver;
                    }],
                    soknadOppsett: ['SoknadOppsettResolver', function(SoknadOppsettResolver) {
                        return SoknadOppsettResolver;
                    }],
                    config: ['ConfigForSoknadResolver', function(ConfigForSoknadResolver) {
                        return ConfigForSoknadResolver;
                    }],
                    behandlingsId: ['BehandlingIdResolver', function(BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    }]
                }
            })
            .when('/endrearbeidsforhold/:faktumId', {
                templateUrl: '../views/templates/arbeidsforhold-nytt.html',
                resolve: {
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }],
                    land: ['LandResolver', function (LandResolver) {
                        return LandResolver;
                    }],
                    soknad: ['SoknadResolver', function(SoknadResolver) {
                        return SoknadResolver;
                    }],
                    fakta: ['FaktaResolver', function(FaktaResolver) {
                        return FaktaResolver;
                    }],
                    soknadOppsett: ['SoknadOppsettResolver', function(SoknadOppsettResolver) {
                        return SoknadOppsettResolver;
                    }],
                    config: ['ConfigForSoknadResolver', function(ConfigForSoknadResolver) {
                        return ConfigForSoknadResolver;
                    }],
                    behandlingsId: ['BehandlingIdResolver', function(BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    }]
                }
            })
            .when('/oppsummering', {
                templateUrl: '../views/templates/oppsummering.html',
                resolve: {
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }],
                    land: ['LandResolver', function (LandResolver) {
                        return LandResolver;
                    }],
                    soknad: ['SoknadResolver', function(SoknadResolver) {
                        return SoknadResolver;
                    }],
                    fakta: ['FaktaResolver', function(FaktaResolver) {
                        return FaktaResolver;
                    }],
                    soknadOppsett: ['SoknadOppsettResolver', function(SoknadOppsettResolver) {
                        return SoknadOppsettResolver;
                    }],
                    config: ['ConfigForSoknadResolver', function(ConfigForSoknadResolver) {
                        return ConfigForSoknadResolver;
                    }],
                    behandlingsId: ['BehandlingIdResolver', function(BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    }]
                }
            })
            .when('/avbryt', {
                templateUrl: '../views/templates/avbryt.html',
                resolve: {
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }],
                    land: ['LandResolver', function (LandResolver) {
                        return LandResolver;
                    }],
                    soknad: ['SoknadResolver', function(SoknadResolver) {
                        return SoknadResolver;
                    }],
                    fakta: ['FaktaResolver', function(FaktaResolver) {
                        return FaktaResolver;
                    }],
                    soknadOppsett: ['SoknadOppsettResolver', function(SoknadOppsettResolver) {
                        return SoknadOppsettResolver;
                    }],
                    config: ['ConfigForSoknadResolver', function(ConfigForSoknadResolver) {
                        return ConfigForSoknadResolver;
                    }],
                    behandlingsId: ['BehandlingIdResolver', function(BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    }]
                }
            })
            .when('/ferdigstilt', {
                templateUrl: '../views/templates/ferdigstilt.html',
                resolve: {
                    cms: ['CmsResolver', function (CmsResolver) {
                        return CmsResolver;
                    }],
                    land: ['LandResolver', function (LandResolver) {
                        return LandResolver;
                    }],
                    soknad: ['SoknadResolver', function(SoknadResolver) {
                        return SoknadResolver;
                    }],
                    fakta: ['FaktaResolver', function(FaktaResolver) {
                        return FaktaResolver;
                    }],
                    soknadOppsett: ['SoknadOppsettResolver', function(SoknadOppsettResolver) {
                        return SoknadOppsettResolver;
                    }],
                    config: ['ConfigForSoknadResolver', function(ConfigForSoknadResolver) {
                        return ConfigForSoknadResolver;
                    }],
                    behandlingsId: ['BehandlingIdResolver', function(BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    }]
                }
            })
            .when('/', {
                redirectTo: '/informasjonsside'
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

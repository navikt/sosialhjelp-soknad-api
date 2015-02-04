angular.module('gjenopptak.routes', ['ngRoute', 'nav.common.routes'])
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
            .when('/informasjonsside', {
                templateUrl: '../views/templates/informasjonsside.html',
                controller: 'InformasjonsSideCtrl',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    config: function (ConfigResolver) {
                        return ConfigResolver;
                    }
                }
            })
            .when('/soknad/', {
                templateUrl: '../views/dagpenger/dagpenger-skjema.html',
                controller: 'GjenopptakCtrl',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    land: function (LandResolver) {
                        return LandResolver;
                    },
                    eosLand: function (EosLandResolver) {
                        return EosLandResolver;
                    },
                    soknad: function (SoknadResolver) {
                        return SoknadResolver;
                    },
                    soknadOppsett: function (SoknadOppsettResolver) {
                        return SoknadOppsettResolver;
                    },
                    config: function (ConfigForSoknadResolver) {
                        return ConfigForSoknadResolver;
                    },
                    behandlingsId: function (BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    },
                    fakta: function (FaktaResolver) {
                        return FaktaResolver;
                    }
                }
            })
            .when('/vedlegg', {
                templateUrl: '../views/templates/vedlegg/vedlegg.html',
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
                templateUrl: '../views/common/vedlegg/visvedlegg.html',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
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
                    }
                }
            })
            .when('/', {
                redirectTo: '/informasjonsside'
            })
            .when('/fortsettsenere', {
                templateUrl: '../views/templates/fortsettSenere.html',
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
                    }
                }
            })
            .when('/nyttarbeidsforhold', {
                templateUrl: '../views/templates/arbeidsforhold/arbeidsforhold-nytt.html',
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
                    }
                }
            })
            .when('/endrearbeidsforhold/:faktumId', {
                templateUrl: '../views/templates/arbeidsforhold/arbeidsforhold-nytt.html',
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
                    }
                }
            })
            .when('/kvittering-fortsettsenere', {
                templateUrl: '../views/templates/kvittering-fortsettsenere.html',
                controller: 'FortsettSenereKvitteringCtrl',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    soknad: function(SoknadResolver) {
                        return SoknadResolver;
                    },
                    fakta: function(FaktaResolver) {
                        return FaktaResolver;
                    },
                    config: function(ConfigForSoknadResolver) {
                        return ConfigForSoknadResolver;
                    },
                    behandlingsId: function(BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    }
                }
            })
            .when('/nyttbarn', {
                templateUrl: '../views/templates/barnetillegg/barnetillegg-nyttbarn.html',
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
                    }
                }
            })
            .when('/endrebarn/:faktumId', {
                templateUrl: '../views/templates/barnetillegg/barnetillegg-nyttbarn.html',
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
                    }
                }
            })
            .when('/sokbarnetillegg/:faktumId', {
                templateUrl: '../views/templates/barnetillegg/endreSystembarnTemplate.html',
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
                    }
                }
            })
            .when('/oppsummering', {
                templateUrl: '../views/templates/oppsummering.html',
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
                    }
                }
            })
            .when('/avbryt', {
                templateUrl: '../views/templates/avbryt.html',
                controller: 'AvbrytCtrl',
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
                    }
                }
            })
            .when('/fortsett',{
                templateUrl: '../views/templates/fortsettRouting.html',
                resolve: {
                    config: function(ConfigResolver) {
                        return ConfigResolver;
                    },
                    behandlingsId: function(BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    },
                    soknad: function(SoknadResolver) {
                        return SoknadResolver;
                    }
                }
            })
            .when('/ferdigstilt', {
                templateUrl: '../views/templates/ferdigstilt.html',
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
                    }
                }
            });
    }]);
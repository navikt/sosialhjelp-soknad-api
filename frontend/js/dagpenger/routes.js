angular.module('sendsoknad.routes', ['ngRoute', 'nav.common.routes'])

    .config(function ($routeProvider) {
        $routeProvider
            .when('/cmstekster', {
                redirectTo: '/informasjonsside',
                resolve: {
                    settCmsTekster: function ($rootScope) {
                        $rootScope.visCmsnokkler = true;
                        return true;
                    }
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
            .when('/behandling/:behandlingId', {
                templateUrl: '../views/templates/informasjonsside.html',
                controller: 'BehandlingCtrl',
                resolve: {
                    behandlingSide: function (BehandlingIdResolver) {
                        return BehandlingIdResolver;
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
            .when('/soknad/:behandlingId', {
                templateUrl: '../views/dagpenger/dagpenger-skjema.html',
                controller: 'DagpengerCtrl',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    land: function (LandResolver) {
                        return LandResolver;
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
            .when('/nyttarbeidsforhold', {
                templateUrl: '../views/templates/arbeidsforhold-nytt.html',
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
                templateUrl: '../views/templates/arbeidsforhold-nytt.html',
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
            })
            .when('/', {
                redirectTo: '/informasjonsside'
            });

    });

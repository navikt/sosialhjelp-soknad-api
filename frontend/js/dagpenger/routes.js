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
            .when('/:behandlingId/behandling', {
                templateUrl: '../views/templates/informasjonsside.html',
                controller: 'BehandlingCtrl',
                resolve: {
                    behandlingSide: function (BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    }
                }
            })
            .when('/:behandlingId/vedlegg', {
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
            .when('/:behandlingId/opplasting/:vedleggId', {
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
            .when('/:behandlingId/visVedlegg/:vedleggId', {
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
            .when('/:behandlingId/soknad', {
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
            .when('/:behandlingId/fortsettsenere', {
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
            .when('/:behandlingId/kvittering-fortsettsenere', {
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
            .when('/:behandlingId/nyttbarn', {
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
            .when('/:behandlingId/endrebarn/:faktumId', {
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
            .when('/:behandlingId/sokbarnetillegg/:faktumId', {
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
            .when('/:behandlingId/nyttarbeidsforhold', {
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
            .when('/:behandlingId/endrearbeidsforhold/:faktumId', {
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
            .when('/:behandlingId/nypermitteringsperiode', {
                templateUrl: '../views/templates/arbeidsforhold/permitteringsperiode-nytt.html',
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
            .when('/:behandlingId/oppsummering', {
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
            .when('/:behandlingId/avbryt', {
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
            .when('/:behandlingId/ferdigstilt', {
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
            .when('/:behandlingId/fortsett',{
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
            .when('/', {
                redirectTo: '/informasjonsside'
            });
    });

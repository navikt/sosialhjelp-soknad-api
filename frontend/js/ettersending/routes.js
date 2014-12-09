angular.module('ettersending.routes', ['ngRoute', 'nav.common.routes'])

    .config(function ($routeProvider) {
        $routeProvider
            .when('/cmstekster', {
                redirectTo: '/',
                resolve: {
                    notUsedButRequiredProperty: function ($rootScope) {
                        $rootScope.visCmsnokkler = true;
                        return true;
                    }
                }
            })
            .when('/', {
                templateUrl: '../views/ettersending/startEttersending.html',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    config: function (ConfigResolver) {
                        return ConfigResolver;
                    },
                    metadata: function(EttersendingMetadataResolver) {
                        return EttersendingMetadataResolver;
                    }
                }
            })
            .when('/vedlegg', {
                templateUrl: '../views/ettersending/ettersending.html',
                controller: 'EttersendingCtrl',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    behandlingskjedeId: function (BehandlingIdResolver) {
                        return BehandlingIdResolver;
                    },
                    ettersending: function (EttersendingResolver) {
                        return EttersendingResolver;
                    },
                    vedlegg: function (VedleggResolver) {
                        return VedleggResolver;
                    },
                    config: function(ConfigResolver) {
                        return ConfigResolver;
                    },
                    personalia: function(EttersendingPersonaliaResolver) {
                        return EttersendingPersonaliaResolver;
                    },
                    soknadOppsett: function(SoknadOppsettResolver) {
                        return SoknadOppsettResolver;
                    }
                }
            })
            .when('/vedlegg/nytt', {
                templateUrl: '../views/ettersending/nytt-vedlegg.html',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    ettersending: function (EttersendingResolver) {
                        return EttersendingResolver;
                    },
                    vedlegg: function (VedleggResolver) {
                        return VedleggResolver;
                    },
                    config: function (ConfigResolver) {
                        return ConfigResolver;
                    }
                }
            })
            .when('/opplasting/:vedleggId', {
                templateUrl: '../views/ettersending/opplastingEttersending.html',
                controller: 'OpplastingVedleggCtrl',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    ettersending: function (EttersendingResolver) {
                        return EttersendingResolver;
                    },
                    vedleggListe: function (VedleggResolver) {
                        return VedleggResolver;
                    },
                    config: function (ConfigResolver) {
                        return ConfigResolver;
                    }
                }
            })
            .when('/avbryt', {
                templateUrl: '../views/ettersending/avbryt.html',
                controller: 'EttersendingAvbrytCtrl',
                resolve: {
                    cms: function (CmsResolver) {
                        return CmsResolver;
                    },
                    ettersending: function (EttersendingResolver) {
                        return EttersendingResolver;
                    },
                    config: function (ConfigResolver) {
                        return ConfigResolver;
                    },
                    vedlegg: function (VedleggResolver) {
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
                    ettersending: function (EttersendingResolver) {
                        return EttersendingResolver;
                    },
                    config: function (ConfigResolver) {
                        return ConfigResolver;
                    }
                }
            });
    });

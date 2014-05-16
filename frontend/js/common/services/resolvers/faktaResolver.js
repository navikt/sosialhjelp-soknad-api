angular.module('nav.services.resolvers.fakta', [])
    .factory('FaktaResolver', ['$rootScope', 'data', 'cms', '$resource', '$q', 'soknadService', 'landService', 'Faktum', '$http', 'SoknadResolver', function ($rootScope, data, cms, $resource, $q, soknadService, landService, Faktum, $http, SoknadResolver) {
        var faktaDefer = $q.defer();

        SoknadResolver
            .then(function() {
                Faktum.query({soknadId: data.soknad.soknadId}, function (result) {
                    data.fakta = result;
                    faktaDefer.resolve();

                    data.finnFaktum = function (key) {
                        var res = null;
                        data.fakta.forEach(function (item) {
                            if (item.key === key) {
                                res = item;
                            }
                        });
                        return res;
                    };

                    data.finnFakta = function (key) {
                        var res = [];
                        data.fakta.forEach(function (item) {
                            if (item.key === key) {
                                res.push(item);
                            }
                        });
                        return res;
                    };

                    data.slettFaktum = function(faktumData) {
                        $rootScope.faktumSomSkalSlettes = new Faktum(faktumData);
                        $rootScope.faktumSomSkalSlettes.$delete({soknadId: faktumData.soknadId}).then(function () {
                        });

                        data.fakta.forEach(function (item, index) {
                            if (item.faktumId === faktumData.faktumId) {
                                data.fakta.splice(index,1);
                            }
                        });
                    };

                    data.leggTilFaktum = function(faktum) {
                        data.fakta.push(faktum);
                    };
                });
            })
            .catch(function() {
                // TODO: Håndtere dersom man ikke kunne hente søknadsid
                faktaDefer.reject("Kunne ikke hente søknadsId for behandlingsId");
            });

        return faktaDefer.promise;
    }]);
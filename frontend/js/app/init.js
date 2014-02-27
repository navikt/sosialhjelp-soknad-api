/* jshint scripturl: true */

angular.module('sendsoknad')
    .value('data', {})
    .value('cms', {})
    .constant('validertKlasse', 'validert')
    .run(['$http', '$templateCache', '$rootScope', 'data', '$location', 'sjekkUtslagskriterier', function ($http, $templateCache, $rootScope, data, $location, sjekkUtslagskriterier) {
        $rootScope.app = {
            laster: true
        };
        $('#hoykontrast a, .skriftstorrelse a').attr('href', 'javascript:void(0)');

        $rootScope.$on('$routeChangeSuccess', function(event, next, current) {
            redirectDersomSoknadErFerdig();
            if (next.$$route) {
                /*
                 * Dersom vi kommer inn på informasjonsside utenfra (current sin redirectTo er informasjonsside), og krav for søknaden er oppfylt, skal vi redirecte til rett side.
                 */
                if (next.$$route.originalPath === "/informasjonsside" && sjekkUtslagskriterier.erOppfylt() && (!current || current.redirectTo === '/informasjonsside') && data.soknad) {

                    if (data.soknad.delstegStatus === "SKJEMA_VALIDERT") {
                        $location.path('/vedlegg');
                    } else if (data.soknad.delstegStatus === "VEDLEGG_VALIDERT") {
                        $location.path('/oppsummering');
                    } else {
                        $location.path('/soknad');
                    }
                } else if (next.$$route.originalPath === "/oppsummering") {
                    redirectTilVedleggsideDersomVedleggIkkeErValidert();
                    redirectTilSkjemasideDersomSkjemaIkkeErValidert();
                } else if (next.$$route.originalPath === "/vedlegg") {
                    redirectTilSkjemasideDersomSkjemaIkkeErValidert();
                } else if (current && next.$$route.originalPath === "/fortsettsenere") {
                    $rootScope.forrigeSide = current.$$route.originalPath;
                }
            }
        });

        function harHentetData() {
            return data && data.soknad;
        }

        function redirectDersomSoknadErFerdig() {
            if (harHentetData() && data.soknad.status === "FERDIG") {
                $location.path('/ferdigstilt');
            }
        }

        function redirectTilSkjemasideDersomSkjemaIkkeErValidert() {
            if (harHentetData() && !skjemaErValidert()) {
                $location.path('/soknad');
            }
        }

        function redirectTilVedleggsideDersomVedleggIkkeErValidert() {
            if (harHentetData() && !vedleggErValidert()) {
                $location.path('/vedlegg');
            }
        }

        function skjemaErValidert() {
            return data.soknad.delstegStatus === "SKJEMA_VALIDERT" || vedleggErValidert();
        }

        function vedleggErValidert() {
            return data.soknad.delstegStatus === "VEDLEGG_VALIDERT";
        }
    }])
    .factory('InformasjonsSideResolver', ['$rootScope', 'data', 'cms', '$resource', '$q', '$timeout', function ($rootScope, data, cms, $resource, $q, $timeout) {
        var promiseArray = [];

        var tekster = $resource('/sendsoknad/rest/enonic/Dagpenger').get(
            function (result) { // Success
                cms.tekster = result;
            }
        );
        var config = $resource('/sendsoknad/rest/getConfig').get(
            function (result) {
                data.config = result;
            }
        );

        var utslagskriterier = $resource('/sendsoknad/rest/utslagskriterier/').get(
            function (result) {
                data.utslagskriterier = result;
            }
        );

        var behandlingId = getBehandlingIdFromUrl();
        if(erSoknadStartet()) {
            var soknadDeferer = $q.defer();
            var soknad = $resource('/sendsoknad/rest/soknad/behandling/:behandlingId').get(
                {behandlingId: behandlingId},
                function (result) { // Success
                    var soknadId = result.result;
                    
                    $resource('/sendsoknad/rest/soknad/metadata/:soknadId').get(
                        {soknadId: soknadId},
                        function (result) { // Success
                            data.soknad = result;
                            soknadDeferer.resolve();
                        }
                    );
                    
                }
            );
            promiseArray.push(soknadDeferer.promise);
        }

        var lasteindikatorDefer = $q.defer();

        // Passer på at laste-indikatoren vises i minimum 2 sekunder, for å unngå at den bare "blinker"
        $timeout(function() {
            lasteindikatorDefer.resolve();
        }, 2000);

        promiseArray.push(tekster.$promise);
        promiseArray.push(utslagskriterier.$promise);
        promiseArray.push(config.$promise);
        promiseArray.push(lasteindikatorDefer.promise);

        var resolve = $q.all(promiseArray)

        resolve.then(function() {
            $rootScope.app.laster = false;
        });

        return resolve;
    }])

    .factory('BehandlingSideResolver', ['$resource', '$q', '$route' , function ($resource, $q, $route) {
        var promiseArray = [];

        var behandlingId = $route.current.params.behandlingId;
        var soknadDeferer = $q.defer();
        var soknad = $resource('/sendsoknad/rest/soknad/behandling/:behandlingId').get(
            {behandlingId: behandlingId},
            function (result) { // Success
                $route.current.params.soknadId = result.result;
                soknadDeferer.resolve();
            }
        );
        promiseArray.push(soknadDeferer.promise);
        
        return $q.all(promiseArray);
    }])

    .factory('HentSoknadService', ['$rootScope', 'data', 'cms', '$resource', '$q', '$route', 'soknadService', 'landService', 'Faktum', '$http', '$timeout', function ($rootScope, data, cms, $resource, $q, $route, soknadService, landService, Faktum, $http, $timeout) {
        var promiseArray = [];
        
        var soknadOppsettDefer = $q.defer();
        var soknadDeferer = $q.defer();
        var faktaDefer = $q.defer();

        var brukerbehandlingsid = getBehandlingIdFromUrl();
        var soknad = $resource('/sendsoknad/rest/soknad/behandling/:behandlingId').get(
            {behandlingId: brukerbehandlingsid},
            function (result) { // Success
                var soknadId = result.result;

                $http.post('/sendsoknad/rest/soknad/personalia', soknadId).then(function() {
                    soknadService.get({soknadId: soknadId},
                        function (result) { // Success
                            data.soknad = result;
                            soknadDeferer.resolve();
                        }
                    );

                    Faktum.query({soknadId: soknadId}, function (result) {
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
                });

                soknadService.options({soknadId: soknadId},
                    function (result) { // Success
                        data.soknadOppsett = result;
                        soknadOppsettDefer.resolve();
                    }
                );
            }
        );
        
        var config = $resource('/sendsoknad/rest/getConfig').get(
            function (result) {
                data.config = result;
            }
        );

        var tekster = $resource('/sendsoknad/rest/enonic/Dagpenger').get(
            function (result) { // Success
                cms.tekster = result;
            }
        );

        var land = landService.get(
            function (result) { // Success
                data.land = result;
            }
        );

        var lasteindikatorDefer = $q.defer();

        // Passer på at laste-indikatoren vises i minimum 2 sekunder, for å unngå at den bare "blinker"
        $timeout(function() {
            lasteindikatorDefer.resolve();
        }, 2000);

        promiseArray.push(soknadOppsettDefer.promise, soknadDeferer.promise, faktaDefer.promise, land.$promise, tekster.$promise, config.$promise, lasteindikatorDefer.promise);

        var resolve = $q.all(promiseArray);

        resolve.then(function() {
            $rootScope.app.laster = false;
        });

        return resolve;
    }]);

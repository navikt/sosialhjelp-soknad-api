angular.module('sendsoknad')
    .run(['$http', '$templateCache', function ($http, $templateCache) {
        $http.get('../html/templates/reellarbeidssoker/reell-arbeidssoker.html', {cache: $templateCache});
        $http.get('../html/templates/egen-naering.html', {cache: $templateCache});
        $http.get('../html/templates/verneplikt.html', {cache: $templateCache});
        $http.get('../html/templates/personalia.html', {cache: $templateCache});
        $http.get('../html/templates/arbeidsforhold.html', {cache: $templateCache});
        $http.get('../html/templates/ytelser.html', {cache: $templateCache});
        $http.get('../html/templates/barnetillegg.html', {cache: $templateCache});
        $http.get('../html/templates/utdanning/utdanning.html', {cache: $templateCache});
        $http.get('../html/templates/utdanning/utdanningKveldTemplate.html', {cache: $templateCache});
        $http.get('../html/templates/utdanning/utdanningKortvarigTemplate.html', {cache: $templateCache});
        $http.get('../html/templates/utdanning/utdanningKortvarigFlereTemplate.html', {cache: $templateCache});
        $http.get('../html/templates/utdanning/utdanningNorskTemplate.html', {cache: $templateCache});
        $http.get('../html/templates/adresse.html', {cache: $templateCache});
        $http.get('../html/dagpenger-singlepage.html', {cache: $templateCache});
        $http.get('../js/common/directives/booleanradio/booleanradioTemplate.html', {cache: $templateCache});
        $http.get('../js/common/directives/accordion/accordionGroupTemplate.html', {cache: $templateCache});
        $http.get('../js/common/directives/accordion/accordionTemplate.html', {cache: $templateCache});
        $http.get('../js/common/directives/hjelpetekst/hjelpetekstTemplate.html', {cache: $templateCache});
        $http.get('../js/common/directives/datepicker/singleDatepickerTemplate.html', {cache: $templateCache});
        $http.get('../js/common/directives/datepicker/doubleDatepickerTemplate.html', {cache: $templateCache});
        $http.get('../js/common/directives/navinput/navbuttonspinnerTemplate.html', {cache: $templateCache});
        $http.get('../js/common/directives/navinput/navcheckboxTemplate.html', {cache: $templateCache});
        $http.get('../js/common/directives/navinput/navradioTemplate.html', {cache: $templateCache});
        $http.get('../js/common/directives/navinput/navtekstTemplate.html', {cache: $templateCache});
        $http.get('../js/common/directives/navtextarea/navtextareaObligatoriskTemplate.html', {cache: $templateCache});
        $http.get('../js/common/directives/navtextarea/navtextareaTemplate.html', {cache: $templateCache});
        $http.get('../js/common/directives/select/selectTemplate.html', {cache: $templateCache});
        $http.get('../js/app/directives/feilmeldinger/feilmeldingerTemplate.html', {cache: $templateCache});
        $http.get('../js/app/directives/feilmeldinger/stickyFeilmeldingTemplate.html', {cache: $templateCache});
        $http.get('../js/app/directives/dagpenger/arbeidsforholdformTemplate.html', {cache: $templateCache});
        $http.get('../js/app/directives/markup/navinfoboksTemplate.html', {cache: $templateCache});
        $http.get('../js/app/directives/markup/panelStandardBelystTemplate.html', {cache: $templateCache});
        $http.get('../js/app/directives/sporsmalferdig/spmblokkFerdigTemplate.html', {cache: $templateCache});
        $http.get('../js/app/directives/stegindikator/stegIndikatorTemplate.html', {cache: $templateCache});
        $http.get('../js/app/directives/stickybunn/stickyBunnTemplate.html', {cache: $templateCache});
    }])
    .value('data', {})
    .value('cms', {})
    .factory('InformasjonsSideResolver', ['data', 'cms', '$resource', '$q', '$route', function (data, cms, $resource, $q, $route) {
        var promiseArray = [];

        var tekster = $resource('/sendsoknad/rest/enonic/Dagpenger').get(
            function (result) { // Success
                cms.tekster = result;
            }
        );

        var utslagskriterier = $resource('/sendsoknad/rest/utslagskriterier/').get(
            function (result) {
                data.utslagskriterier = result;
            }
        );

        promiseArray.push(tekster.$promise);
        promiseArray.push(utslagskriterier.$promise);

        return $q.all(promiseArray);
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
        promiseArray.push(soknadDeferer.promise)
        
        return $q.all(promiseArray);
    }])

    .factory('HentSoknadService', ['$rootScope', 'data', 'cms', '$resource', '$q', '$route', 'soknadService', 'landService', 'Faktum', '$http', function ($scope, data, cms, $resource, $q, $route, soknadService, landService, Faktum, $http) {
        var soknadId = $route.current.params.soknadId;
        var promiseArray = [];

        var tekster = $resource('/sendsoknad/rest/enonic/Dagpenger').get(
            function (result) { // Success
                cms.tekster = result;
            }
        );
        promiseArray.push(tekster.$promise);

        var land = landService.get(
            function (result) { // Success
                data.land = result;
            }
        );
        promiseArray.push(land.$promise);

        if (soknadId != undefined) {
            // Barn må hentes før man henter søknadsdataene.
            var soknadDeferer = $q.defer();
            var faktaDeferer = $q.defer();

            var personaliaPromise = $http.post('/sendsoknad/rest/soknad/personalia', soknadId).then(function() {
                soknadService.get({param: soknadId},
                    function (result) { // Success
                        data.soknad = result;
                        soknadDeferer.resolve();
                    }
                );

                Faktum.query({soknadId: soknadId}, function (result) {
                    data.fakta = result;
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
                        $scope.faktumSomSkalSlettes = new Faktum(faktumData);
                        $scope.faktumSomSkalSlettes.$delete({soknadId: faktumData.soknadId}).then(function () {
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

                    faktaDeferer.resolve();
                });
            });
            promiseArray.push(personaliaPromise.$promise);

            var soknadOppsett = soknadService.options({param: soknadId},
                function (result) { // Success
                    data.soknadOppsett = result;
                }
            );
            promiseArray.push(soknadOppsett.$promise, soknadDeferer.promise, faktaDeferer.promise);
        }

        return $q.all(promiseArray);
    }])

    //Lagt til for å tvinge ny lasting av fakta fra server. Da er vi sikker på at e-post kommer med til fortsett-senere siden.
    .factory('EpostResolver', ['data', 'cms', '$resource', '$q', '$route', 'soknadService', function (data, cms, $resource, $q, $route, soknadService) {
        var soknadId = $route.current.params.soknadId;
        var promiseArray = [];

        var tekster = $resource('/sendsoknad/rest/enonic/Dagpenger').get(
            function (result) { // Success
                cms.tekster = result;
            }
        );
        promiseArray.push(tekster.$promise);

        var soknad = soknadService.get({param: soknadId},
            function (result) { // Success
                data.soknad = result;
            }
        );
        var soknadOppsett = soknadService.options({param: soknadId},
            function (result) { // Success
                data.soknadOppsett = result;
            });
        promiseArray.push(soknad.$promise, soknadOppsett.$promise);

        return $q.all(promiseArray);
    }]);

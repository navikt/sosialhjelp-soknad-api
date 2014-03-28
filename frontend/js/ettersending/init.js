/* jshint scripturl: true */

angular.module('ettersending')
    .value('data', {})
    .value('cms', {})
    .constant('validertKlasse', 'validert')
    .run(['$http', '$templateCache', '$rootScope', function ($http, $templateCache, $rootScope) {
        $rootScope.app = {
            laster: true
        };
        $('#hoykontrast a, .skriftstorrelse a').attr('href', 'javascript:void(0)');
    }])
    .factory('HentEttersendingsService', ['$location', '$rootScope', 'data', 'cms', '$resource', '$q', '$route', 'ettersendingService', 'landService', 'Faktum', '$http', '$timeout', function ($location, $rootScope, data, cms, $resource, $q, $route, ettersendingService, landService, Faktum, $http, $timeout) {
        var promiseArray = [];
        var soknadDeferer = $q.defer();

        var brukerbehandlingsid = getBehandlingIdFromUrl();
        ettersendingService.get({behandlingsId: brukerbehandlingsid},
            function (result) { // Success
                data.soknad = result;
                data.finnFaktum = function (key) {
                    var res = null;
                    data.soknad.faktaListe.forEach(function (item) {
                        if (item.key === key) {
                            res = item;
                        }
                    });
                    return res;
                };
                soknadDeferer.resolve();
            }
        );
        var tekster = $resource('/sendsoknad/rest/enonic/Dagpenger').get(
            function (result) { // Success
                cms.tekster = result;
            }
        );

        var lasteindikatorDefer = $q.defer();
        // Passer på at laste-indikatoren vises i minimum 2 sekunder, for å unngå at den bare "blinker"
        $timeout(function() {
            lasteindikatorDefer.resolve();
        }, 2000);
        promiseArray.push( tekster.$promise, soknadDeferer.promise, lasteindikatorDefer.promise);

        var resolve = $q.all(promiseArray);
        resolve.then(function() {
            $rootScope.app.laster = false;
        });
        return resolve;
    }])
    .factory('StartEttersendingService', ['$rootScope', 'cms', '$resource', '$q', '$timeout', function ($rootScope, cms, $resource, $q, $timeout) {
        var promiseArray = [];

        var tekster = $resource('/sendsoknad/rest/enonic/Dagpenger').get(
            function (result) { // Success
                cms.tekster = result;
            }
        );
        var lasteindikatorDefer = $q.defer();

        // Passer på at laste-indikatoren vises i minimum 2 sekunder, for å unngå at den bare "blinker"
        $timeout(function() {
            lasteindikatorDefer.resolve();
        }, 2000);

        promiseArray.push(tekster.$promise, lasteindikatorDefer.promise);

        var resolve = $q.all(promiseArray);

        resolve.then(function() {
            $rootScope.app.laster = false;
        });

        return resolve;
    }])
    .factory('FeilsideService', ['$rootScope', 'cms', '$resource', '$q', '$timeout', 'data', function ($rootScope, cms, $resource, $q, $timeout, data) {
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

        var lasteindikatorDefer = $q.defer();

        // Passer på at laste-indikatoren vises i minimum 2 sekunder, for å unngå at den bare "blinker"
        $timeout(function() {
            lasteindikatorDefer.resolve();
        }, 2000);

        promiseArray.push(config.$promise, tekster.$promise, lasteindikatorDefer.promise);

        var resolve = $q.all(promiseArray);

        resolve.then(function() {
            $rootScope.app.laster = false;
        });

        return resolve;
    }]);

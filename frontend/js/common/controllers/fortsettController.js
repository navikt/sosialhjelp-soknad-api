angular.module('nav.fortsett.controller', [])
    .controller('FortsettCtrl', ['$rootScope', 'data', '$location', function ($rootScope, data, $location) {
        if (data.soknad.delstegStatus === "SKJEMA_VALIDERT") {
            $location.path(data.soknad.brukerBehandlingId + '/vedlegg');
        } else if (data.soknad.delstegStatus === "VEDLEGG_VALIDERT") {
            $location.path(data.soknad.brukerBehandlingId + '/oppsummering');
        } else {
            $location.path(data.soknad.brukerBehandlingId + '/soknad/');
        }
    }]);
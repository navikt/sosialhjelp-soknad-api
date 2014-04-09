/* jshint scripturl: true */

angular.module('sendsoknad')
    .value('data', {})
    .value('cms', {})
    .constant('validertKlasse', 'validert')
    .run(['$rootScope', 'data', '$location', 'sjekkUtslagskriterier', function ($rootScope, data, $location, sjekkUtslagskriterier) {
        $('#hoykontrast a, .skriftstorrelse a').attr('href', 'javascript:void(0)');

        $rootScope.$on('$routeChangeSuccess', function(event, next, current) {
            if (next.$$route) {
                /*
                 * Dersom vi kommer inn på informasjonsside utenfra (current sin redirectTo er informasjonsside), og krav for søknaden er oppfylt, skal vi redirecte til rett side.
                 */
                if (skalRedirecteTilRettSideIfolgeDelstegStatus()) {
                    redirectTilRettSideBasertPaDelstegStatus();
                } else if (next.$$route.originalPath === "/oppsummering") {
                    redirectTilVedleggsideDersomVedleggIkkeErValidert();
                    redirectTilSkjemasideDersomSkjemaIkkeErValidert();
                } else if (next.$$route.originalPath === "/vedlegg") {
                    redirectTilSkjemasideDersomSkjemaIkkeErValidert();
                } else if (current && next.$$route.originalPath === "/fortsettsenere") {
                    $rootScope.forrigeSide = current.$$route.originalPath;
                }
            }

            function skalRedirecteTilRettSideIfolgeDelstegStatus() {
                return next.$$route.originalPath === "/informasjonsside" && sjekkUtslagskriterier.erOppfylt() && (!current || current.redirectTo === '/informasjonsside') && data.soknad;
            }
        });

        function harHentetData() {
            return data && data.soknad;
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

        function redirectTilRettSideBasertPaDelstegStatus() {
            if (data.soknad.delstegStatus === "SKJEMA_VALIDERT") {
                $location.path('/vedlegg');
            } else if (data.soknad.delstegStatus === "VEDLEGG_VALIDERT") {
                $location.path('/oppsummering');
            } else {
                $location.path('/soknad');
            }
        }

        function skjemaErValidert() {
            return data.soknad.delstegStatus === "SKJEMA_VALIDERT" || vedleggErValidert();
        }

        function vedleggErValidert() {
            return data.soknad.delstegStatus === "VEDLEGG_VALIDERT";
        }
    }]);

angular.module('nav.services.interceptor.delsteg', [])
    .factory('settDelstegStatusEtterKallMotServer', ['data', function (data) {
        function settDelsteg(urlArray, response) {
            if (urlArray.contains('fakta') && response.data.key !== 'epost') {
                data.soknad.delstegStatus = 'UTFYLLING';
            } else if (urlArray.contains('vedlegg')) {
                data.soknad.delstegStatus = 'SKJEMA_VALIDERT';
            } else if (urlArray.contains('delsteg')) {
                if (response.config.data.delsteg === "vedlegg") {
                    data.soknad.delstegStatus = 'SKJEMA_VALIDERT';
                } else if (response.config.data.delsteg === "oppsummering") {
                    data.soknad.delstegStatus = 'VEDLEGG_VALIDERT';
                } else {
                    data.soknad.delstegStatus = 'UTFYLLING';
                }
            }
        }

        return {
            'response': function(response) {
                if (response === undefined || response.config === undefined) {
                    return response;
                }

                if (response.config.method === 'POST') {
                    if (data.soknad) {
                        data.soknad.sistLagret = new Date().getTime();
                    }
                    var urlArray = response.config.url.split('/');
                    settDelsteg(urlArray, response);
                }
                return response;
            }
        };
    }]);
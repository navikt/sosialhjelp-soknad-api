angular.module('nav.stegindikator', [])
	.directive('stegindikator', function (data) {
		return {
			restrict   : 'A',
			replace    : true,
			templateUrl: '../js/common/directives/stegindikator/stegIndikatorTemplate.html',
			scope      : {
				'aktivIndex': '@',
				'stegListe' : '@'
			},
            link: {
                pre: function(scope) {
                    scope.stegListe = scope.stegListe.replace(/ /g, '');
                    scope.data = {
                        'liste': $.trim(scope.stegListe).split(',')
                    };

                    scope.erKlikkbar = function(idx) {
                        if (idx === 3) {
                            return vedleggErValidert();
                        } else if (idx === 2) {
                            return skjemaErValidert();
                        } else if (idx === 1) {
                            return utfyllingStartet();
                        } else {
                            return true;
                        }

                    };

                    scope.erIkkeKlikkbar = function(idx) {
                        return !scope.erKlikkbar(idx);
                    };

                    scope.hentLenke = function(idx) {
                        var baseUrl = "#";
                        if(data.soknad) {
                            baseUrl += '/' + data.soknad.brukerBehandlingId;
                        }

                        if (idx === 2) {
                            return baseUrl + '/vedlegg';
                        } else if (idx === 1) {
                            return baseUrl + '/soknad';
                        } else if (idx === 3) {
                            return baseUrl + '/oppsummering';
                        } else {
                            return '#/informasjonsside';
                        }
                    };

                    function utfyllingStartet() {
                        if (data && data.soknad) {
                            return data.soknad.delstegStatus === "UTFYLLING" || skjemaErValidert();
                        }
                        return false;
                    }

                    function skjemaErValidert() {
                        if (data && data.soknad) {
                            return data.soknad.delstegStatus === "SKJEMA_VALIDERT" || vedleggErValidert();
                        }
                        return false;
                    }

                    function vedleggErValidert() {
                        if (data && data.soknad) {
                            return data.soknad.delstegStatus === "VEDLEGG_VALIDERT";
                        }
                        return false;
                    }
                }
            }
		};
	});

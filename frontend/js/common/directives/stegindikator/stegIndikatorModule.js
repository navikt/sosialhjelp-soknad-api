angular.module('nav.stegindikator', ['nav.cmstekster'])
	.directive('stegindikator', ['data', function (data) {
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
                        if (idx === 2) {
                            return '#/vedlegg';
                        } else if (idx === 1) {
                            return '#/soknad';
                        }
                        else if (idx === 3)
                        {
                            return '#/oppsummering';
                        }
                        else {
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
	}]);

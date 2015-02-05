angular.module('nav.sistLagret', [])
	.directive('sistLagret', ['data', function (data) {
		return {
			replace    : true,
            scope: {
                navtilbakelenke: '@'
            },
            templateUrl: '../js/common/directives/sistlagret/sistLagretTemplate.html',
			link       : function (scope) {
				scope.soknadId = data.soknad.soknadId;
                scope.brukerBehandlingId = data.soknad.brukerBehandlingId;
                scope.lenke = {
                    value: ""
                };

                if(scope.navtilbakelenke.indexOf('vedlegg') > -1) {
                    scope.lenke.value="#/vedlegg";
                } else if (scope.navtilbakelenke.indexOf('soknad') > -1) {
                    scope.lenke.value="#/soknad";
                }

				scope.hentSistLagretTid = function () {
					return data.soknad.sistLagret;
				};

				scope.soknadHarBlittLagret = function () {
                    if(data.soknad.sistLagret) {
                        return data.soknad.sistLagret !== null;
                    } else {
                        return false;
                    }
				};

				scope.soknadHarAldriBlittLagret = function () {
					return !scope.soknadHarBlittLagret();
				};
			}
		};
	}]);

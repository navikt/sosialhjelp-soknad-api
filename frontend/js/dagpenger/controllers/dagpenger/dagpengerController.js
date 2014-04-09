angular.module('nav.dagpenger', [])
	.controller('DagpengerCtrl', ['$scope', 'data', '$modal', function ($scope, data, $modal) {

		$scope.grupper = [
			{id: 'reellarbeidssoker', tittel: 'reellarbeidssoker.tittel', template: '../views/templates/reellarbeidssoker/reell-arbeidssoker.html', apen: false, skalSettesTilValidVedForsteApning: false, validering: false},
			{id: 'arbeidsforhold', tittel: 'arbeidsforhold.tittel', template: '../views/templates/arbeidsforhold.html', apen: false, skalSettesTilValidVedForsteApning: false, validering: false},
			{id: 'egennaering', tittel: 'egennaering.tittel', template: '../views/templates/egennaering/egen-naering.html', apen: false, skalSettesTilValidVedForsteApning: false, validering: false},
			{id: 'verneplikt', tittel: 'ikkeavtjentverneplikt.tittel', template: '../views/templates/verneplikt.html', apen: false, skalSettesTilValidVedForsteApning: false, validering: false},
			{id: 'utdanning', tittel: 'utdanning.tittel', template: '../views/templates/utdanning/utdanning.html', apen: false, skalSettesTilValidVedForsteApning: false, validering: false},
			{id: 'ytelser', tittel: 'ytelser.tittel', template: '../views/templates/ytelser.html', apen: false, skalSettesTilValidVedForsteApning: false, validering: false},
			{id: 'personalia', tittel: 'personalia.tittel', template: '../views/templates/personalia.html', apen: false, skalSettesTilValidVedForsteApning: true, validering: false},
            {id: 'barnetillegg', tittel: 'barnetillegg.tittel', template: '../views/templates/barnetillegg/barnetillegg.html', apen: false, skalSettesTilValidVedForsteApning: true, validering: false},
            {id: 'tilleggsopplysninger', tittel: 'tilleggsopplysninger.tittel', template: '../views/templates/tilleggsopplysninger.html', apen: false, skalSettesTilValidVedForsteApning: false, validering: false}
		];

        $scope.leggTilValideringsmetode = function(bolkId, valideringsmetode) {
            var idx = $scope.grupper.indexByValue(bolkId);
            $scope.grupper[idx].valideringsmetode = valideringsmetode;
        };

        $scope.fremdriftsindikator = {
            laster: false
        };

		$scope.mineHenveldelserUrl = data.config["minehenvendelser.link.url"];

        $scope.stickyFeilmelding = function() {
            $scope.leggTilStickyFeilmelding();
        };

        $scope.apneTab = function(ider) {
            settApenStatusForAccordion(true, ider);
        };

        $scope.lukkTab = function(ider) {
            settApenStatusForAccordion(false, ider);
        };

        $scope.settValidert = function(id) {
            var idx = $scope.grupper.indexByValue(id);
            $scope.grupper[idx].validering = false;
        };

		function settApenStatusForAccordion(apen, ider) {
			if (ider instanceof Array) {
				angular.forEach(ider, function (id) {
					settApenForId(apen, id);
				});
			} else {
				settApenForId(apen, ider);
			}
		}

		function settApenForId(apen, id) {
			var idx = $scope.grupper.indexByValue(id);
            if (idx > -1) {
                $scope.grupper[idx].apen = apen;
            }
		}
	}])
	.controller('FerdigstiltCtrl', ['$scope', 'data', function ($scope, data) {
		$scope.mineHenveldelserUrl = data.config["minehenvendelser.link.url"];
	}]);


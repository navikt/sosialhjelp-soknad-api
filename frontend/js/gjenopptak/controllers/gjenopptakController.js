angular.module('nav.gjenopptak', [])
	.controller('GjenopptakCtrl', ['$scope', 'data', '$modal', function ($scope, data, $modal) {

		$scope.grupper = [
            {id: 'reellarbeidssoker', tittel: 'reellarbeidssoker.tittel', template: '../views/templates/reellarbeidssoker/reell-arbeidssoker.html', apen: false, skalSettesTilValidVedForsteApning: false, validering: false}
		];

        $scope.leggTilValideringsmetode = function(bolkId, valideringsmetode) {
            var idx = $scope.grupper.indexByValue(bolkId);
            $scope.grupper[idx].valideringsmetode = valideringsmetode;
        };

        $scope.fremdriftsindikator = {
            laster: false
        };

		$scope.saksoversiktUrl = data.config["saksoversikt.link.url"];

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
		$scope.saksoversiktUrl = data.config["saksoversikt.link.url"];
	}]);


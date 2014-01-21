angular.module('nav.vedlegg.controller', [])
	.controller('VisVedleggCtrl', ['$scope', '$routeParams', 'vedleggService', 'Faktum', function ($scope, $routeParams, vedleggService, Faktum) {
		$scope.vedlegg = vedleggService.get({
			soknadId : $routeParams.soknadId,
			faktumId : $routeParams.faktumId,
			vedleggId: $routeParams.vedleggId
		});
	}])

	.controller('VedleggCtrl', ['$scope', '$location', '$routeParams', '$anchorScroll', 'data', 'vedleggService', 'Faktum', function ($scope, $location, $routeParams, $anchorScroll, data, vedleggService, Faktum) {
        $scope.data = {soknadId: data.soknad.soknadId};

		$scope.forventninger = vedleggService.query({soknadId: data.soknad.soknadId});
		$scope.sidedata = {navn: 'vedlegg'};

		$scope.vedleggEr = function (vedlegg, status) {
			return vedlegg.innsendingsvalg === status;
		};

		$scope.slettVedlegg = function (forventning) {
			if (false && $scope.erEkstraVedlegg(forventning)) {
				$scope.slettAnnetVedlegg(forventning);
			}
			forventning.$remove().then(function () {
				forventning.innsendingsvalg = 'VedleggKreves';
				forventning.vedleggId = null;
			});
		};
        $scope.lagreVedlegg = function (forventning) {
			forventning.$save();
		};

		$scope.key = function (forventning) {
			return 'vedlegg_' + forventning.skjemaNummer;
		};

		$scope.endreInnsendingsvalg = function (forventning, valg) {
			if (valg !== undefined) {
				forventning.innsendingsvalg = valg;
			}
			forventning.$save();
		};

		$scope.erEkstraVedlegg = function (forventning) {
			return forventning.skjemaNummer === 'L6';
		};

		$scope.slettAnnetVedlegg = function (forventning) {
			var index = $scope.forventninger.indexOf(forventning);
			Faktum.delete({soknadId: data.soknad.soknadId, faktumId: forventning.faktum.faktumId});
			$scope.forventninger.splice(index, 1);
		};

		$scope.nyttAnnetVedlegg = function () {
			new Faktum({
				key     : 'ekstraVedlegg',
				value   : 'true',
				soknadId: data.soknad.soknadId
			}).$save().then(function (nyttfaktum) {
					VedleggForventning.query({soknadId: data.soknad.soknadId, faktumId: nyttfaktum.faktumId}, function (forventninger) {
						$scope.forventninger.push.apply($scope.forventninger, forventninger);
					});
				});
		};
	}])

	.directive('bildeNavigering', [function () {
		return {
			restrict   : 'a',
			replace    : 'true',
			templateUrl: '../../'
		}
	}]);

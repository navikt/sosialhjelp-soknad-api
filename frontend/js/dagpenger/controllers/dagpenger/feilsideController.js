angular.module('nav.feilside', [])
    .controller('FeilSideCtrl', ['$scope', 'data',  function ($scope, data) {
        $scope.mineInnsendinger = data.config["minehenvendelser.link.url"];
        $scope.inngangsportenUrl = data.config["soknad.inngangsporten.url"];
    }]);
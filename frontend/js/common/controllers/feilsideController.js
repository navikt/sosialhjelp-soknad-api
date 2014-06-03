angular.module('nav.feilside', [])
    .run([function() {
        // Preload ikon for feilside
        var img = new Image();
        img.src = "../img/utropstegn-sirkel-gra.svg";
    }])
    .controller('FeilSideCtrl', ['$scope', 'data',  function ($scope, data) {
        $scope.mineInnsendinger = data.config["saksoversikt.link.url"];
        $scope.inngangsportenUrl = data.config["soknad.inngangsporten.url"];
    }]);
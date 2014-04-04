angular.module('nav.feilside', [])
    .run([function() {
        // Preload ikon for feilside
        var img = new Image();
        img.src = "../img/utropstegn-sirkel-gra.svg";
    }])
    .controller('FeilSideCtrl', ['$scope', 'data',  function ($scope, data) {
        $scope.mineInnsendinger = data.config["minehenvendelser.link.url"];
        $scope.inngangsportenUrl = data.config["soknad.inngangsporten.url"];
    }])
    .controller('ServerFeilCtrl', ['$scope', 'data', '$filter',  function ($scope, data, $filter) {
        $scope.relast = function() {
            window.location.reload();
        };

        $scope.loggutUrl = $('.innstillinger-innlogget .innlogging .loggut').attr('href');
        $scope.lagretTidspunkt = $filter('date')(data.soknad.sistLagret, 'short');
    }]);
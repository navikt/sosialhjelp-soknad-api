angular.module('nav.opplasting.controller', ['blueimp.fileupload'])
    .controller('OpplastingCtrl', ['$scope', '$http', '$location', '$routeParams', 'data', function ($scope, $http, $location, $routeParams, data) {
        $scope.fremdriftsindikator = {
            laster: false
        };
        $scope.data = {
            faktumId: $routeParams.faktumId
        };

        $scope.options = {
            maxFileSize: 10000000,
            acceptFileTypes: /(\.|\/)(jpg|png|pdf|jpeg)$/i,
            url: "/sendsoknad/rest/soknad/" + data.soknad.soknadId + "/vedlegg?faktumId=" + $scope.data.faktumId
        };
        $scope.lastopp = function () {
            submit();
        };
        $scope.leggVed = function () {
            var soknadId = data.soknad.soknadId;
            $scope.fremdriftsindikator.laster = true;
            $http({
                method: 'post',
                url: '../rest/soknad/' + +soknadId + '/vedlegg/generer?faktumId=' + $scope.data.faktumId
            }).success(function () {
                    $scope.fremdriftsindikator.laster = false;
                    $location.path('/vedlegg/' + soknadId);
                }).error(function () {
                    $scope.fremdriftsindikator.laster = false;
                });
        };
    }])
    .controller('SlettOpplastingCtrl', ['$scope', '$http', function ($scope, $http) {
        var file = $scope.file;
        file.$destroy = function () {
            $http({
                method: "post",
                url: "../rest/" + file.deleteUrl
            }).success(function () {
                    $scope.clear(file);
                });
        }
    }])

    .directive('lastOppFil', function () {
        'use strict';
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                element.bind('change', function () {
                    scope.submit();
                });
            }
        }
    })
    .directive('asyncImage', function () {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                var img = new Image();
                img.onload = function () {
                    element.parent().css('background-image', 'none');
                    element.replaceWith(img);
                };
                img.src = attrs['asyncImage'];
            }
        }
    });

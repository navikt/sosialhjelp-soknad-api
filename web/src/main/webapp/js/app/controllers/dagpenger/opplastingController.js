angular.module('nav.opplasting.controller', ['blueimp.fileupload'])
    .controller('OpplastingCtrl', ['$scope', '$http', '$routeParams', 'data', function ($scope, $http, $routeParams, data) {
        $scope.data = {
            faktumId: $routeParams.faktumId
        };
        $scope.options = {
            maxFileSize: 10000000,
            acceptFileTypes: /(\.|\/)(jpg|png|pdf|jpeg)$/i,
            url: "/sendsoknad/rest/soknad/" + data.soknadId + "/vedlegg?faktumId=" + $scope.data.faktumId
        };
        $scope.lastopp = function () {
            submit();
        };
    }])
    .controller('SlettOpplastingCtrl', ['$scope', '$http', function ($scope, $http) {
        var file = $scope.file;
        file.$destroy = function () {
            $http({
                method: "post",
                url: "../rest/" + file.deleteUrl
            }).then(function () {
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

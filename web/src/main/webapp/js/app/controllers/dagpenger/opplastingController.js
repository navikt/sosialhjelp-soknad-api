angular.module('nav.opplasting.controller', ['blueimp.fileupload'])
    .controller('OpplastingCtrl', ['$scope', '$http', '$location', '$routeParams', 'vedleggService', 'data', function ($scope, $http, $location, $routeParams, vedleggService, data) {
        $scope.fremdriftsindikator = {
            laster: false
        };
        $scope.data = {
            faktumId: $routeParams.faktumId
        };

        $scope.options = {
            maxFileSize: 10000000,
            acceptFileTypes: /(\.|\/)(jpg|png|pdf|jpeg)$/i,
            url: "/sendsoknad/rest/soknad/" + data.soknad.soknadId + "/faktum/" + $scope.data.faktumId + "/vedlegg"
        };
        $scope.lastopp = function () {
            submit();
        };
        $scope.leggVed = function () {
            var soknadId = data.soknad.soknadId;
            $scope.fremdriftsindikator.laster = true;
            vedleggService.merge({
                soknadId: soknadId,
                faktumId: $scope.data.faktumId
            }, function () {
                $scope.fremdriftsindikator.laster = false;
                $location.path('/vedlegg/' + soknadId);
            }, function () {
                $scope.fremdriftsindikator.laster = false;
            });
        };
    }])
    .controller('SlettOpplastingCtrl', ['$scope', 'vedleggService', 'data', function ($scope, vedleggService, data) {
        var file = $scope.file;
        file.$destroy = function () {
            vedleggService.remove({
                soknadId: data.soknad.soknadId,
                faktumId: $scope.data.faktumId,
                vedleggId: $scope.file.id
            }, function () {
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

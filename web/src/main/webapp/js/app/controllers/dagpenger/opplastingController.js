angular.module('nav.opplasting.controller', ['blueimp.fileupload'])
    .controller('OpplastingCtrl', ['$scope', '$http', '$location', '$routeParams', 'vedleggService', 'soknadService', 'data', function ($scope, $http, $location, $routeParams, vedleggService, soknadService, data) {
        $scope.fremdriftsindikator = {
            laster: false
        };
        $scope.data = {
            faktumId: $routeParams.faktumId,
            soknadId: data.soknad.soknadId
        };

        $scope.options = {
            maxFileSize: 10000000,
            acceptFileTypes: /(\.|\/)(jpg|png|pdf|jpeg)$/i,
            url: "/sendsoknad/rest/soknad/" + data.soknad.soknadId + "/faktum/" + $scope.data.faktumId + "/vedlegg"
        };
        $scope.lastopp = function () {
            submit();
        };
        $scope.oppdaterSoknad = function () {
            soknadService.get({param: data.soknad.soknadId},
                function (result) { // Success
                    data.soknad = result;
                    $scope.fremdriftsindikator.laster = false;
                    $location.url('/vedlegg/' + data.soknad.soknadId + "?scrollTo=faktum_" + $scope.data.faktumId).replace();
                }
            );
        }
        $scope.leggVed = function () {

            var soknadId = data.soknad.soknadId;

            $scope.fremdriftsindikator.laster = true;
            vedleggService.merge({
                soknadId: soknadId,
                faktumId: $scope.data.faktumId
            }, function (data) {
                $scope.oppdaterSoknad();
            }, function () {
                $scope.fremdriftsindikator.laster = false;
            });
        };


        $scope.loadingFiles = true;
        vedleggService.get({
                soknadId: data.soknad.soknadId,
                faktumId: $scope.data.faktumId
            }, function (data) {
                $scope.queue = data.files || [];
                $scope.loadingFiles = false;
            }
        );
    }])
    .controller('SlettOpplastingCtrl', ['$scope', 'vedleggService', 'data', function ($scope, vedleggService, data) {
        var file = $scope.file;
        file.$destroy = function () {
            vedleggService.remove({
                soknadId: data.soknad.soknadId,
                faktumId: $scope.data.faktumId,
                vedleggId: file.vedlegg.id
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

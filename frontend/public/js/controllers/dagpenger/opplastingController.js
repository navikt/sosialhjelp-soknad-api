angular.module('nav.opplasting.controller', ['blueimp.fileupload'])
    .controller('OpplastingCtrl', ['$scope', '$http', '$location', '$routeParams', 'vedleggService', 'soknadService', 'data', function ($scope, $http, $location, $routeParams, vedleggService, soknadService, data) {
        $scope.fremdriftsindikator = {
            laster: false
        };
        $scope.opplastingFeilet = false;
        $scope.data = {
            faktumId: $routeParams.faktumId,
            gosysId: $routeParams.gosysId,
            soknadId: data.soknad.soknadId,
            autoUpload: true
        };

        $scope.$on('fileuploadprocessfail', function (event, data) {
            $.each(data.files, function (index, file) {
                if (file.error) {
                    $scope.opplastingFeilet = file.error;
                    data.scope().clear(file);
                    $scope.clear(file);
                }
            })
        });

        $scope.options = {
            maxFileSize: 10000000,
            acceptFileTypes: /(\.|\/)(jpg|png|pdf|jpeg)$/i,
            url: '/sendsoknad/rest/soknad/' + data.soknad.soknadId + '/faktum/' + $scope.data.faktumId + '/vedlegg?gosysId=' + $scope.data.gosysId
        };

        $scope.opplastingFeil = function (error) {
            $scope.opplastingFeilet = error;
        };

        $scope.lastopp = function () {
            submit();
            $scope.submit()
        };

        $scope.oppdaterSoknad = function () {
            soknadService.get({param: data.soknad.soknadId},
                function (result) { // Success
                    data.soknad = result;
                    $scope.fremdriftsindikator.laster = false;
                    $location.url('/vedlegg/' + data.soknad.soknadId + '?scrollTo=faktum_' + $scope.data.faktumId).replace();
                }
            );
        };

        $scope.leggVed = function () {
            var soknadId = data.soknad.soknadId;
            $scope.fremdriftsindikator.laster = true;
            vedleggService.merge({
                soknadId: soknadId,
                faktumId: $scope.data.faktumId,
                gosysId: $scope.data.gosysId
            }, function (data) {
                $scope.oppdaterSoknad();
            }, function () {
                $scope.fremdriftsindikator.laster = false;
            });
        };


        $scope.loadingFiles = true;
        vedleggService.get({
                soknadId: data.soknad.soknadId,
                faktumId: $scope.data.faktumId,
                gosysId: $scope.data.gosysId
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

    .directive('filFeil', function () {
        'use strict';
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                scope.$watch('file.error', function (a1, a2, a3, a4) {
                    if (a2) {
                        scope.clear(scope.file);
                    }
                })
            }
        }
    })

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

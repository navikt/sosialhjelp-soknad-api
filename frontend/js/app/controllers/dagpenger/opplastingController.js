angular.module('nav.opplasting.controller', ['blueimp.fileupload'])

    .controller('OpplastingVedleggCtrl', ['$scope', '$http', '$location', '$routeParams', 'vedleggService', 'soknadService', 'data', function ($scope, $http, $location, $routeParams, vedleggService, soknadService, data) {
        $scope.vedlegg = vedleggService.get({soknadId: data.soknad.soknadId, vedleggId: $routeParams.vedleggId});
        $scope.soknad = data.soknad;
    }])
    .controller('OpplastingCtrl', ['$scope', '$http', '$location', '$routeParams', '$cookies', 'vedleggService', 'soknadService', 'data', 'cms', function ($scope, $http, $location, $routeParams, $cookies, vedleggService, soknadService, data, cms) {

        $scope.fremdriftsindikator = {
            laster: false
        };

        $scope.harLagtTilVedlegg = {
            value:false
        };

        $scope.skalViseFeilmelding = false;
        $scope.opplastingFeilet = false;

        $scope.data = {
            vedleggId: $routeParams.vedleggId,
            soknadId: data.soknad.soknadId,
            opplastingFeilet: false,
            xsrfToken: $cookies['XSRF-TOKEN']
        };

        $scope.$on('fileuploadstart', function () {
            $scope.skalViseFeilmelding = false;
            $scope.data.opplastingFeilet = false;
        });

        $scope.$on('fileuploadprocessfail', function (event, data) {
            $.each(data.files, function (index, file) {
                if (file.error) {
                    $scope.data.opplastingFeilet = file.error;
                    data.scope().clear(file);
                    $scope.clear(file);
                }
            });
        });
        $.ajaxSetup({
            converters: {
                'iframe json': function(iframe){
                    var result = iframe && $.parseJSON($(iframe[0].body).text())
                    if(result.kode){
                        throw result.kode
                    }
                    return result;
                }
            }
        });
        $scope.options = {
            maxFileSize: 4000000,
            formData: {'X-XSRF-TOKEN': $cookies['XSRF-TOKEN']},
            acceptFileTypes: /(\.|\/)(jpg|png|pdf|jpeg)$/i,
            autoUpload: true,
            dataType: 'json',
            headers: {'X-XSRF-TOKEN': $cookies['XSRF-TOKEN'] },
            url: '/sendsoknad/rest/soknad/' + data.soknad.soknadId + '/vedlegg/' + $scope.data.vedleggId + '/opplasting',
            done: function (e, data) {
                $scope.clear(data.originalFiles[0]);
                data.result.files.forEach(function (item) {
                    $scope.queue.push(new vedleggService(item));
                });
            },
            fail: function(e, data){
                var errorCode;
                if(data.jqXHR.responseJSON){
                    errorCode = data.jqXHR.responseJSON.kode;
                } else {
                    errorCode = data.response().errorThrown;
                }
                $scope.data.opplastingFeilet =cms.tekster[errorCode];
                $.each(data.files, function (index, file) {
                    data.scope.clear(file);
                    $scope.clear(file);
                });
            },
            // Error and info messages:
            messages: {
                maxNumberOfFiles: cms.tekster['opplasting.feilmelding.makssider'],
                acceptFileTypes: cms.tekster['opplasting.feilmelding.feiltype'],
                maxFileSize: cms.tekster['opplasting.feilmelding.maksstorrelse']
            }
        };

        $scope.opplastingFeil = function (error) {
            $scope.data.opplastingFeilet = error;
        };

        $scope.lastopp = function () {
            submit();
            $scope.submit();
            $scope.data.opplastingFeilet = false;
            $scope.$apply();
        };

        $scope.oppdaterSoknad = function () {
            soknadService.get({soknadId: data.soknad.soknadId},
                function (result) { // Success
                    data.soknad = result;
                    $scope.fremdriftsindikator.laster = false;
                    $location.url('/vedlegg?scrollTo=vedlegg_' + $scope.data.vedleggId).replace();
                }
            );
        };

        $scope.leggVed = function () {
            if ($scope.queue.length > 0) {
                $scope.skalViseFeilmelding = false;
                var soknadId = data.soknad.soknadId;
                $scope.fremdriftsindikator.laster = true;
                vedleggService.merge({
                    soknadId: soknadId,
                    vedleggId: $scope.data.vedleggId
                }, function (data) {
                    $scope.oppdaterSoknad();
                }, function () {
                    $scope.fremdriftsindikator.laster = false;
                });
            } else {
                $scope.skalViseFeilmelding = true;
            }

        };

        $scope.loadingFiles = true;
        vedleggService.underbehandling({
                soknadId: data.soknad.soknadId,
                vedleggId: $scope.data.vedleggId
            }, function (data) {
                $scope.queue = data || [];
                $scope.loadingFiles = false;
            }
        );
    }])

    .controller('SlettOpplastingCtrl', ['$scope', function ($scope) {
        var file = $scope.file;
        file.$destroy = function () {
            $scope.data.opplastingFeilet = false;
            file.$remove().then(function () {
                $scope.clear(file);
            });
        };
    }])

    .directive('filFeil', [function () {
        'use strict';
        return {
            restrict: 'A',
            link: function (scope) {
                scope.$watch('file.error', function (a1, a2) {
                    if (a2) {
                        scope.clear(scope.file);
                    }
                });
            }
        };
    }])

    .directive('lastOppFil', [function () {
        'use strict';
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                element.bind('change', function () {
                    scope.submit();
                });
            }
        };
    }]);

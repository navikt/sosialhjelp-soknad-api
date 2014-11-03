angular.module('nav.opplasting.controller', ['blueimp.fileupload'])

    .controller('OpplastingVedleggCtrl', function ($scope, $routeParams, vedleggService, data, vedleggListe) {
        $scope.vedleggListe = vedleggListe;
        $scope.vedlegg = vedleggService.get({soknadId: data.soknad.soknadId, vedleggId: $routeParams.vedleggId});
        $scope.soknad = data.soknad;

        $scope.erAnnetVedlegg = function() {
            return $scope.vedlegg.skjemaNummer === "N6";
        };

        $scope.erIkkeAnnetVedlegg = function() {
            return !$scope.erAnnetVedlegg();
        };
    })
    .controller('OpplastingCtrl', function ($scope, $location, $routeParams, $cookies, vedleggService, data, cms) {
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
            $scope.fremdriftsindikator.laster = true;
            $scope.skalViseFeilmelding = false;
            $scope.data.opplastingFeilet = false;
        });

        $scope.$on('fileuploadprocessfail', function (event, data) {
            $.each(data.files, function (index, file) {
                if (file.error) {
                    $scope.fremdriftsindikator.laster = false;
                    $scope.data.opplastingFeilet = file.error;
                    $scope.clear(file);
                }
            });
        });

        $scope.harIkkeLastetOppFil = function(v) {
            return v.vedleggId === undefined;
        };

        $.ajaxSetup({
            converters: {
                'iframe json': function(iframe){
                    var result = iframe && $.parseJSON($(iframe[0].body).text());
                    if(result.kode){
                        throw result.kode;
                    }
                    return result;
                }
            }
        });


        $scope.options = {
            maxFileSize: 1024*1024*10,
            formData: {'X-XSRF-TOKEN': $cookies['XSRF-TOKEN']},
            acceptFileTypes: /(\.|\/)(jpg|png|pdf|jpeg)$/i,
            autoUpload: true,
            dataType: 'json',
            headers: {'X-XSRF-TOKEN': $cookies['XSRF-TOKEN'] },
            url: '/sendsoknad/rest/soknad/' + data.soknad.soknadId + '/vedlegg/' + $scope.data.vedleggId + '/opplasting',
            done: function (e, data) {
                data.originalFiles.forEach(function(file) {
                    $scope.clear(file);
                });
                data.result.files.forEach(function (item) {
                    $scope.queue.push(new vedleggService(item));
                });
            },
            fail: function(e, data){
                var errorCode;
                if(data.jqXHR.responseJSON){
                    errorCode = data.jqXHR.responseJSON.kode;
                } else if (data.response().textStatus === 'parsererror'){
                    errorCode = 'generell';
                } else {
                    errorCode = data.response().errorThrown;
                }

                if (errorCode === 'generell') {
                    // Generell feil fra server fordi filstørrelsen er for stor. Sett rett key for CMS
                    errorCode = 'opplasting.feilmelding.maksstorrelse';
                }
                $scope.fremdriftsindikator.laster = false;
                $scope.data.opplastingFeilet = cms.tekster[errorCode];
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

        $scope.oppdaterSoknad = function (v) {
            v.$get({}, function(result) {
                // TODO: Burde skrive om så vi ikke bruker data.soknad i det hele tatt
                var soknadVedleggIdx = data.soknad.vedlegg.indexByFieldValue('vedleggId', v.vedleggId);
                var vedleggListeIdx = $scope.vedleggListe.indexByFieldValue('vedleggId', v.vedleggId);

                data.soknad.vedlegg[soknadVedleggIdx] = result;
                $scope.vedleggListe[vedleggListeIdx] = result;
                $scope.fremdriftsindikator.laster = false;
                $location.url(data.soknad.brukerBehandlingId + '/vedlegg?scrollTo=vedlegg_' + $scope.data.vedleggId).replace();
            });
        };

        $scope.leggVed = function () {
            if ($scope.queue.length > 0) {
                $scope.skalViseFeilmelding = false;
                $scope.fremdriftsindikator.laster = true;
                vedleggService.merge({
                    soknadId: data.soknad.soknadId,
                    vedleggId: $scope.data.vedleggId
                }, function (data) {
                    $scope.oppdaterSoknad($scope.vedlegg);
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
    })

    .controller('SlettOpplastingCtrl', function ($scope) {
        var file = $scope.file;
        file.$destroy = function () {
            $scope.data.opplastingFeilet = false;
            file.$remove().then(function () {
                $scope.clear(file);
            });
        };
    })

    .directive('filFeil', function () {
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
    })

    .directive('lastOppFil', function () {
        'use strict';
        return {
            restrict: 'A',
            link: function (scope, element) {
                element.bind('change', function () {
                    scope.submit();
                });
            }
        };
    })

    .directive('alleFilerFerdig', function($timeout) {
        return function(scope, element) {
            $timeout(function() {
                scope.$watch(
                    function() {
                        return element.find('a.laster').length === 0;
                    },
                    function(value) {
                        if (value) {
                            scope.fremdriftsindikator.laster = false;
                        } else {
                            scope.fremdriftsindikator.laster = true;
                        }
                    }
                );
            });
        };
    })
    .directive('scrollOnError', function() {
        return function(scope, elm) {
            scrollToElement(elm, 50);
        };
    });

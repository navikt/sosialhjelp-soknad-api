angular.module('nav.cms.service', [])
    .factory('resolveKeyService', function($injector, cms) {
        var prefix;

        try {
            prefix = $injector.get('cmsprefix');
        } catch(e) {
            prefix = '';
        }

        return {
            getKey: function(nokkel) {
                if (nokkel instanceof Object) {
                    if (cms.tekster[prefix + nokkel.key]) {
                        return prefix + nokkel.key;
                    } else if (cms.tekster[nokkel.key]) {
                        return nokkel.key;
                    } else if (cms.tekster[prefix + nokkel.fallbackKey]) {
                        return prefix + nokkel.fallbackKey;
                    } else {
                        return nokkel.fallbackKey;
                    }
                } else {
                    if (cms.tekster[prefix + nokkel]) {
                        return prefix + nokkel;
                    } else {
                        return nokkel;
                    }
                }
            }
        };
    })
    .factory('cmsService', function($sce, $rootScope, cms, resolveKeyService) {
        return {
            getText: function(nokkel, args) {
                var key = resolveKeyService.getKey(nokkel);
                var tekst = cms.tekster[key];

                if (args instanceof Array) {
                    args.forEach(function (argTekst, idx) {
                        tekst = tekst.replace('{' + idx + '}', argTekst);
                    });
                } else if (args) {
                    tekst = tekst.replace('{0}', args);
                }

                if ($rootScope.visCmsnokkler) {
                    tekst += ' [' + key + ']';
                }

                return tekst === undefined ? '' : $sce.trustAsHtml(tekst);
            }
        };
    });
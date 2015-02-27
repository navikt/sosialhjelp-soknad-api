describe('cms', function () {
    var key = 'key';
    var fallbackKey = 'fallback.key';
    var utenPrefixValue = 'uten prefix';
    var utenPrefixFallbackValue = 'uten prefix fallback';
    var medPrefixKey = 'prefix.key';
    var medPrefixFallbackKey = 'prefix.fallback.key';
    var medPrefixValue = 'med prefix';
    var medPrefixFallbackValue = 'med prefix fallback';

    beforeEach(module(function ($provide) {
        $provide.value('cms', {
            'tekster': {
                'key': utenPrefixValue,
                'fallback.key': utenPrefixFallbackValue,
                'prefix.key': medPrefixValue,
                'prefix.fallback.key': medPrefixFallbackValue
            }
        });
    }));

    beforeEach(module('nav.cms'));

    describe('service for å resolve cms-key', function() {
        var keyService;

        describe('ingen prefix-constant definert', function () {
            beforeEach(inject(function (resolveKeyService) {
                keyService = resolveKeyService;
            }));

            it('skal få key uten prefix når ingen prefix-constant er definert', function() {
                expect(keyService.getKey(key)).toBe(key);
            });

            it('skal få hovednøkkel dersom nokkel er ett objekt', function() {
                expect(keyService.getKey({
                    key: 'key',
                    fallbackKey: 'fallback.key'
                })).toBe(key);
            });

            it('skal få fallback dersom hovednøkkel ikke er definert når nokkel er ett objekt', function() {
                expect(keyService.getKey({
                    key: 'key.finnes.ikke',
                    fallbackKey: 'fallback.key'
                })).toBe(fallbackKey);
            });
        });
        describe('med prefix-constant definert', function () {
            beforeEach(module(function ($provide) {
                $provide.constant('cmsprefix', 'prefix.');
            }));

            beforeEach(inject(function (resolveKeyService) {
                keyService = resolveKeyService;
            }));

            it('skal få key med prefix når prefix-constant er definert', function() {
                expect(keyService.getKey(key)).toBe(medPrefixKey);
            });

            it('skal få hovednøkkel med prefix dersom nokkel er ett objekt', function() {
                expect(keyService.getKey({
                    key: 'key',
                    fallbackKey: 'fallback.key'
                })).toBe(medPrefixKey);
            });

            it('skal få fallback dersom hovednøkkel ikke er definert når nokkel er ett objekt', function() {
                expect(keyService.getKey({
                    key: 'key.finnes.ikke',
                    fallbackKey: 'fallback.key'
                })).toBe(medPrefixFallbackKey);
            });
        });
    });

    describe('service for å hente tekst', function() {
        var cmsTextService, sce, rootScope;

        beforeEach(inject(function (cmsService, $sce, $rootScope) {
            cmsTextService = cmsService;
            sce = $sce;
            rootScope = $rootScope;
        }));

        it('skal få ut tekst fra gitt key', function() {
            expect(sce.getTrustedHtml(cmsTextService.getText(key))).toBe(utenPrefixValue);
        });

        it('skal få ut tekst fra gitt key', function() {
            rootScope.visCmsnokkler = true;
            expect(sce.getTrustedHtml(cmsTextService.getText(key))).toBe(utenPrefixValue + ' [' + key + ']');
        });
    });
});
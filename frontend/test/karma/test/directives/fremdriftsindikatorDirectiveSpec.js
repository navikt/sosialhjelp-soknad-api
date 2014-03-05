describe('fremdriftsindikator', function () {
    var scope, element;

    beforeEach(module('nav.fremdriftsindikator'));

    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element('<div><input type="button" fremdriftsindikator></div>');
        scope = $rootScope;

        scope.fremdriftsindikator = {
            laster: false
        };

        $compile(element)(scope);
        scope.$apply();
    }));

    describe('funksjonalitet', function() {
        beforeEach(inject(function ($compile, $rootScope) {
            element = angular.element('<div><input type="button" fremdriftsindikator></div>');
            scope = $rootScope;

            scope.fremdriftsindikator = {
                laster: false
            };

            $compile(element)(scope);
            scope.$apply();
        }));

        it('lasteindikator skal ikke vises, mens knappen skal vises, dersom fremdriftsindikator.laster er satt til false', function() {
            expect(element.find('input').css('display')).not.toBe('none');
            expect(element.find('img').css('display')).toBe('none');
        });

        it('lasteindikator skal vises og bilde skal skjules dersom fremdriftsindikator.laster er satt til true', function() {
            scope.fremdriftsindikator.laster = true;
            scope.$apply();
            expect(element.find('input').css('display')).toBe('none');
            expect(element.find('img').css('display')).not.toBe('none');
        });
    });

    describe('sjekk farge', function() {
        var compile;
        beforeEach(inject(function ($compile, $rootScope) {
            element = angular.element('<div></div>');
            scope = $rootScope;
            compile = $compile;
            scope.fremdriftsindikator = {
                laster: false
            };
        }));

        it('sjekk at fargen blir satt til gra', function() {
            var input = '<input type="button" fremdriftsindikator>';
            element.append(input);

            compile(element)(scope);
            scope.$apply();

            var imageSrc = element.find('img').attr('src');

            expect(imageSrc).toContain('hvit');
        });

        it('sjekk at fargen blir satt til gra', function() {
            var input = '<input type="button" fremdriftsindikator="rød">';
            element.append(input);

            compile(element)(scope);
            scope.$apply();

            var imageSrc = element.find('img').attr('src');

            expect(imageSrc).toContain('rod');
            expect(imageSrc).toContain('roed');
        });

        it('sjekk at fargen blir satt til gra', function() {
            var input = '<input type="button" fremdriftsindikator="svart">';
            element.append(input);

            compile(element)(scope);
            scope.$apply();

            var imageSrc = element.find('img').attr('src');

            expect(imageSrc).toContain('svart');
        });

        it('sjekk at fargen blir satt til gra', function() {
            var input = '<input type="button" fremdriftsindikator="grå">';
            element.append(input);

            compile(element)(scope);
            scope.$apply();

            var imageSrc = element.find('img').attr('src');

            expect(imageSrc).toContain('graa');
        });
    });
});
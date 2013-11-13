describe('directives', function () {
    var scope, form, element;

    beforeEach(module('app.directives'));
    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope;
        element = angular.element(
            '<form name="form">' +
                '<input type="text" ng-model="permiteringProsent" name="permiteringProsent" prosent />' +
                '</form>'
        );
        scope.permiteringProsent = '';
        $compile(element)(scope);
        scope.$digest();
        form = scope.form;
        element.scope().$apply();

    }));

    describe('prosent', function () {
        it('skal returnere true for prosenten 0', function () {
            form.permiteringProsent.$setViewValue('0');
            expect(scope.permiteringProsent).toEqual('0');
            expect(form.permiteringProsent.$valid).toBe(true);
        });
        it('skal returnere true for prosenten 100', function () {
            form.permiteringProsent.$setViewValue('100');
            expect(scope.permiteringProsent).toEqual('100');
            expect(form.permiteringProsent.$valid).toBe(true);
        });
        it('skal returnere false for prosenten p', function () {
            form.permiteringProsent.$setViewValue('p');
            expect(scope.permiteringProsent).toBeUndefined();
            expect(form.permiteringProsent.$valid).toBe(false);
        });
        it('skal returnere false for prosenten -1', function () {
            form.permiteringProsent.$setViewValue('-1');
            expect(scope.permiteringProsent).toBeUndefined();
            expect(form.permiteringProsent.$valid).toBe(false);
        });
        it('skal returnere false for prosenten 50.0', function () {
            form.permiteringProsent.$setViewValue('50.0');
            expect(scope.permiteringProsent).toBeUndefined();
            expect(form.permiteringProsent.$valid).toBe(false);
        });
        it('skal returnere false for prosenten 101', function () {
            form.permiteringProsent.$setViewValue('101');
            expect(scope.permiteringProsent).toBeUndefined();
            expect(form.permiteringProsent.$valid).toBe(false);
        });
        it('skal returnere false for prosenten 20%', function () {
            form.permiteringProsent.$setViewValue('20%');
            expect(scope.permiteringProsent).toBeUndefined();
            expect(form.permiteringProsent.$valid).toBe(false);
        });
    });
});


describe('directives', function () {
    var scope, form, element, checkbox;

    beforeEach(module('app.directives'));
    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope;
        element = angular.element(
            '<form name="form">' +
                '<input type="checkbox" ng-model="checkboxValueModel" name="checkbox" boolean-verdi />' +
                '</form>'
        );
        $compile(element)(scope);
        scope.$digest();
        form = scope.form;
        checkbox = form.checkbox;
        element.scope().$apply();

    }));

    describe('booleanVerdi', function () {
        it('viewvalue skal settes til false dersom det ikke er satt en verdi i modellen', function () {
            expect(checkbox.$viewValue).toEqual(false);
        });
        it('viewvalue skal være true når modellen er en string som sier "true"', function () {
            scope.checkboxValueModel = 'true';
            element.scope().$apply();
            expect(checkbox.$viewValue).toEqual(true);
        });
        it('viewvalue skal være false når modellen er en string som sier "false"', function () {
            scope.checkboxValueModel = 'false';
            element.scope().$apply();
            expect(checkbox.$viewValue).toEqual(false);
        });
        it('modellen skal lagres som string "true" når viewvalue (boolean) settes til true', function () {
            checkbox.$setViewValue(true);
            element.scope().$apply();
            expect(scope.checkboxValueModel).toEqual('true');
        });
        it('modellen skal lagres som string "false" når viewvalue (boolean) settes til false', function () {
            checkbox.$setViewValue(false);
            element.scope().$apply();
            expect(scope.checkboxValueModel).toEqual('false');
        });
    });
});

describe('directives', function () {
    var scope, form, element;

    beforeEach(module('nav.textarea'));

    beforeEach(inject(['$compile', '$rootScope', '$templateCache', function ($c, $r, $templateCache) {
        $templateCache.put("../js/app/directives/navtextarea/navtextareaTemplate.html", "<div>hello</div>");
        $compile = $c;
        $rootScope = $r
        scope = $rootScope;

        element = angular.element(
            '<form name="form">' +
                '<div navtextarea inputname="fritekst" data-ng-model="fritekst" nokkel="fritekst" maxlengde="500" feilmelding="fritekst.feilmelding"></div>' +
            '</form>'
        );

        scope.counter = 500;
        scope.feil = false;

        $compile(element)(scope);
        manualCompiledElement =angular.element($templateCache.get("../js/app/directives/navtextarea/navtextareaTemplate.html"));
        scope.$digest();
        form = scope.form;
        element.scope().$apply();

    }]));

    it('###########################################################################################', function () {
        scope.counter = -1;
        validerAntallTegn();
        expect(scope.feil).toEqual(true);

    });
});

// Må løse problem med å hente templates i testene før vi kan teste direktiv som bruker templateUrl
/*describe('directives', function() {
 var scope, element, form, radioknappen, $httpBackend;

 beforeEach(module('app.directives', function($provide) {
 $provide.value("data", {
 tekster: {
 "nokkel.sporsmal": "Sporsmal",
 "nokkel.true": "true",
 "nokkel.false": "false"
 }
 });
 }));
 beforeEach(inject(function($compile, $rootScope, _$httpBackend_){
 scope = $rootScope;
 $httpBackend = _$httpBackend_;
 scope.soknadData = {
 fakta: {
 nokkel: true
 }
 };

 scope.data = {
 redigeringsModus: true
 };

 element = angular.element(
 '<form name="form">' +
 '<booleanradio name="radioknappen" model="soknadData.fakta.nokkel.value" modus="data.redigeringsModus" nokkel="nokkel"/>' +
 '</form>'
 );
 $compile(element)(scope);
 scope.$digest();
 form = scope.form;
 radioknappen = form.radioknappen;
 element.scope().$apply();

 }));
 });*/

// Må løse problem med å hente templates i testene før vi kan teste direktiv som bruker templateUrl
/*describe('directives', function () {
 var scope, element, form;


 //    beforeEach(module('../js/app/directives/navinput/navcheckboxTemplate.html'));
 beforeEach(module('nav.input', function($provide) {
 $provide.value("data", {
 tekster: {
 "nokkel.label": "Label"
 }
 });
 }));

 beforeEach(inject(function ($compile, $rootScope, $templateCache) {
 var getTemplate = $templateCache.get;

 $templateCache.get = function(key) {
 console.log("############# " + key);
 return getTemplate(key);
 }

 scope = $rootScope;
 scope.soknadData = {
 fakta: {}
 };

 scope.data = {
 redigeringsModus: true
 };

 element = angular.element(
 '<form name="form">' +
 '<navcheckbox data-ng-model="soknadData.fakta.nokkel.value" modus="data.redigeringsModus" inputname="nokkel" label="nokkel.label"/>' +
 '</form>'
 );
 $compile(element)(scope);
 scope.$digest();
 form = scope.form;
 element.scope().$apply();
 }));

 it('skal kjøre', function(){
 console.log("Hallo");
 });
 });*/


describe('cmstekster-direktiv', function () {
    var scope, element;

    beforeEach(module('nav.cmstekster', function ($provide) {
        $provide.value("data", {
            tekster: {
                "nokkel.label": "Label",
                "nokkel.input": "Inputlabel"
            }
        });
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope;

        element = angular.element(
            '<div name="testdiv" >' +
                '<input type="text" name="testinput" cmstekster="nokkel.input" />' +
                '<span name="testspan" cmstekster="nokkel.label"></span>' +
            '</div>'
        );
        $compile(element)(scope);
        scope.$digest();
        element.scope().$apply();
    }));

    it('skal legge inn rett tekst ("Label") fra key i ett html-element', function () {
        var spanElement = element.find('span');
        expect(spanElement.text()).toEqual('Label');
    });

    it('skal legge inn rett tekst fra key som value-attributt på ett input-element', function () {
        var inputElement = element.find('input');
        expect(inputElement.attr('value')).toEqual('Inputlabel');
    });
});

describe('directives', function () {
    var scope, form, element;
    beforeEach(module('app.directives'));
    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope;
        element = angular.element(
            '<form name="form">' +
                '<input ng-model="arbeidsforhold.fra" name="fra" />' +
                '<input ng-model="$parent.arbeidsforhold.til" name="til" datotil fra-dato="arbeidsforhold.fra" til-dato="$parent.arbeidsforhold.til"/>' +
                '<input data-ng-model="arbeidsforhold.permiteringVetikke" name="permiteringVetikke" value="vetikke"  ng-click="vetIkke()" />' +
                '<input ng-model="arbeidsforhold.fraTo" name="fraTo" />' +
                '<input ng-model="$parent.arbeidsforhold.tilTo" name="tilTo" datotil fra-dato="arbeidsforhold.fraTo" til-dato="$parent.arbeidsforhold.tilTo"/>' +
                '</form>'
        );

        scope.arbeidsforhold = {
            fra: new Date(2010, 10, 10),
            til: new Date(2011, 10, 10),
            fraTo: new Date(2010, 10, 10),
            tilTo: new Date(2011, 10, 10),
            permiteringVetikke: true
        };

        $compile(element)(scope);
        scope.$digest();
        form = scope.form;
        element.scope().$apply();
    }));
    it('skal returnere false når fra-dato endres fra mindre til større enn til-dato', function () {
        scope.arbeidsforhold.fra = new Date(2012, 10, 10);
        element.scope().$apply();
        expect(scope.arbeidsforhold.fra).toEqual(new Date(2012, 10, 10));
        expect(form.fra.$invalid).toBe(false);
        expect(scope.$parent.arbeidsforhold.til).toBeUndefined();
        element.scope().$apply();
    });

    it('skal returnere true når fra-dato endres fra større til mindre enn til-dato', function () {
        scope.arbeidsforhold.fra = new Date(2011, 10, 10);
        element.scope().$apply();

        expect(scope.arbeidsforhold.fra).toEqual(new Date(2011, 10, 10));

        expect(form.til.$valid).toBe(false);
        expect(scope.$parent.arbeidsforhold.til).toBeUndefined();

        scope.arbeidsforhold.fra = new Date(2010, 10, 10);
        element.scope().$apply();
        expect(scope.arbeidsforhold.fra).toEqual(new Date(2010, 10, 10));
        expect(scope.arbeidsforhold.til).toEqual(new Date(2011, 10, 10));
        expect(form.til.$valid).toBe(true);
    });

    it('skal returnere false når til-dato endres til mindre enn fra-dato', function () {
        form.til.$setViewValue(new Date(2010, 10, 10));
        element.scope().$apply();
        expect(scope.$parent.arbeidsforhold.til).toBeUndefined();
        expect(form.til.$valid).toBe(false);
        expect(scope.arbeidsforhold.fra).toEqual(new Date(2010, 10, 10));
    });

    it('skal returnere true når til-dato endres til større enn fra-dato', function () {
        scope.arbeidsforhold.fra = new Date(2011, 10, 10);
        element.scope().$apply();

        expect(scope.$parent.arbeidsforhold.til).toBeUndefined();
        expect(form.til.$valid).toBe(false);
        expect(scope.arbeidsforhold.fra).toEqual(new Date(2011, 10, 10));

        form.til.$setViewValue(new Date(2012, 10, 10));
        element.scope().$apply();
        expect(scope.$parent.arbeidsforhold.til).toEqual(new Date(2012, 10, 10));
        expect(form.til.$valid).toBe(true);
        expect(scope.arbeidsforhold.fra).toEqual(new Date(2011, 10, 10));
        element.scope().$apply();
    });

    it('endringer på fra skal ikke påvirke tilTo', function () {
        scope.arbeidsforhold.fra = new Date(2011, 10, 10);
        element.scope().$apply();
        expect(scope.arbeidsforhold.tilTo).toEqual(new Date(2011, 10, 10));
        expect(form.tilTo.$valid).toBe(true);
    });

    it('endringer på fra2 skal ikke påvirke til', function () {
        scope.arbeidsforhold.fraTo = new Date(2011, 10, 10);
        element.scope().$apply();
        expect(scope.arbeidsforhold.til).toEqual(new Date(2011, 10, 10));
        expect(form.til.$valid).toBe(true);
    });

});
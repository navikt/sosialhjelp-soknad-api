describe('ArbeidsforholdCtrl', function () {

    beforeEach(
        module('app.services', 'nav.arbeidsforhold', function ($provide) {
            $provide.value("data", []);
        }));
    var scope, $httpBackend, ctrl, routeParams;

    beforeEach(inject(function ($rootScope, _$httpBackend_, $controller) {
        scope = $rootScope.$new();
        $httpBackend = _$httpBackend_;

        //$httpBackend.expectGET('/sendsoknad/rest/soknad/1/'\/.*/).

        $httpBackend.whenGET(/sendsoknad\/[^\/]*/).
            respond({"soknadId": 1, "gosysId": "Dagpenger", "brukerBehandlingId": "100000000",
                "fakta": {
                    "fornavn": {"soknadId": 1, "key": "fornavn", "value": "Ola"},
                    "mellomnavn": {"soknadId": 1, "key": "mellomnavn", "value": "Johan"},
                    "etternavn": {"soknadId": 1, "key": "etternavn", "value": "Nordmann"},
                    "fnr": {"soknadId": 1, "key": "fnr", "value": "***REMOVED***"},
                    "adresse": {"soknadId": 1, "key": "adresse", "value": "Waldemar Thranes Gt. 98B"},
                    "postnr": {"soknadId": 1, "key": "postnr", "value": "0175"},
                    "poststed": {"soknadId": 1, "key": "poststed", "value": "Oslo"}
                }
            });

        // TODO: result her? kontra backend
        $httpBackend.expectGET('/sendsoknad/rest/soknad/kodeverk/landliste').
            respond({result: ["Norge", "Sverige", "Danmark"]});

        routeParams = {};
        routeParams.soknadId = 1;

        ctrl = $controller('ArbeidsforholdCtrl', {
            $scope: scope,
            $routeParams: routeParams
        });

        $httpBackend.flush();
    }));

    /*
     it('arbeidsforhold skal skjules naar bruker velger aa avbryte', function() {
     scope.avbrytArbeidsforhold();
     expect(scope.arbeidsforholdaapen).toBe(false);
     });

     it('arbeidsforhold skal ha status aapen naar bruker har valgt nytt arbeidsforhold', function(){
     scope.nyttArbeidsforhold();
     expect(scope.arbeidsforholdaapen).toBe(true);
     });


     it('arbeidsforhold skal inneholde et element og arbeidsforhold skal skjules naar bruker har lagret et arbeidsforhold', function(){
     scope.arbeidsforhold = [];
     scope.arbeidsgiver = {};
     scope.arbeidsgiver.navn = "Hei Sjef Pizza";
     scope.arbeidsgiver.land = "Sannerland";

     scope.lagreArbeidsforhold();

     expect(scope.arbeidsforholdaapen).toBe(false)
     expect(scope.arbeidsforhold).toEqual([{"navn": "Hei Sjef Pizza", "land": "Sannerland"}]);
     });
     */

    it('skal vise checkbox når ingen arbeidsforhold er lagret', function () {
        scope.arbeidsforhold = [];

        expect(scope.harIkkeLagretArbeidsforhold()).toBe(true);
    });

    it('skal slette første arbeidsforhold naar man klikker slett', function () {
        var bekk = {"navn": "Bekk", "land": "UK", "varighetFra": "2013-10-14T22:00:00.000Z", "varighetTil": "2013-10-17T22:00:00.000Z", "sluttaarsak": "avskjediget"};
        var computas = {"navn": "Computas", "varighetFra": "2013-10-09T22:00:00.000Z", "varighetTil": "2013-10-24T22:00:00.000Z"};
        scope.arbeidsforhold = [bekk, computas];
        scope.slettArbeidsforhold(bekk);

        expect(scope.arbeidsforhold).toEqual([computas]);
    });

    it('skal slette andre arbeidsforhold naar man klikker slett', function () {
        var bekk = {"navn": "Bekk", "land": "UK", "varighetFra": "2013-10-14T22:00:00.000Z", "varighetTil": "2013-10-17T22:00:00.000Z", "sluttaarsak": "avskjediget"};
        var computas = {"navn": "Computas", "varighetFra": "2013-10-09T22:00:00.000Z", "varighetTil": "2013-10-24T22:00:00.000Z"};
        scope.arbeidsforhold = [bekk, computas];
        scope.slettArbeidsforhold(computas);

        expect(scope.arbeidsforhold).toEqual([bekk]);
    });

    it('skal ikke vise LeggTil-link dersom man allerede har nytt arbeidsforhold-skjema aapent', function () {
        scope.arbeidsforholdaapen = true;
        scope.posisjonForArbeidsforholdUnderRedigering = -1;

        expect(scope.kanLeggeTilArbeidsforhold()).toBe(false);

    });

    it('skal ikke vise LeggTil-link dersom man allerede har endre arbeidsforhold-skjema aapent', function () {
        scope.posisjonForArbeidsforholdUnderRedigering = 1;
        scope.arbeidsforholdaapen = false;

        expect(scope.kanLeggeTilArbeidsforhold()).toBe(false);

    });

    it('skal vise LeggTil-link dersom man ikke har et skjema aapent', function () {
        scope.posisjonForArbeidsforholdUnderRedigering = -1;
        scope.arbeidsforholdaapen = false;

        expect(scope.kanLeggeTilArbeidsforhold()).toBe(true);

    });
})


angular.module('templates-main', ['../views/dagpenger-singlepage.html', '../views/templates/adresse.html', '../views/templates/arbeidsforhold-nytt.html', '../views/templates/arbeidsforhold.html', '../views/templates/arbeidsforhold/arbeidsforhold_form.html', '../views/templates/arbeidsforhold/avskjediget-oppsummering.html', '../views/templates/arbeidsforhold/avskjediget.html', '../views/templates/arbeidsforhold/konkurs-oppsummering.html', '../views/templates/arbeidsforhold/konkurs.html', '../views/templates/arbeidsforhold/kontrakt-utgaatt-oppsummering.html', '../views/templates/arbeidsforhold/kontrakt-utgaatt.html', '../views/templates/arbeidsforhold/permittert-oppsummering.html', '../views/templates/arbeidsforhold/permittert.html', '../views/templates/arbeidsforhold/redusertarbeidstid-oppsummering.html', '../views/templates/arbeidsforhold/redusertarbeidstid.html', '../views/templates/arbeidsforhold/sagt-opp-av-arbeidsgiver-oppsummering.html', '../views/templates/arbeidsforhold/sagt-opp-av-arbeidsgiver.html', '../views/templates/arbeidsforhold/sagt-opp-selv-oppsummering.html', '../views/templates/arbeidsforhold/sagt-opp-selv.html', '../views/templates/arbeidsforhold/utdanning_form.html', '../views/templates/avbryt.html', '../views/templates/barnetillegg-nyttbarn.html', '../views/templates/barnetillegg.html', '../views/templates/bekreftelse.html', '../views/templates/egen-naering.html', '../views/templates/feilside.html', '../views/templates/ferdigstilt.html', '../views/templates/fortsettSenere.html', '../views/templates/fritekst.html', '../views/templates/gjenoppta/skjema-ferdig.html', '../views/templates/gjenoppta/skjema-sendt.html', '../views/templates/gjenoppta/skjema-under-arbeid.html', '../views/templates/gjenoppta/skjema-validert.html', '../views/templates/ikkekvalifisert.html', '../views/templates/informasjonsside.html', '../views/templates/kvittering-fortsettsenere.html', '../views/templates/kvittering-innsendt.html', '../views/templates/opplasting.html', '../views/templates/oppsummering.html', '../views/templates/personalia.html', '../views/templates/reellarbeidssoker/reell-arbeidssoker.html', '../views/templates/soknadSlettet.html', '../views/templates/soknadliste.html', '../views/templates/utdanning/utdanning.html', '../views/templates/utdanning/utdanningKortvarigFlereTemplate.html', '../views/templates/utdanning/utdanningKortvarigTemplate.html', '../views/templates/utdanning/utdanningKveldTemplate.html', '../views/templates/utdanning/utdanningNorskTemplate.html', '../views/templates/utdanningsinformasjon-template.html', '../views/templates/vedlegg.html', '../views/templates/verneplikt.html', '../views/templates/visvedlegg.html', '../views/templates/ytelser.html', '../js/app/directives/bildenavigering/bildenavigeringTemplateLiten.html', '../js/app/directives/bildenavigering/bildenavigeringTemplateStor.html', '../js/app/directives/dagpenger/arbeidsforholdformTemplate.html', '../js/app/directives/feilmeldinger/feilmeldingerTemplate.html', '../js/app/directives/feilmeldinger/stickyFeilmeldingTemplate.html', '../js/app/directives/markup/navinfoboksTemplate.html', '../js/app/directives/markup/panelStandardBelystTemplate.html', '../js/app/directives/markup/vedlegginfoboksTemplate.html', '../js/app/directives/sporsmalferdig/spmblokkFerdigTemplate.html', '../js/app/directives/stegindikator/stegIndikatorTemplate.html', '../js/app/directives/stickybunn/stickyBunnTemplate.html', '../js/common/directives/accordion/accordionGroupTemplate.html', '../js/common/directives/accordion/accordionTemplate.html', '../js/common/directives/booleanradio/booleanradioTemplate.html', '../js/common/directives/datepicker/doubleDatepickerTemplate.html', '../js/common/directives/datepicker/singleDatepickerTemplate.html', '../js/common/directives/hjelpetekst/hjelpetekstTemplate.html', '../js/common/directives/navinput/navbuttonspinnerTemplate.html', '../js/common/directives/navinput/navcheckboxTemplate.html', '../js/common/directives/navinput/navorgnrfeltTemplate.html', '../js/common/directives/navinput/navradioTemplate.html', '../js/common/directives/navinput/navtekstTemplate.html', '../js/common/directives/navtextarea/navtextareaObligatoriskTemplate.html', '../js/common/directives/navtextarea/navtextareaTemplate.html', '../js/common/directives/select/selectTemplate.html', '../js/common/directives/tittel/tittelTemplate.html']);

angular.module("../views/dagpenger-singlepage.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/dagpenger-singlepage.html",
    "<div data-nav-tittel=\"skjema.tittel\"></div>\n" +
    "<div data-stegindikator data-steg-liste=\"veiledning, skjema, vedlegg, sendInn\" data-aktiv-index=\"1\"></div>\n" +
    "<div class=\"dagpenger\" data-ng-form=\"dagpengerForm\" data-ng-cloak data-tab-autoscroll  >\n" +
    "    <div data-ng-controller=\"DagpengerCtrl\" data-scroll-tilbake-directive data-apne-bolker>\n" +
    "        <div data-sticky-feilmelding></div>\n" +
    "        <div class=\"rad soknad\" data-sidetittel=\"sidetittel.skjema\">\n" +
    "            <section class=\"sak-hel\">\n" +
    "                <div class=\"panel-standard-belyst skjema\">\n" +
    "                    <div data-accordion data-close-others=\"false\">\n" +
    "                        <div data-accordion-group data-ng-repeat=\"gruppe in grupper\" class=\"spm-blokk\"\n" +
    "                             id=\"{{gruppe.id}}\" data-is-open=\"gruppe.apen\" data-heading=\"{{ gruppe.tittel | cmstekst}}\" data-sjekk-validert=\"gruppe.skalSettesTilValidVedForsteApning\">\n" +
    "                            <div data-div data-ng-if=\"gruppe.apen || gruppe.validering\" data-ng-include=\"gruppe.template\"></div>\n" +
    "                        </div>\n" +
    "                    </div>\n" +
    "                </div>\n" +
    "            </section>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"rad ferdig-skjema\">\n" +
    "            <div class=\"begrensning\">\n" +
    "                <section class=\"sak-hel\">\n" +
    "                    <a href=\"#/vedlegg\" class=\"knapp-hoved\" data-cmstekster=\"skjema.ferdig\" role=\"button\" data-fremdriftsindikator=\"grå\" data-valider-skjema data-ng-click=\"validerSkjema($event)\"></a>\n" +
    "                </section>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "<div data-sist-lagret data-navtilbakelenke=\"ingenlenke\"></div>\n" +
    "");
}]);

angular.module("../views/templates/adresse.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/adresse.html",
    "<div class=\"adresse\" data-ng-controller=\"AdresseCtrl\">\n" +
    "    <div data-ng-if=\"harGjeldendeAdresse()\">\n" +
    "        <span class=\"adressetype\">{{ gjeldendeAdresseTypeLabel | cmstekst }}</span>\n" +
    "        <span data-ng-bind-html=\"formattertGjeldendeAdresse\"></span>\n" +
    "    </div>\n" +
    "\n" +
    "    <div data-ng-if=\"harSekundarAdresse()\">\n" +
    "        <span class=\"adressetype\">{{ sekundarAdresseTypeLabel | cmstekst }}</span>\n" +
    "        <span data-ng-bind-html=\"formattertSekundarAdresse\"></span>\n" +
    "    </div>\n" +
    "</div>");
}]);

angular.module("../views/templates/arbeidsforhold-nytt.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/arbeidsforhold-nytt.html",
    "<div class=\"modalBoks\" data-ng-controller=\"ArbeidsforholdNyttCtrl\">\n" +
    "    <div data-ng-form=\"nyttArbeidsforholdForm\" class=\"vertikal\">\n" +
    "        <div class=\"begrensning\">\n" +
    "            <div class=\"sak-totredel\">\n" +
    "                <div class=\"arbeidsforhold-spm spm-boks panel-standard-belyst\">\n" +
    "                    <a href=\"#/soknad\" class=\"lukk\"> </a>\n" +
    "\n" +
    "                    <div data-form-errors></div>\n" +
    "                    <h2 class=\"stor-strek\">\n" +
    "                        <span data-cmstekster=\"arbeidsforhold.nyttarbeidsforhold.tittel\"></span>\n" +
    "                    </h2>\n" +
    "\n" +
    "                    <div data-nav-faktum=\"arbeidsforhold\" data-ikke-auto-lagre=\"true\">\n" +
    "                        <div data-navtekst\n" +
    "                             data-navconfig\n" +
    "                             data-nav-faktum-property=\"arbeidsgivernavn\"\n" +
    "                             data-navlabel=\"arbeidsforhold.arbeidsgiver.navn\"\n" +
    "                             data-navfeilmelding=\"'arbeidsforhold.arbeidsgiver.navn.feilmelding'\"\n" +
    "                             data-ng-required></div>\n" +
    "\n" +
    "                        <div class=\"land\">\n" +
    "                            <div data-nav-select\n" +
    "                                 data-navconfig\n" +
    "                                 data-nav-faktum-property=\"land\"\n" +
    "                                 data-label=\"arbeidsforhold.arbeidsgiver.land\"\n" +
    "                                 data-options=\"land.result\"\n" +
    "                                 data-er-required=\"true\"\n" +
    "                                 data-ikke-auto-lagre=\"true\"\n" +
    "                                 data-default-value=\"arbeidsforhold.arbeidsgiver.landDefault\"\n" +
    "                                 data-required-feilmelding=\"arbeidsforhold.arbeidsgiver.land.feilmelding\"\n" +
    "                                 data-ugyldig-feilmelding=\"arbeidsforhold.arbeidsgiver.land.ugyldig.feilmelding\">\n" +
    "                            </div>\n" +
    "                        </div>\n" +
    "                        <div data-ng-show=\"arbeidsforhold.properties.eosland == 'true'\">\n" +
    "                            <div class=\"ekstra-spm-boks\" data-vedlegginfoboks>\n" +
    "                                <ul>\n" +
    "                                    <li>\n" +
    "                                        {{ 'arbeidsforhold.arbeidsgiver.land.norge.vedlegginformasjon' | cmstekst}}\n" +
    "                                    </li>\n" +
    "                                </ul>\n" +
    "                            </div>\n" +
    "                        </div>\n" +
    "\n" +
    "                        <div class=\"spm boolean form-linje\">\n" +
    "                            <h4 class=\"spm-sporsmal\">{{ 'arbeidsforhold.arbeidsgiver.sluttaarsak.informasjon' |\n" +
    "                                cmstekst }}</h4>\n" +
    "\n" +
    "                            <div class=\"nav-radio-knapp\">\n" +
    "                                <input class=\"sendsoknad-radio\"\n" +
    "                                       id=\"radio5\"\n" +
    "                                       name=\"arbeidsforhold.sluttaarsak.radio\"\n" +
    "                                       type=\"radio\"\n" +
    "                                       value=\"Sagt opp av arbeidsgiver\"\n" +
    "                                       data-ng-model=\"faktum.properties.type\"\n" +
    "                                       data-ng-required=\"true\"\n" +
    "                                       data-click-validate\n" +
    "                                       data-error-messages=\"'arbeidsforhold.arbeidsgiver.sluttaarsak.feilmelding'\">\n" +
    "                                <label for='radio5'\n" +
    "                                       data-cmstekster=\"arbeidsforhold.sluttaarsak.radio.sagtOppAvArbeidsgiver\"></label>\n" +
    "                            </div>\n" +
    "\n" +
    "                            <div class=\"nav-radio-knapp\">\n" +
    "                                <input class=\"sendsoknad-radio\"\n" +
    "                                       id=\"radio7\"\n" +
    "                                       type=\"radio\"\n" +
    "                                       value=\"Permittert\"\n" +
    "                                       name=\"arbeidsforhold.sluttaarsak.radio\"\n" +
    "                                       data-ng-model=\"faktum.properties.type\"\n" +
    "                                       data-ng-required=\"true\"\n" +
    "                                       data-click-validate\n" +
    "                                       data-error-messages=\"'arbeidsforhold.arbeidsgiver.sluttaarsak.feilmelding'\">\n" +
    "                                <label for='radio7'\n" +
    "                                       data-cmstekster=\"arbeidsforhold.sluttaarsak.radio.permittert\"></label>\n" +
    "                            </div>\n" +
    "\n" +
    "                            <div class=\"nav-radio-knapp\">\n" +
    "                                <input class=\"sendsoknad-radio\"\n" +
    "                                       id=\"radio1\"\n" +
    "                                       type=\"radio\"\n" +
    "                                       value=\"Kontrakt utgått\"\n" +
    "                                       name=\"arbeidsforhold.sluttaarsak.radio\"\n" +
    "                                       data-ng-model=\"faktum.properties.type\"\n" +
    "                                       data-click-validate\n" +
    "                                       data-ng-required=\"true\"\n" +
    "                                       data-error-messages=\"'arbeidsforhold.arbeidsgiver.sluttaarsak.feilmelding'\">\n" +
    "                                <label for='radio1'\n" +
    "                                       data-cmstekster=\"arbeidsforhold.sluttaarsak.radio.kontraktutgaatt\"></label>\n" +
    "                            </div>\n" +
    "\n" +
    "                            <div class=\"nav-radio-knapp\">\n" +
    "                                <input class=\"sendsoknad-radio\"\n" +
    "                                       id=\"radio6\"\n" +
    "                                       type=\"radio\"\n" +
    "                                       value=\"Sagt opp selv\"\n" +
    "                                       name=\"arbeidsforhold.sluttaarsak.radio\"\n" +
    "                                       data-ng-model=faktum.properties.type\n" +
    "                                       data-ng-required=\"true\"\n" +
    "                                       data-click-validate\n" +
    "                                       data-error-messages=\"'arbeidsforhold.arbeidsgiver.sluttaarsak.feilmelding'\">\n" +
    "                                <label for='radio6'\n" +
    "                                       data-cmstekster=\"arbeidsforhold.sluttaarsak.radio.sagtOppSelv\"></label>\n" +
    "                            </div>\n" +
    "\n" +
    "                            <div class=\"nav-radio-knapp\">\n" +
    "                                <input class=\"sendsoknad-radio\"\n" +
    "                                       id=\"radio3\"\n" +
    "                                       type=\"radio\"\n" +
    "                                       value=\"Redusert arbeidstid\"\n" +
    "                                       name=\"arbeidsforhold.sluttaarsak.radio\"\n" +
    "                                       data-ng-model=\"faktum.properties.type\"\n" +
    "                                       data-ng-required=\"true\"\n" +
    "                                       data-click-validate\n" +
    "                                       data-error-messages=\"'arbeidsforhold.arbeidsgiver.sluttaarsak.feilmelding'\">\n" +
    "                                <label for='radio3'\n" +
    "                                       data-cmstekster=\"arbeidsforhold.sluttaarsak.radio.redusertArbeidstid\"></label>\n" +
    "                            </div>\n" +
    "\n" +
    "                            <div class=\"nav-radio-knapp\">\n" +
    "                                <input class=\"sendsoknad-radio\"\n" +
    "                                       id=\"radio4\"\n" +
    "                                       type=\"radio\"\n" +
    "                                       value=\"Arbeidsgiver er konkurs\"\n" +
    "                                       name=\"arbeidsforhold.sluttaarsak.radio\"\n" +
    "                                       data-ng-model=\"faktum.properties.type\"\n" +
    "                                       data-ng-required=\"true\"\n" +
    "                                       data-click-validate\n" +
    "                                       data-error-messages=\"'arbeidsforhold.arbeidsgiver.sluttaarsak.feilmelding'\">\n" +
    "                                <label for='radio4'\n" +
    "                                       data-cmstekster=\"arbeidsforhold.sluttaarsak.radio.arbeidsgiverErKonkurs\"></label>\n" +
    "                            </div>\n" +
    "\n" +
    "                            <div class=\"nav-radio-knapp\">\n" +
    "                                <input class=\"sendsoknad-radio\"\n" +
    "                                       id=\"radio2\"\n" +
    "                                       type=\"radio\"\n" +
    "                                       value=\"Avskjediget\"\n" +
    "                                       name=\"arbeidsforhold.sluttaarsak.radio\"\n" +
    "                                       data-ng-model=\"faktum.properties.type\"\n" +
    "                                       data-ng-required=\"true\"\n" +
    "                                       data-click-validate\n" +
    "                                       data-error-messages=\"'arbeidsforhold.arbeidsgiver.sluttaarsak.feilmelding'\">\n" +
    "                                <label for='radio2'\n" +
    "                                       data-cmstekster=\"arbeidsforhold.sluttaarsak.radio.avskjediget\"></label>\n" +
    "                            </div>\n" +
    "\n" +
    "                            <span class=\"melding\"></span>\n" +
    "                        </div>\n" +
    "\n" +
    "                        <div data-ng-if=\"faktum.properties.type\">\n" +
    "                            <div data-ng-include=\"templates[faktum.properties.type].url\"></div>\n" +
    "                        </div>\n" +
    "                        <div class=\"knapper-opprett\">\n" +
    "                            <input class=\"knapp-hoved-liten\" name=\"lagre\" type=\"submit\"\n" +
    "                                   data-cmstekster=\"arbeidsforhold.nyttarbeidsforhold.lagre\"\n" +
    "                                   data-ng-click=\"lagreArbeidsforhold(nyttArbeidsforholdForm);\">\n" +
    "                            <a href=\"#/soknad\" data-cmstekster=\"arbeidsforhold.nyttarbeidsforhold.avbryt\"></a>\n" +
    "                        </div>\n" +
    "                    </div>\n" +
    "\n" +
    "                </div>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>");
}]);

angular.module("../views/templates/arbeidsforhold.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/arbeidsforhold.html",
    "<div class=\"skjemaramme\">\n" +
    "    <div data-ng-form=\"arbeidsforholdForm\" class=\"skjemainnhold\" data-ng-controller=\"ArbeidsforholdCtrl\">\n" +
    "        <div data-form-errors></div>\n" +
    "        <p class=\"arbeidsforhold-informasjonstekst\" data-cmstekster=\"arbeidsforhold.informasjon\"></p>\n" +
    "\n" +
    "        <div class=\"form-linje boolean\">\n" +
    "\n" +
    "            <div data-navradio\n" +
    "                 data-value=\"fastArbeidstid\"\n" +
    "                 data-navconfig\n" +
    "                 data-nav-faktum=\"arbeidstilstand\"\n" +
    "                 data-navlabel=\"arbeidsforhold.arbeidstilstand.fastarbeidstid\"\n" +
    "                 data-navfeilmelding=\"arbeidsforhold.arbeidstilstand.feilmelding\">\n" +
    "            </div>\n" +
    "\n" +
    "            <div data-navradio\n" +
    "                 data-value=\"varierendeArbeidstid\"\n" +
    "                 data-navconfig\n" +
    "                 data-nav-faktum=\"arbeidstilstand\"\n" +
    "                 data-navlabel=\"arbeidsforhold.arbeidstilstand.varierendearbeidstid\"\n" +
    "                 data-navfeilmelding=\"arbeidsforhold.arbeidstilstand.feilmelding\">\n" +
    "            </div>\n" +
    "\n" +
    "            <div data-navradio\n" +
    "                 data-value=\"harIkkeJobbet\"\n" +
    "                 data-navconfig\n" +
    "                 data-nav-faktum=\"arbeidstilstand\"\n" +
    "                 data-navlabel=\"arbeidsforhold.arbeidstilstand.harikkejobbet\"\n" +
    "                 data-navfeilmelding=\"arbeidsforhold.arbeidstilstand.feilmelding\">\n" +
    "            </div>\n" +
    "\n" +
    "            <span class=\"melding\"> </span>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-ng-if=\"hvisHarIkkeJobbet()\">\n" +
    "            <div data-navinfoboks>\n" +
    "                <p>{{ 'arbeidsforhold.arbeidstilstand.harikkejobbet.informasjonstekst.del1' | cmstekst }}</p>\n" +
    "                <p>{{ 'arbeidsforhold.arbeidstilstand.harikkejobbet.informasjonstekst.del2' | cmstekst }}</p>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-ng-if=\"hvisHarJobbetVarierende()\">\n" +
    "            <div data-navinfoboks>\n" +
    "                <span data-cmstekster=\"arbeidsforhold.arbeidstilstand.varierendearbeidstid.informasjonstekst\"></span>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "        <div data-ng-if=\"hvisHarJobbetFast()\">\n" +
    "            <div data-navinfoboks>\n" +
    "                <span data-cmstekster=\"arbeidsforhold.arbeidstilstand.fastarbeidstid.informasjonstekst\"></span>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"spm-boks vertikal\" data-ng-repeat=\"af in arbeidsliste\" data-ng-class=\"{'last': $last}\">\n" +
    "            <div id=\"arbeidsforhold{{af.arbeidsforhold.faktumId}}\">\n" +
    "                <a href=\"#\" class=\"lukk\" data-ng-click=\"slettArbeidsforhold(af, $index, $event)\" data-fokus-slettmoduler=\"arbeidsforhold\" > </a>\n" +
    "                <span class=\"robust\">{{ af.arbeidsforhold.properties.arbeidsgivernavn }}</span>\n" +
    "                <span class=\"robust\">, </span>  \n" +
    "                <span class=\"robust\">{{ finnLandFraLandkode(af.arbeidsforhold.properties.land) }}</span>\n" +
    "\n" +
    "                <div class=\"arbeidsforhold-land-sluttaarsak-blokk\">\n" +
    "                    <div class=\"arbeidsgiver-oppsummering-sluttaarsaker\">\n" +
    "                        <div data-ng-include=\"templates[af.sluttaarsak.properties.type].oppsummeringsurl\"></div>\n" +
    "                    </div>\n" +
    "                </div>\n" +
    "\n" +
    "                <div class=\"arbeidsforhold-oppsummering-endre-slett\">\n" +
    "                    <ul class=\"liste-vannrett\">\n" +
    "                        <li>\n" +
    "                            <a href data-ng-click=\"slettArbeidsforhold(af, $index, $event)\"\n" +
    "                               data-cmstekster=\"arbeidsforhold.slettarbeidsforhold\" data-fokus-slettmoduler=\"arbeidsforhold\" ></a>\n" +
    "                        </li>\n" +
    "                        <li>\n" +
    "                            <a href data-ng-click=\"endreArbeidsforhold(af, $index, $event)\"\n" +
    "                               data-cmstekster=\"arbeidsforhold.endrearbeidsforhold\"></a>\n" +
    "                        </li>\n" +
    "                    </ul>\n" +
    "                </div>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"knapper-arbeidsforhold\">\n" +
    "            <div id=\"legg-til\" class=\"form-linje knapp ikke-fadebakgrunn\" data-ng-if=\"harSvart()\" data-ng-class=\"{feil: skalViseFeil()}\">\n" +
    "                <div data-ng-if=\"hvisHarJobbet()\">\n" +
    "                    <input type=\"hidden\"\n" +
    "                           data-ng-model=\"harLagretArbeidsforhold\"\n" +
    "                           data-nav-faktum=\"harLagretArbeidsforhold\"\n" +
    "                           data-ng-required=\"true\"\n" +
    "                           data-error-messages=\"'arbeidsforhold.arbeidsforhold.required'\"\n" +
    "                           >\n" +
    "                </div>\n" +
    "\n" +
    "                <button class=\"knapp-leggtil-liten\" data-ng-click=\"nyttArbeidsforhold($event, arbeidsforholdForm)\"\n" +
    "                        data-cmstekster=\"arbeidsforhold.nyttarbeidsforhold\" role=\"button\"></button>\n" +
    "                <span class=\"melding\" data-cmstekster=\"arbeidsforhold.arbeidsforhold.required\"></span>\n" +
    "            </div>\n" +
    "\n" +
    "            <div data-spmblokkferdig\n" +
    "                 data-submit-method=\"validerArbeidsforhold(true)\"></div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "\n" +
    "\n" +
    "");
}]);

angular.module("../views/templates/arbeidsforhold/arbeidsforhold_form.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/arbeidsforhold/arbeidsforhold_form.html",
    "<form name=\"endreArbeidsforholdForm\" data-ng-submit=\"aksjon()\">\n" +
    "\n" +
    "	<div class=\"varighet\">\n" +
    "		<label>Fra:</label>\n" +
    "		<input type=\"text\" name=\"varighetFra\" data-ui-date data-ui-date-format ng-model=\"af.varighetFra\" data-ng-change=\"validateTilFraDato(af)\" required>\n" +
    "		<label>Til:</label>\n" +
    "		<span data-ng-show=\"datoError\" style=\"color:red\">Tildato må være etter fradato</span>\n" +
    "		<input type=\"text\" name=\"varighetTil\" data-ui-date data-ui-date-format ng-model=\"af.varighetTil\" data-ng-change=\"validateTilFraDato(af)\" required>\n" +
    "	</div>\n" +
    "\n" +
    "	<input data-cmstekster=\"arbeidsforhold.lagre\" type=\"submit\" id=\"arbeidsgiverLagre\"/>\n" +
    "	<a href data-cmstekster=\"arbeidsforhold.avbryt\" data-ng-click=\"avbrytEndringAvArbeidsforhold()\"></a>\n" +
    "</form>\n" +
    "");
}]);

angular.module("../views/templates/arbeidsforhold/avskjediget-oppsummering.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/arbeidsforhold/avskjediget-oppsummering.html",
    "<div class=\"varighet\">\n" +
    "    <span data-ng-bind=\"af.sluttaarsak.properties.datofra | date:'dd.MM.yyyy' | norskdato\"></span>\n" +
    "    <span data-ng-if=\"af.sluttaarsak.properties.datotil\" data-cmstekster=\"dato.til\"></span>\n" +
    "    <span data-ng-if=\"af.sluttaarsak.properties.datotil\" data-ng-bind=\"af.sluttaarsak.properties.datotil | date:'dd.MM.yyyy' | norskdato\"></span>\n" +
    "</div>\n" +
    "\n" +
    "<ul>\n" +
    "    <li>\n" +
    "        <p data-ng-bind=\"af.sluttaarsak.properties.type\"></p>\n" +
    "    </li>\n" +
    "    <li>\n" +
    "        <span data-cmstekster=\"arbeidsforhold.sluttaarsak.avskjedigetgrunn.sporsmal\"></span>\n" +
    "        <p data-ng-bind=\"af.sluttaarsak.properties.avskjedigetGrunn\"></p>\n" +
    "    </li>\n" +
    "</ul>\n" +
    "\n" +
    "\n" +
    "<div data-vedlegginfoboks>\n" +
    "    <ul>\n" +
    "        <li>\n" +
    "            <span data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.1\"></span>\n" +
    "        </li>\n" +
    "        <li>\n" +
    "            <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2\"></p>\n" +
    "            <a href=\"{{sluttaarsakUrl}}\" data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2.lenketekst\">\n" +
    "            </a>\n" +
    "        </li>\n" +
    "    </ul>\n" +
    "</div>\n" +
    "\n" +
    "");
}]);

angular.module("../views/templates/arbeidsforhold/avskjediget.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/arbeidsforhold/avskjediget.html",
    "<div>\n" +
    "    <div data-navinfoboks>\n" +
    "        <p class=\"sluttaarsak-informasjon\" cmstekster=\"arbeidsforhold.sluttaarsak.avskjediget.informasjon.1\"></p>\n" +
    "        <p class=\"sluttaarsak-informasjon\" cmstekster=\"arbeidsforhold.sluttaarsak.avskjediget.informasjon.2\"></p>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"spm\" data-nav-faktum=\"sluttaarsak\"\n" +
    "         data-ikke-auto-lagre=\"true\">\n" +
    "\n" +
    "        <div class=\"varighet form-linje\">\n" +
    "            <div data-nav-dato\n" +
    "                 data-ng-model=\"sluttaarsak.properties.datofra\"\n" +
    "                 data-label=\"arbeidsforhold.arbeidsgiver.varighet.fra\"\n" +
    "                 data-er-required=\"true\"\n" +
    "                 data-required-error-message=\"arbeidsforhold.arbeidsgiver.varighet.fra.feilmelding\">\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"varighet form-linje\">\n" +
    "            <div data-nav-dato\n" +
    "                 data-ng-model=\"sluttaarsak.properties.datotil\"\n" +
    "                 data-label=\"arbeidsforhold.arbeidsgiver.varighet.til\"\n" +
    "                 data-er-required=\"true\"\n" +
    "                 data-er-fremtidigdato-tillatt=\"true\"\n" +
    "                 data-required-error-message=\"arbeidsforhold.arbeidsgiver.varighet.til.feilmelding\">\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-navtextarea\n" +
    "             data-navconfig\n" +
    "             data-nav-faktum-property=\"avskjedigetGrunn\"\n" +
    "             data-nokkel=\"arbeidsforhold.sluttaarsak.avskjedigetgrunn\"\n" +
    "             data-maxlengde=\"500\"\n" +
    "             data-feilmelding=\"arbeidsforhold.sluttaarsak.avskjedigetgrunn.feilmelding\"\n" +
    "             data-obligatorisk=\"true\"></div>\n" +
    "\n" +
    "        <div class=\"ekstra-spm-boks\" data-vedlegginfoboks>\n" +
    "            <ul>\n" +
    "                <li>\n" +
    "                    <span data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.1\"></span>\n" +
    "                </li>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2\"></p>\n" +
    "                    <a href=\"{{sluttaarsakUrl}}\" data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2.lenketekst\">\n" +
    "                    </a>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../views/templates/arbeidsforhold/konkurs-oppsummering.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/arbeidsforhold/konkurs-oppsummering.html",
    "<div class=\"varighet\">\n" +
    "    <span data-ng-bind=\"af.sluttaarsak.properties.datofra | date:'dd.MM.yyyy' | norskdato\"></span>\n" +
    "    <span data-ng-if=\"af.sluttaarsak.properties.datotil\" data-cmstekster=\"dato.til\"></span>\n" +
    "    <span data-ng-if=\"af.sluttaarsak.properties.datotil\" data-ng-bind=\"af.sluttaarsak.properties.datotil | date:'dd.MM.yyyy' | norskdato\"></span>\n" +
    "</div>\n" +
    "\n" +
    "<ul>\n" +
    "    <li>\n" +
    "        <p data-ng-bind=\"af.sluttaarsak.properties.type\"></p>\n" +
    "    </li>\n" +
    "    <li>\n" +
    "        <span data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.bostyrersnavn.sporsmal\"></span>\n" +
    "        <p data-ng-bind=\"af.sluttaarsak.properties.bostyrersnavn\"></p>\n" +
    "    </li>\n" +
    "    <li>\n" +
    "        <span data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnsgaranti.sporsmal\"></span>\n" +
    "        <p data-ng-show=\"af.sluttaarsak.properties.lonnsgaranti == 'JaHarSokt'\" \n" +
    "            data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnsgaranti.jaharsokt\"></p>\n" +
    "        <p data-ng-show=\"af.sluttaarsak.properties.lonnsgaranti == 'JaSkalSoke'\" \n" +
    "            data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnsgaranti.jaskalsoke\"></p>\n" +
    "        <p data-ng-show=\"af.sluttaarsak.properties.lonnsgaranti == 'Nei'\" \n" +
    "            data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnsgaranti.nei\"></p>\n" +
    "    </li>\n" +
    "    <li data-ng-show=\"af.sluttaarsak.properties.lonnsgaranti == 'JaHarSokt'\">\n" +
    "        <span data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnsgarantidekker.sporsmal\"></span>\n" +
    "        <p data-ng-show=\"af.sluttaarsak.properties.lonnsgarantidekker == 'Nei'\" \n" +
    "            data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnsgarantidekker.nei\"></p>\n" +
    "        <p data-ng-show=\"af.sluttaarsak.properties.lonnsgarantidekker == 'Ja'\" \n" +
    "            data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnsgarantidekker.ja\"></p>\n" +
    "        <p data-ng-show=\"af.sluttaarsak.properties.lonnsgarantidekker == 'Vet ikke'\" \n" +
    "            data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnsgarantidekker.vetikke\"></p>\n" +
    "    </li>\n" +
    "    <li>\n" +
    "        <span data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnkonkursmaaned.sporsmal\"></span>\n" +
    "        <p data-ng-show=\"af.sluttaarsak.properties.lonnkonkursmaaned == 'true'\" \n" +
    "            data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnkonkursmaaned.true\"></p>\n" +
    "        <p data-ng-show=\"af.sluttaarsak.properties.lonnkonkursmaaned == 'false'\" \n" +
    "            data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnkonkursmaaned.false\"></p>\n" +
    "    </li>\n" +
    "</ul>\n" +
    "<div data-vedlegginfoboks>\n" +
    "    <ul>\n" +
    "        <li>\n" +
    "            <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.1\"></p>\n" +
    "        </li>\n" +
    "        <li>\n" +
    "            <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2\"></p>\n" +
    "            <a href=\"{{sluttaarsakUrl}}\" data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2.lenketekst\">\n" +
    "            </a>\n" +
    "        </li>\n" +
    "        <li>\n" +
    "            <p data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.vedlegg.liste.3\"></p>\n" +
    "            <a href=\"{{lonnskravSkjema}}\" data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.vedlegg.liste.3.lenketekst\">\n" +
    "            </a>\n" +
    "        </li>\n" +
    "    </ul>\n" +
    "</div>");
}]);

angular.module("../views/templates/arbeidsforhold/konkurs.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/arbeidsforhold/konkurs.html",
    "<div>\n" +
    "    <div class=\"spm\" data-nav-faktum=\"sluttaarsak\"\n" +
    "         data-ikke-auto-lagre=\"true\">\n" +
    "\n" +
    "        <div class=\"varighet form-linje\">\n" +
    "            <div data-nav-dato\n" +
    "                 data-ng-model=\"sluttaarsak.properties.datofra\"\n" +
    "                 data-label=\"arbeidsforhold.arbeidsgiver.varighet.fra\"\n" +
    "                 data-er-required=\"true\"\n" +
    "                 data-required-error-message=\"arbeidsforhold.arbeidsgiver.varighet.fra.feilmelding\">\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"varighet form-linje\">\n" +
    "            <div data-nav-dato\n" +
    "                 data-ng-model=\"sluttaarsak.properties.konkursdato\"\n" +
    "                 data-label=\"arbeidsforhold.sluttaarsak.konkurs.konkursdato.sporsmal\"\n" +
    "                 data-er-required=\"true\"\n" +
    "                 data-er-fremtidigdato-tillatt=\"true\"\n" +
    "                 data-required-error-message=\"arbeidsforhold.sluttaarsak.konkurs.konkursdato.feilmelding\">\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-navtextarea\n" +
    "             data-navconfig\n" +
    "             data-nav-faktum-property=\"bostyrersnavn\"\n" +
    "             data-nokkel=\"arbeidsforhold.sluttaarsak.konkurs.bostyrersnavn\"\n" +
    "             data-maxlengde=\"500\"\n" +
    "             data-feilmelding=\"arbeidsforhold.sluttaarsak.konkurs.bostyrersnavn.feilmelding\"\n" +
    "             data-obligatorisk=\"true\"></div>\n" +
    "\n" +
    "        <div data-navinfoboks>\n" +
    "            <p class=\"sluttaarsak-informasjon\"\n" +
    "               data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnsgaranti.informasjon\"></p>\n" +
    "        </div>\n" +
    "\n" +
    "\n" +
    "        <div class=\"form-linje spm boolean\">\n" +
    "            <h4 class=\"spm-sporsmal\" data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnsgaranti.sporsmal\"></h4>\n" +
    "\n" +
    "            <div class=\"nav-radio-knapp\">\n" +
    "                <input class=\"sendsoknad-radio\"\n" +
    "                       id=\"lonnsgaranti1\"\n" +
    "                       type=\"radio\"\n" +
    "                       value=\"JaHarSokt\"\n" +
    "                       name=\"lonnsgaranti\"\n" +
    "                       data-ng-model=\"sluttaarsak.properties.lonnsgaranti\"\n" +
    "                       data-click-validate\n" +
    "                       data-ng-required=\"true\"\n" +
    "                       data-error-messages=\"'arbeidsforhold.sluttaarsak.konkurs.lonnsgaranti.feilmelding'\">\n" +
    "                <label for='lonnsgaranti1'\n" +
    "                       data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnsgaranti.jaharsokt\"></label>\n" +
    "            </div>\n" +
    "\n" +
    "            <div class=\"ekstra-spm-boks\" data-ng-if=\"sluttaarsak.properties.lonnsgaranti == 'JaHarSokt'\">\n" +
    "                <div class=\"form-linje boolean\">\n" +
    "                    <h4 class=\"spm-sporsmal\" data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnsgarantidekker.sporsmal\"></h4>\n" +
    "\n" +
    "                    <div class=\"nav-radio-knapp\">\n" +
    "                        <input class=\"sendsoknad-radio\"\n" +
    "                               id=\"lonnsgarantidekker1\"\n" +
    "                               type=\"radio\"\n" +
    "                               value=\"Nei\"\n" +
    "                               name=\"lonnsgarantidekker\"\n" +
    "                               data-ng-model=\"sluttaarsak.properties.lonnsgarantidekker\"\n" +
    "                               data-click-validate\n" +
    "                               data-ng-required=\"sluttaarsak.properties.lonnsgaranti == 'JaHarSokt'\"\n" +
    "                               data-error-messages=\"'arbeidsforhold.sluttaarsak.konkurs.lonnsgarantidekker.feilmelding'\">\n" +
    "                        <label for='lonnsgarantidekker1'\n" +
    "                               data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnsgarantidekker.nei\"></label>\n" +
    "                    </div>\n" +
    "                    <div class=\"nav-radio-knapp\">\n" +
    "                        <input class=\"sendsoknad-radio\"\n" +
    "                               id=\"lonnsgarantidekker2\"\n" +
    "                               type=\"radio\"\n" +
    "                               value=\"Ja\"\n" +
    "                               name=\"lonnsgarantidekker\"\n" +
    "                               data-ng-model=\"sluttaarsak.properties.lonnsgarantidekker\"\n" +
    "                               data-click-validate\n" +
    "                               data-ng-required=\"sluttaarsak.properties.lonnsgaranti == 'JaHarSokt'\"\n" +
    "                               data-error-messages=\"'arbeidsforhold.sluttaarsak.konkurs.lonnsgarantidekker.feilmelding'\">\n" +
    "                        <label for='lonnsgarantidekker2'\n" +
    "                               data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnsgarantidekker.ja\"></label>\n" +
    "                    </div>\n" +
    "                    <div class=\"nav-radio-knapp\">\n" +
    "                        <input class=\"sendsoknad-radio\"\n" +
    "                               id=\"lonnsgarantidekker3\"\n" +
    "                               type=\"radio\"\n" +
    "                               value=\"Vet ikke\"\n" +
    "                               name=\"lonnsgarantidekker\"\n" +
    "                               data-ng-model=\"sluttaarsak.properties.lonnsgarantidekker\"\n" +
    "                               data-click-validate\n" +
    "                               data-ng-required=\"sluttaarsak.properties.lonnsgaranti == 'JaHarSokt'\"\n" +
    "                               data-error-messages=\"'arbeidsforhold.sluttaarsak.konkurs.lonnsgarantidekker.feilmelding'\">\n" +
    "                        <label for='lonnsgarantidekker3'\n" +
    "                               data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnsgarantidekker.vetikke\"></label>\n" +
    "                    </div>\n" +
    "\n" +
    "                    <span class=\"melding\"></span>\n" +
    "                </div>\n" +
    "            </div>\n" +
    "\n" +
    "            <div class=\"nav-radio-knapp\">\n" +
    "                <input class=\"sendsoknad-radio\"\n" +
    "                       id=\"lonnsgaranti2\"\n" +
    "                       type=\"radio\"\n" +
    "                       value=\"JaSkalSoke\"\n" +
    "                       name=\"lonnsgaranti\"\n" +
    "                       data-ng-model=\"sluttaarsak.properties.lonnsgaranti\"\n" +
    "                       data-click-validate\n" +
    "                       data-ng-required=\"true\"\n" +
    "                       data-error-messages=\"'arbeidsforhold.sluttaarsak.konkurs.lonnsgaranti.feilmelding'\">\n" +
    "                <label for='lonnsgaranti2'\n" +
    "                       data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnsgaranti.jaskalsoke\"></label>\n" +
    "            </div>\n" +
    "            <div class=\"nav-radio-knapp\">\n" +
    "                <input class=\"sendsoknad-radio\"\n" +
    "                       id=\"lonnsgaranti3\"\n" +
    "                       type=\"radio\"\n" +
    "                       value=\"Nei\"\n" +
    "                       name=\"lonnsgaranti\"\n" +
    "                       data-ng-model=\"sluttaarsak.properties.lonnsgaranti\"\n" +
    "                       data-click-validate\n" +
    "                       data-ng-required=\"true\"\n" +
    "                       data-error-messages=\"'arbeidsforhold.sluttaarsak.konkurs.lonnsgaranti.feilmelding'\">\n" +
    "                <label for='lonnsgaranti3'\n" +
    "                       data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnsgaranti.nei\"></label>\n" +
    "            </div>\n" +
    "\n" +
    "            <span class=\"melding\"></span>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-ng-show=\"sluttaarsak.properties.lonnsgaranti == 'Nei'\">\n" +
    "            <div data-navinfoboks>\n" +
    "                <p data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.lonnsgaranti.nei.informasjon\"></p>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-booleanradio\n" +
    "             data-navconfig\n" +
    "             data-nav-faktum-property=\"lonnkonkursmaaned\"\n" +
    "             data-nokkel=\"arbeidsforhold.sluttaarsak.konkurs.lonnkonkursmaaned\"></div>\n" +
    "\n" +
    "        <div class=\"ekstra-spm-boks\" data-vedlegginfoboks>\n" +
    "            <ul>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.1\"></p>\n" +
    "                </li>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2\"></p>\n" +
    "                    <a href=\"{{sluttaarsakUrl}}\"\n" +
    "                       data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2.lenketekst\">\n" +
    "                    </a>\n" +
    "                </li>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.vedlegg.liste.3\"></p>\n" +
    "                    <a href=\"{{lonnskravSkjema}}\"\n" +
    "                       data-cmstekster=\"arbeidsforhold.sluttaarsak.konkurs.vedlegg.liste.3.lenketekst\">\n" +
    "                    </a>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>");
}]);

angular.module("../views/templates/arbeidsforhold/kontrakt-utgaatt-oppsummering.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/arbeidsforhold/kontrakt-utgaatt-oppsummering.html",
    "<div class=\"varighet\">\n" +
    "    <span data-ng-bind=\"af.sluttaarsak.properties.datofra | date:'dd.MM.yyyy' | norskdato\"></span>\n" +
    "    <span data-ng-if=\"af.sluttaarsak.properties.datotil\" data-cmstekster=\"dato.til\"></span>\n" +
    "    <span data-ng-if=\"af.sluttaarsak.properties.datotil\" data-ng-bind=\"af.sluttaarsak.properties.datotil | date:'dd.MM.yyyy' | norskdato\"></span>\n" +
    "</div>\n" +
    "\n" +
    "<ul>\n" +
    "    <li>\n" +
    "        <p data-ng-bind=\"af.sluttaarsak.properties.type\"></p>\n" +
    "    </li>\n" +
    "    <li>\n" +
    "        <span data-cmstekster=\"arbeidsforhold.sluttaarsak.kontraktutgaatt.tilbudomaafortsette.sporsmal\"></span>\n" +
    "        <p data-ng-show=\"af.sluttaarsak.properties.tilbudomjobbannetsted == 'true'\" \n" +
    "            data-cmstekster=\"arbeidsforhold.sluttaarsak.kontraktutgaatt.tilbudomaafortsette.true\"></p>\n" +
    "        <p data-ng-show=\"af.sluttaarsak.properties.tilbudomjobbannetsted == 'false'\" \n" +
    "            data-cmstekster=\"arbeidsforhold.sluttaarsak.kontraktutgaatt.tilbudomaafortsette.false\"></p>\n" +
    "    </li>\n" +
    "</ul>\n" +
    "\n" +
    "<div data-vedlegginfoboks>\n" +
    "    <ul>\n" +
    "        <li>\n" +
    "            <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.1\"></p>\n" +
    "        </li>\n" +
    "        <li>\n" +
    "            <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2\"></p>\n" +
    "            <a href=\"{{sluttaarsakUrl}}\" data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2.lenketekst\">\n" +
    "            </a>\n" +
    "        </li>\n" +
    "    </ul>\n" +
    "</div>");
}]);

angular.module("../views/templates/arbeidsforhold/kontrakt-utgaatt.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/arbeidsforhold/kontrakt-utgaatt.html",
    "<div>\n" +
    "    <div class=\"spm\" data-nav-faktum=\"sluttaarsak\"\n" +
    "         data-ikke-auto-lagre=\"true\">\n" +
    "\n" +
    "\n" +
    "        <div class=\"varighet form-linje\">\n" +
    "            <div data-nav-dato\n" +
    "                 data-ng-model=\"sluttaarsak.properties.datofra\"\n" +
    "                 data-label=\"arbeidsforhold.arbeidsgiver.varighet.fra\"\n" +
    "                 data-er-required=\"true\"\n" +
    "                 data-required-error-message=\"arbeidsforhold.arbeidsgiver.varighet.fra.feilmelding\">\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"varighet form-linje\">\n" +
    "            <div data-nav-dato\n" +
    "                 data-ng-model=\"sluttaarsak.properties.datotil\"\n" +
    "                 data-label=\"arbeidsforhold.arbeidsgiver.varighet.til\"\n" +
    "                 data-er-required=\"true\"\n" +
    "                 data-er-fremtidigdato-tillatt=\"true\"\n" +
    "                 data-required-error-message=\"arbeidsforhold.arbeidsgiver.varighet.til.feilmelding\">\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-booleanradio\n" +
    "             data-navconfig\n" +
    "             data-nav-faktum-property=\"tilbudomjobbannetsted\"\n" +
    "             data-nokkel=\"arbeidsforhold.sluttaarsak.kontraktutgaatt.tilbudomaafortsette\"></div>\n" +
    "\n" +
    "        <div data-ng-show=\"sluttaarsak.properties.tilbudomjobbannetsted == 'true'\">\n" +
    "            <div data-navinfoboks>\n" +
    "                <p cmstekster=\"arbeidsforhold.sluttaarsak.advarsel\"></p>\n" +
    "                <p data-cmstekster=\"arbeidsforhold.sluttaarsak.sagtoppavarbeidsgiver.tilbudomjobbannetsted.true.informasjon\"></p>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"ekstra-spm-boks\" data-vedlegginfoboks>\n" +
    "            <ul>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.1\"></p>\n" +
    "                </li>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2\"></p>\n" +
    "                    <a href=\"{{sluttaarsakUrl}}\" data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2.lenketekst\">\n" +
    "                    </a>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>");
}]);

angular.module("../views/templates/arbeidsforhold/permittert-oppsummering.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/arbeidsforhold/permittert-oppsummering.html",
    "<div class=\"varighet\">\n" +
    "    <span data-ng-bind=\"af.sluttaarsak.properties.datofra | date:'dd.MM.yyyy' | norskdato\"></span>\n" +
    "    <span data-ng-if=\"af.sluttaarsak.properties.datotil\" data-cmstekster=\"dato.til\"></span>\n" +
    "    <span data-ng-if=\"af.sluttaarsak.properties.datotil\" data-ng-bind=\"af.sluttaarsak.properties.datotil | date:'dd.MM.yyyy' | norskdato\"></span>\n" +
    "</div>\n" +
    "\n" +
    "<ul>\n" +
    "    <li>\n" +
    "        <p data-ng-bind=\"af.sluttaarsak.properties.type\"></p>\n" +
    "    </li>\n" +
    "    <li>\n" +
    "        <p data-cmstekster=\"arbeidsforhold.arbeidsgiver.permittert.varighet\"> </p>\n" +
    "        <span data-ng-bind=\"af.sluttaarsak.properties.permiteringsperiodedatofra | date:'dd.MM.yyyy'\"></span>\n" +
    "        <span data-ng-if=\"af.sluttaarsak.properties.permiteringsperiodedatotil\"> TIL </span>\n" +
    "        <span data-ng-if=\"af.sluttaarsak.properties.permiteringsperiodedatotil\" data-ng-bind=\"af.sluttaarsak.properties.permiteringsperiodedatotil | date:'dd.MM.yyyy'\"></span>\n" +
    "    </li>\n" +
    "    <li>\n" +
    "        <p data-cmstekster=\"arbeidsforhold.arbeidsgiver.permittert.lonnsplikt.varighet\"> </p>\n" +
    "        <span data-ng-bind=\"af.sluttaarsak.properties.lonnspliktigperiodedatofra | date:'dd.MM.yyyy'\"></span>\n" +
    "        <span data-ng-if=\"af.sluttaarsak.properties.lonnspliktigperiodedatotil\"> TIL </span>\n" +
    "        <span data-ng-if=\"af.sluttaarsak.properties.lonnspliktigperiodedatotil\" data-ng-bind=\"af.sluttaarsak.properties.lonnspliktigperiodedatotil | date:'dd.MM.yyyy'\"></span>\n" +
    "    </li>\n" +
    "    <li>\n" +
    "        <span data-cmstekster=\"arbeidsforhold.sluttaarsak.permittert.permitteringsgrad\"></span>\n" +
    "        <p>\n" +
    "            <span data-ng-bind=\"af.sluttaarsak.properties.permitteringProsent\"></span>\n" +
    "            <span class=\"prosent\" data-cmstekster=\"arbeidsforhold.permittert.prosent\"></span>\n" +
    "        </p>\n" +
    "    </li>\n" +
    "</ul>\n" +
    "\n" +
    "\n" +
    "<div data-vedlegginfoboks>\n" +
    "    <ul>\n" +
    "        <li>\n" +
    "            <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.1\"></p>\n" +
    "        </li>\n" +
    "        <li>\n" +
    "            <p data-cmstekster=\"arbeidsforhold.sluttaarsak.permittert.vedlegg.liste.2\"></p>\n" +
    "            <a href=\"{{permiteringUrl}}\" data-cmstekster=\"arbeidsforhold.sluttaarsak.permittert.vedlegg.liste.2.lenketekst\">\n" +
    "            </a>\n" +
    "        </li>\n" +
    "        <li>\n" +
    "            <p data-cmstekster=\"arbeidsforhold.sluttaarsak.permittert.vedlegg.liste.3\"></p>\n" +
    "        </li>\n" +
    "    </ul>\n" +
    "    </p>\n" +
    "</div>");
}]);

angular.module("../views/templates/arbeidsforhold/permittert.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/arbeidsforhold/permittert.html",
    "<div>\n" +
    "    <div data-navinfoboks>\n" +
    "        <p class=\"sluttaarsak-informasjon\" data-cmstekster=\"arbeidsforhold.sluttaarsak.permittert.informasjon\"/>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"spm\" data-nav-faktum=\"sluttaarsak\"\n" +
    "         data-ikke-auto-lagre=\"true\">\n" +
    "\n" +
    "        <div class=\"varighet form-linje\">\n" +
    "            <div data-nav-dato\n" +
    "                 data-ng-model=\"sluttaarsak.properties.datofra\"\n" +
    "                 data-label=\"arbeidsforhold.arbeidsgiver.varighet.fra\"\n" +
    "                 data-er-required=\"true\"\n" +
    "                 data-required-error-message=\"arbeidsforhold.arbeidsgiver.varighet.fra.feilmelding\">\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <p data-cmstekster=\"arbeidsforhold.arbeidsgiver.permittert.varighet\"></p>\n" +
    "\n" +
    "        <div class=\"varighet\">\n" +
    "            <div data-nav-dato-intervall\n" +
    "                 data-fra-dato=\"faktum.properties.permiteringsperiodedatofra\"\n" +
    "                 data-til-dato=\"faktum.properties.permiteringsperiodedatotil\"\n" +
    "                 data-label=\"arbeidsforhold.arbeidsgiver.permittert.varighet\"\n" +
    "                 data-er-fremtidigdato-tillatt=\"true\"\n" +
    "                 data-er-fradato-required=\"true\">\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <p>\n" +
    "            <span data-cmstekster=\"arbeidsforhold.arbeidsgiver.permittert.lonnsplikt.varighet\"></span>\n" +
    "            <span data-nav-hjelpetekstelement></span>\n" +
    "        </p>\n" +
    "\n" +
    "        <div class=\"varighet\">\n" +
    "            <div data-nav-dato-intervall\n" +
    "                 data-fra-dato=\"faktum.properties.lonnspliktigperiodedatofra\"\n" +
    "                 data-til-dato=\"faktum.properties.lonnspliktigperiodedatotil\"\n" +
    "                 data-label=\"arbeidsforhold.arbeidsgiver.permittert.lonnsplikt.varighet\"\n" +
    "                 data-er-fremtidigdato-tillatt=\"true\"\n" +
    "                 data-er-begge-required=\"true\">\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"tekstfelt form-linje prosentfelt\">\n" +
    "            <label>\n" +
    "\n" +
    "                <span class=\"labeltekst\"\n" +
    "                      data-cmstekster=\"arbeidsforhold.sluttaarsak.permittert.permitteringsgrad\"></span>\n" +
    "                <input data-ng-model=\"sluttaarsak.properties.permitteringProsent\"\n" +
    "                       type=\"text\"\n" +
    "                       data-ng-required=\"true\"\n" +
    "                       data-error-messages=\"{required:'arbeidsforhold.sluttaarsak.permittert.permitteringsgrad.feilmelding', pattern: 'regex.tall'}\"\n" +
    "                       data-ng-pattern=\"/^\\d+$/\"\n" +
    "                       data-blur-validate\n" +
    "                       data-ng-blur=\"settPermitteringsflagg($event)\"\n" +
    "                       maxlength=\"3\"\n" +
    "                       data-tekstfelt-patternvalidering/>\n" +
    "                <span class=\"prosent\" data-cmstekster=\"egennaering.prosent\"></span>\n" +
    "                <span class=\"melding\"></span>\n" +
    "            </label>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-ng-show=\"skalVisePermitteringInfo\">\n" +
    "            <div data-navinfoboks>\n" +
    "                <p data-cmstekster=\"arbeidsforhold.sluttaarsak.permittert.forlite\"></p>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"ekstra-spm-boks\" data-vedlegginfoboks>\n" +
    "            <ul>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.1\"></p>\n" +
    "                </li>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"arbeidsforhold.sluttaarsak.permittert.vedlegg.liste.2\"></p>\n" +
    "                    <a href=\"{{permiteringUrl}}\"\n" +
    "                       data-cmstekster=\"arbeidsforhold.sluttaarsak.permittert.vedlegg.liste.2.lenketekst\">\n" +
    "                    </a>\n" +
    "                </li>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"arbeidsforhold.sluttaarsak.permittert.vedlegg.liste.3\"></p>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "            </p>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../views/templates/arbeidsforhold/redusertarbeidstid-oppsummering.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/arbeidsforhold/redusertarbeidstid-oppsummering.html",
    "<div class=\"varighet\">\n" +
    "    <span data-ng-bind=\"af.sluttaarsak.properties.datofra | date:'dd.MM.yyyy' | norskdato\"></span>\n" +
    "    <span data-ng-if=\"af.sluttaarsak.properties.datotil\" data-cmstekster=\"dato.til\"></span>\n" +
    "    <span data-ng-if=\"af.sluttaarsak.properties.datotil\" data-ng-bind=\"af.sluttaarsak.properties.datotil | date:'dd.MM.yyyy' | norskdato\"></span>\n" +
    "</div>\n" +
    "\n" +
    "<ul>\n" +
    "    <li>\n" +
    "        <p data-ng-bind=\"af.sluttaarsak.properties.type\"></p>\n" +
    "    </li>\n" +
    "    <li>\n" +
    "        <span data-cmstekster=\"arbeidsforhold.sluttaarsak.redusertArbeidstid.fraDato.sporsmal\"></span>\n" +
    "        <p data-ng-bind=\"af.sluttaarsak.properties.redusertfra\"></p>\n" +
    "    </li>\n" +
    "    <li>\n" +
    "        <span data-cmstekster=\"arbeidsforhold.sluttaarsak.redusertArbeidstid.nyttTilbud.sporsmal\"></span>\n" +
    "        <p data-ng-show=\"af.sluttaarsak.properties.nyttTilbud == 'true'\" \n" +
    "            data-cmstekster=\"arbeidsforhold.sluttaarsak.redusertArbeidstid.nyttTilbud.true\"></p>\n" +
    "        <p data-ng-show=\"af.sluttaarsak.properties.nyttTilbud == 'false'\" \n" +
    "            data-cmstekster=\"arbeidsforhold.sluttaarsak.redusertArbeidstid.nyttTilbud.false\"></p>\n" +
    "    </li>\n" +
    "</ul>\n" +
    "\n" +
    "<div data-vedlegginfoboks>\n" +
    "    <ul>\n" +
    "        <li>\n" +
    "            <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.1\"></p>\n" +
    "        </li>\n" +
    "        <li>\n" +
    "            <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2\"></p>\n" +
    "            <a href=\"{{sluttaarsakUrl}}\" data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2.lenketekst\">\n" +
    "            </a>\n" +
    "        </li>\n" +
    "    </ul>\n" +
    "</div>");
}]);

angular.module("../views/templates/arbeidsforhold/redusertarbeidstid.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/arbeidsforhold/redusertarbeidstid.html",
    "<div>\n" +
    "    <div data-navinfoboks>\n" +
    "        <p class=\"sluttaarsak-informasjon\"\n" +
    "           data-cmstekster=\"arbeidsforhold.sluttaarsak.redusertArbeidstid.informasjon\"></p>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"spm\" data-nav-faktum=\"sluttaarsak\"\n" +
    "         data-ikke-auto-lagre=\"true\">\n" +
    "\n" +
    "        <div class=\"varighet form-linje\">\n" +
    "            <div data-nav-dato\n" +
    "                 data-ng-model=\"sluttaarsak.properties.datofra\"\n" +
    "                 data-label=\"arbeidsforhold.arbeidsgiver.varighet.fra\"\n" +
    "                 data-er-required=\"true\"\n" +
    "                 data-required-error-message=\"arbeidsforhold.arbeidsgiver.varighet.fra.feilmelding\">\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"varighet form-linje\">\n" +
    "            <div data-nav-dato\n" +
    "                 data-ng-model=\"sluttaarsak.properties.redusertfra\"\n" +
    "                 data-label=\"arbeidsforhold.sluttaarsak.redusertArbeidstid.fraDato.sporsmal\"\n" +
    "                 data-er-required=\"true\"\n" +
    "                 data-er-fremtidigdato-tillatt=\"true\"\n" +
    "                 data-required-error-message=\"arbeidsforhold.sluttaarsak.redusertArbeidstid.fraDato.feilmelding\">\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-booleanradio\n" +
    "             data-navconfig\n" +
    "             data-nav-faktum-property=\"nyttTilbud\"\n" +
    "             data-nokkel=\"arbeidsforhold.sluttaarsak.redusertArbeidstid.nyttTilbud\"></div>\n" +
    "\n" +
    "        <div data-ng-show=\"sluttaarsak.properties.nyttTilbud == 'true'\">\n" +
    "            <div data-navinfoboks>\n" +
    "                <p data-cmstekster=\"arbeidsforhold.sluttaarsak.redusertArbeidstid.nyttTilbud.true.informasjon\"></p>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"ekstra-spm-boks\" data-vedlegginfoboks>\n" +
    "            <ul>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.1\"></p>\n" +
    "                </li>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2\"></p>\n" +
    "                    <a href=\"{{sluttaarsakUrl}}\"\n" +
    "                       data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2.lenketekst\">\n" +
    "                    </a>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>");
}]);

angular.module("../views/templates/arbeidsforhold/sagt-opp-av-arbeidsgiver-oppsummering.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/arbeidsforhold/sagt-opp-av-arbeidsgiver-oppsummering.html",
    "<div class=\"varighet\">\n" +
    "    <span data-ng-bind=\"af.sluttaarsak.properties.datofra | date:'dd.MM.yyyy' | norskdato\"></span>\n" +
    "    <span data-ng-if=\"af.sluttaarsak.properties.datotil\" data-cmstekster=\"dato.til\"></span>\n" +
    "    <span data-ng-if=\"af.sluttaarsak.properties.datotil\" data-ng-bind=\"af.sluttaarsak.properties.datotil | date:'dd.MM.yyyy' | norskdato\"></span>\n" +
    "</div>\n" +
    "\n" +
    "<ul>\n" +
    "    <li>\n" +
    "        <p data-ng-bind=\"af.sluttaarsak.properties.type\"></p>\n" +
    "    </li>\n" +
    "    <li>\n" +
    "        <span data-cmstekster=\"arbeidsforhold.sluttaarsak.sagtoppavarbeidsgiver.aarsak.sporsmal\"></span>\n" +
    "        <p data-ng-bind=\"af.sluttaarsak.properties.aarsak\"></p>\n" +
    "    </li>\n" +
    "    <li>\n" +
    "        <span data-cmstekster=\"arbeidsforhold.sluttaarsak.sagtoppavarbeidsgiver.tilbudomjobbannetsted.sporsmal\"></span>\n" +
    "        <p data-ng-show=\"af.sluttaarsak.properties.sagtOppAvArbeidsgiverTilbudomjobbannetsted == 'true'\" \n" +
    "            data-cmstekster=\"arbeidsforhold.sluttaarsak.sagtoppavarbeidsgiver.tilbudomjobbannetsted.true\"></p>\n" +
    "        <p data-ng-show=\"af.sluttaarsak.properties.sagtOppAvArbeidsgiverTilbudomjobbannetsted == 'false'\" \n" +
    "            data-cmstekster=\"arbeidsforhold.sluttaarsak.sagtoppavarbeidsgiver.tilbudomjobbannetsted.false\"></p>\n" +
    "    </li>\n" +
    "</ul>\n" +
    "\n" +
    "<div data-vedlegginfoboks>\n" +
    "    <ul>\n" +
    "        <li>\n" +
    "            <span data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.1\"></span>\n" +
    "        </li>\n" +
    "        <li>\n" +
    "            <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2\"></p>\n" +
    "            <a href=\"{{sluttaarsakUrl}}\" data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2.lenketekst\">\n" +
    "            </a>\n" +
    "        </li>\n" +
    "    </ul>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../views/templates/arbeidsforhold/sagt-opp-av-arbeidsgiver.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/arbeidsforhold/sagt-opp-av-arbeidsgiver.html",
    "<div>\n" +
    "\n" +
    "    <div class=\"spm\" data-nav-faktum=\"sluttaarsak\"\n" +
    "         data-ikke-auto-lagre=\"true\">\n" +
    "\n" +
    "        <div class=\"varighet form-linje\">\n" +
    "            <div data-nav-dato\n" +
    "                 data-ng-model=\"sluttaarsak.properties.datofra\"\n" +
    "                 data-label=\"arbeidsforhold.arbeidsgiver.varighet.fra\"\n" +
    "                 data-er-required=\"true\"\n" +
    "                 data-required-error-message=\"arbeidsforhold.arbeidsgiver.varighet.fra.feilmelding\">\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"varighet form-linje\">\n" +
    "            <div data-nav-dato\n" +
    "                 data-ng-model=\"sluttaarsak.properties.datotil\"\n" +
    "                 data-label=\"arbeidsforhold.arbeidsgiver.varighet.til\"\n" +
    "                 data-er-required=\"true\"\n" +
    "                 data-er-fremtidigdato-tillatt=\"true\"\n" +
    "                 data-required-error-message=\"arbeidsforhold.arbeidsgiver.varighet.til.feilmelding\">\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-navtextarea\n" +
    "             data-navconfig\n" +
    "             data-nav-faktum-property=\"aarsak\"\n" +
    "             data-nokkel=\"arbeidsforhold.sluttaarsak.sagtoppavarbeidsgiver.aarsak\"\n" +
    "             data-maxlengde=\"500\"\n" +
    "             data-feilmelding=\"arbeidsforhold.sluttaarsak.sagtoppavarbeidsgiver.aarsak.feilmelding\"\n" +
    "             data-obligatorisk=\"true\"></div>\n" +
    "\n" +
    "        <div data-booleanradio\n" +
    "             data-navconfig\n" +
    "             data-nav-faktum-property=\"sagtOppAvArbeidsgiverTilbudomjobbannetsted\"\n" +
    "             data-nokkel=\"arbeidsforhold.sluttaarsak.sagtoppavarbeidsgiver.tilbudomjobbannetsted\"></div>\n" +
    "\n" +
    "        <div data-ng-show=\"sluttaarsak.properties.sagtOppAvArbeidsgiverTilbudomjobbannetsted == 'true'\">\n" +
    "            <div data-navinfoboks>\n" +
    "                <p cmstekster=\"arbeidsforhold.sluttaarsak.sagtoppavarbeidsgiver.informasjon\"></p>\n" +
    "                <p cmstekster=\"arbeidsforhold.sluttaarsak.advarsel\"></p>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"ekstra-spm-boks\" data-vedlegginfoboks>\n" +
    "            <ul>\n" +
    "                <li>\n" +
    "                    <span data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.1\"></span>\n" +
    "                </li>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2\"></p>\n" +
    "                    <a href=\"{{sluttaarsakUrl}}\" data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2.lenketekst\">\n" +
    "                    </a>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "\n" +
    "\n" +
    "");
}]);

angular.module("../views/templates/arbeidsforhold/sagt-opp-selv-oppsummering.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/arbeidsforhold/sagt-opp-selv-oppsummering.html",
    "<div class=\"varighet\">\n" +
    "    <span data-ng-bind=\"af.sluttaarsak.properties.datofra | date:'dd.MM.yyyy' | norskdato\"></span>\n" +
    "    <span data-ng-if=\"af.sluttaarsak.properties.datotil\" data-cmstekster=\"dato.til\"></span>\n" +
    "    <span data-ng-if=\"af.sluttaarsak.properties.datotil\" data-ng-bind=\"af.sluttaarsak.properties.datotil | date:'dd.MM.yyyy' | norskdato\"></span>\n" +
    "</div>\n" +
    "\n" +
    "<ul>\n" +
    "    <li>\n" +
    "        <p data-ng-bind=\"af.sluttaarsak.properties.type\"></p>\n" +
    "    </li>\n" +
    "    <li>\n" +
    "        <span data-cmstekster=\"arbeidsforhold.sluttaarsak.sagtoppselv.aarsak.sporsmal\"></span>\n" +
    "        <p data-ng-bind=\"af.sluttaarsak.properties.sagtoppselvAarsak\"></p>\n" +
    "    </li>\n" +
    "</ul>\n" +
    "\n" +
    "<div data-vedlegginfoboks>\n" +
    "    <ul>\n" +
    "        <li>\n" +
    "            <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.1\"></p>\n" +
    "        </li>\n" +
    "        <li>\n" +
    "            <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2\"></p>\n" +
    "            <a href=\"{{sluttaarsakUrl}}\" data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2.lenketekst\">\n" +
    "            </a>\n" +
    "        </li>\n" +
    "    </ul>\n" +
    "</div>");
}]);

angular.module("../views/templates/arbeidsforhold/sagt-opp-selv.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/arbeidsforhold/sagt-opp-selv.html",
    "<div>\n" +
    "    <div data-navinfoboks>\n" +
    "        <p class=\"sluttaarsak-informasjon\" cmstekster=\"arbeidsforhold.sluttaarsak.sagtoppselv.informasjon\"></p>\n" +
    "        <p cmstekster=\"arbeidsforhold.sluttaarsak.advarsel\"></p>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"spm\" data-nav-faktum=\"sluttaarsak\"\n" +
    "         data-ikke-auto-lagre=\"true\">\n" +
    "\n" +
    "        <div class=\"varighet\">\n" +
    "            <div data-nav-dato-intervall\n" +
    "                 data-fra-dato=\"faktum.properties.datofra\"\n" +
    "                 data-til-dato=\"faktum.properties.datotil\"\n" +
    "                 data-label=\"arbeidsforhold.arbeidsgiver.varighet\"\n" +
    "                 data-er-fremtidigdato-tillatt=\"true\"\n" +
    "                 data-er-begge-required=\"true\">\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-navtextarea\n" +
    "             data-navconfig\n" +
    "             data-nav-faktum-property=\"sagtoppselvAarsak\"\n" +
    "             data-nokkel=\"arbeidsforhold.sluttaarsak.sagtoppselv.aarsak\"\n" +
    "             data-maxlengde=\"500\"\n" +
    "             data-feilmelding=\"arbeidsforhold.sluttaarsak.sagtoppselv.aarsak.feilmelding\"\n" +
    "             data-obligatorisk=\"true\"></div>\n" +
    "\n" +
    "        <div class=\"ekstra-spm-boks\" data-vedlegginfoboks>\n" +
    "            <ul>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.1\"></p>\n" +
    "                </li>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2\"></p>\n" +
    "                    <a href=\"{{sluttaarsakUrl}}\" data-cmstekster=\"arbeidsforhold.sluttaarsak.vedlegg.liste.2.lenketekst\">\n" +
    "                    </a>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../views/templates/arbeidsforhold/utdanning_form.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/arbeidsforhold/utdanning_form.html",
    "\n" +
    "");
}]);

angular.module("../views/templates/avbryt.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/avbryt.html",
    "<div id=\"slett\" class=\"soknad avbryt-boks\" data-sidetittel=\"sidetittel.slett\">\n" +
    "    <div class=\"rad-belyst inforad\">\n" +
    "        <div class=\"begrensning sak-totredel\">\n" +
    "            <section class=\"panel-standard oversikt\" data-ng-controller=\"AvbrytCtrl\">\n" +
    "\n" +
    "                <div class=\"fremdriftsindikator\" ng-show=\"krevBekreftelse === false\">\n" +
    "                    <img src=\"../img/ajaxloader/hvit/loader_hvit_64.gif\" alt=\"Fremdriftsindikator\"/>\n" +
    "                </div>\n" +
    "\n" +
    "                <div ng-show=\"krevBekreftelse == true\">\n" +
    "                    <h1 class=\"stor-ikon-slett-strek\" data-cmstekster=\"slett.tittel\"></h1>\n" +
    "\n" +
    "                    <p class=\"info\" data-cmstekster=\"slett.informasjon\"></p>\n" +
    "                </div>\n" +
    "\n" +
    "                <div class=\"slett-soknad-stor\">\n" +
    "                    <div data-ng-show=\"krevBekreftelse == true\">\n" +
    "\n" +
    "                        <form novalidate>\n" +
    "                            <input type=\"button\" class=\"knapp-advarsel\" data-ng-click=\"submitForm()\"\n" +
    "                                   data-cmstekster=\"avbryt.slett\"\n" +
    "                                   data-fremdriftsindikator>\n" +
    "                        </form>\n" +
    "\n" +
    "                        <div>\n" +
    "                            <a href=\"#/soknad\" data-cmstekster=\"slett.tilbake\"></a>\n" +
    "                        </div>\n" +
    "                    </div>\n" +
    "                </div>\n" +
    "            </section>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "\n" +
    "");
}]);

angular.module("../views/templates/barnetillegg-nyttbarn.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/barnetillegg-nyttbarn.html",
    "<div class=\"modalBoks\" data-ng-controller=\"BarneCtrl\">\n" +
    "    <div data-ng-form=\"nyttBarnForm\" class=\"vertikal\">\n" +
    "        <div class=\"begrensning\">\n" +
    "            <div class=\"sak-totredel\">\n" +
    "                <div class=\"barn-spm spm-boks panel-standard-belyst\">\n" +
    "                    <a href=\"#/soknad\" class=\"lukk\"> </a>\n" +
    "\n" +
    "                    <div data-form-errors></div>\n" +
    "\n" +
    "                    <h2 class=\"stor-strek\" data-ng-if=\"endrerSystemregistrertBarn()\">\n" +
    "                        <span data-cmstekster=\"barnetillegg.sokerbarnetillegg.tittel\"></span>\n" +
    "                        <span data-ng-bind=\"barnenavn\"></span>\n" +
    "                    </h2>\n" +
    "\n" +
    "                    <div data-nav-faktum=\"barn\" data-ikke-auto-lagre=\"true\">\n" +
    "\n" +
    "\n" +
    "                        <div data-ng-if=\"leggerTilNyttBarnEllerEndrerBarn()\">\n" +
    "                            <div data-form-errors></div>\n" +
    "                            <h2 class=\"stor-strek\" data-cmstekster=\"barnetillegg.nyttbarn.tittel\"></h2>\n" +
    "\n" +
    "                            <div class=\"form-linje\">\n" +
    "                                <label>\n" +
    "                                    <span data-cmstekster=\"barnetillegg.nyttbarn.fornavn\"></span>\n" +
    "                                    <input name=\"fornavn\"\n" +
    "                                           data-error-messages=\"'barnetillegg.nyttbarn.fornavn.feilmelding'\"\n" +
    "                                           type=\"text\"\n" +
    "                                           data-ng-model=\"barn.properties.fornavn\"\n" +
    "                                           data-blur-validate\n" +
    "                                           data-maxlength=\"200\"\n" +
    "                                           maxlength=\"200\"\n" +
    "                                           required>\n" +
    "                                    <span class=\"melding\"></span>\n" +
    "                                </label>\n" +
    "                            </div>\n" +
    "                            <div class=\"form-linje\">\n" +
    "                                <label>\n" +
    "                                    <span data-cmstekster=\"barnetillegg.nyttbarn.etternavn\"></span>\n" +
    "                                    <input name=\"fnr\"\n" +
    "                                           data-error-messages=\"'barnetillegg.nyttbarn.etternavn.feilmelding'\"\n" +
    "                                           type=\"text\"\n" +
    "                                           data-ng-model=\"barn.properties.etternavn\"\n" +
    "                                           data-blur-validate\n" +
    "                                           data-maxlength=\"200\"\n" +
    "                                           maxlength=\"200\"\n" +
    "                                           required>\n" +
    "                                    <span class=\"melding\"></span>\n" +
    "                                </label>\n" +
    "                            </div>\n" +
    "\n" +
    "                            <div class=\"form-linje under-atten\"\n" +
    "                                 data-ng-class=\"{feil: skalViseFeilmelding === true }\">\n" +
    "                                <input type=\"hidden\" class=\"under-atten-dato\" data-ng-model=\"underAtten.value\"\n" +
    "                                       data-ng-required=\"true\"\n" +
    "                                       data-error-messages=\"'barnetillegg.nyttbarn.fodselsdato.feilmelding'\">\n" +
    "\n" +
    "                                <div data-nav-dato\n" +
    "                                     data-ng-model=\"barn.properties.fodselsdato\"\n" +
    "                                     data-label=\"barnetillegg.nyttbarn.fodselsdato\"\n" +
    "                                     data-er-required=\"true\"\n" +
    "                                     data-required-error-message=\"barnetillegg.nyttbarn.fodselsdato.feilmelding\">\n" +
    "                                </div>\n" +
    "\n" +
    "                                <span class=\"melding\"\n" +
    "                                      data-cmstekster=\"barnetillegg.nyttbarn.fodselsdato.underAtten.feilmelding\"></span>\n" +
    "                            </div>\n" +
    "\n" +
    "                            <div class=\"land\">\n" +
    "                                <div data-nav-select\n" +
    "                                     data-navconfig\n" +
    "                                     data-nav-faktum-property=\"land\"\n" +
    "                                     data-label=\"barnetillegg.nyttbarn.land\"\n" +
    "                                     data-options=\"land.result\"\n" +
    "                                     data-er-required=\"true\"\n" +
    "                                     data-ikke-auto-lagre=\"true\"\n" +
    "                                     data-default-value=\"barnetillegg.nyttbarn.landDefault\"\n" +
    "                                     data-required-feilmelding=\"barnetillegg.nyttbarn.land.feilmelding\"\n" +
    "                                     data-ugyldig-feilmelding=\"barnetillegg.nyttbarn.land.ugyldig.feilmelding\">\n" +
    "                                </div>\n" +
    "                            </div>\n" +
    "\n" +
    "                            <div data-ng-show=\"erEosLandAnnetEnnNorge()\">\n" +
    "                                <div data-navinfoboks>\n" +
    "                                    <p class=\"sluttaarsak-informasjon\" cmstekster=\"barnetillegg.nyttbarn.land.eos\"></p>\n" +
    "                                </div>\n" +
    "                            </div>\n" +
    "                            <div data-ng-show=\"erIkkeEosLand()\">\n" +
    "                                <div data-navinfoboks>\n" +
    "                                    <p class=\"sluttaarsak-informasjon\" cmstekster=\"barnetillegg.nyttbarn.land.ikkeeos\"></p>\n" +
    "                                </div>\n" +
    "                            </div>\n" +
    "\n" +
    "                            <div data-ng-show=\"barn.properties.land == 'NOR'\">\n" +
    "                                <div class=\"ekstra-spm-boks\" data-vedlegginfoboks>\n" +
    "                                    <ul>\n" +
    "                                        <li>\n" +
    "                                            {{ 'barnetillegg.nyttbarn.land.norge.vedlegginformasjon' | cmstekst}}\n" +
    "                                        </li>\n" +
    "                                    </ul>\n" +
    "                                </div>\n" +
    "                            </div>\n" +
    "\n" +
    "                        </div>\n" +
    "\n" +
    "                        <div>\n" +
    "                            <div class=\"form-linje checkbox\" data-checkbox-validate>\n" +
    "                                <input data-ng-model=\"barn.properties.barnetillegg\" name=\"barnetilegg\"\n" +
    "                                       id=\"barnetilegg\" type=\"checkbox\"\n" +
    "                                       data-error-messages=\"'barnetillegg.nyttbarn.barnetillegg.feilmelding'\"\n" +
    "                                       data-boolean-verdi data-ng-required=\"true\">\n" +
    "                                <label for=\"barnetilegg\"\n" +
    "                                       data-cmstekster=\"barnetillegg.barnetilegg.sporsmal\"></label>\n" +
    "                                <span class=\"melding\"\n" +
    "                                      data-cmstekster=\"barnetillegg.nyttbarn.barnetillegg.feilmelding\"> </span>\n" +
    "                            </div>\n" +
    "\n" +
    "                            <div data-ng-show=\"barnetilleggErRegistrert()\">\n" +
    "                                <p data-cmstekster=\"barnetillegg.barnetilegg.ikkebarneinntekt.sporsmal\"></p>\n" +
    "\n" +
    "                                <div class=\"form-linje boolean\">\n" +
    "                                    <div class=\"nav-radio-knapp\">\n" +
    "                                        <input class=\"sendsoknad-radio\"\n" +
    "                                               id=\"ikkebarneinntekt\"\n" +
    "                                               type=\"radio\"\n" +
    "                                               value=\"true\"\n" +
    "                                               data-ng-model=\"barn.properties.ikkebarneinntekt\"\n" +
    "                                               data-ng-required=\"barnetilleggErRegistrert()\"\n" +
    "                                               name=\"ikkebarneinntekt\"\n" +
    "                                               data-click-validate\n" +
    "                                               data-error-messages=\"'barnetillegg.barnetilegg.ikkebarneinntekt.feilmelding'\">\n" +
    "                                        <label for='ikkebarneinntekt'\n" +
    "                                               data-cmstekster=\"barnetillegg.barnetilegg.ikkebarneinntekt.true\"></label>\n" +
    "                                    </div>\n" +
    "                                    <div class=\"nav-radio-knapp\">\n" +
    "                                        <input class=\"sendsoknad-radio\"\n" +
    "                                               id=\"ikkebarneinntektNei\"\n" +
    "                                               type=\"radio\"\n" +
    "                                               value=\"false\"\n" +
    "                                               data-ng-model=\"barn.properties.ikkebarneinntekt\"\n" +
    "                                               data-ng-required=\"barnetilleggErRegistrert()\"\n" +
    "                                               name=\"ikkebarneinntekt\"\n" +
    "                                               data-click-validate\n" +
    "                                               data-error-messages=\"'barnetillegg.barnetilegg.ikkebarneinntekt.feilmelding'\">\n" +
    "                                        <label for='ikkebarneinntektNei'\n" +
    "                                               data-cmstekster=\"barnetillegg.barnetilegg.ikkebarneinntekt.false\"></label>\n" +
    "                                    </div>\n" +
    "                                    <span class=\"melding\"></span>\n" +
    "                                </div>\n" +
    "\n" +
    "                                <div data-ng-if=\"barnetHarInntekt()\">\n" +
    "                                    <div class=\"form-linje\">\n" +
    "                                        <label>\n" +
    "                                            <span data-cmstekster=\"barnetillegg.barnetilegg.barneinntekttall.sporsmal\"></span>\n" +
    "                                            <input name=\"barneinntekttall\"\n" +
    "                                                   data-error-messages=\"{required:'barnetillegg.barnetilegg.barneinntekttall.feilmelding', pattern:'barnetillegg.barnetilegg.barneinntekttall.ugyldig.feilmelding'}\"\n" +
    "                                                   type=\"text\"\n" +
    "                                                   data-ng-pattern=\"/\\d+/\"\n" +
    "                                                   data-ng-model=\"barn.properties.barneinntekttall\"\n" +
    "                                                   data-blur-validate\n" +
    "                                                   data-maxlength=\"200\"\n" +
    "                                                   maxlength=\"200\"\n" +
    "                                                   required=\"barnetHarInntekt()\">\n" +
    "                                            <span class=\"melding\"></span>\n" +
    "                                        </label>\n" +
    "                                    </div>\n" +
    "\n" +
    "                                    <div class=\"ekstra-spm-boks\" data-vedlegginfoboks>\n" +
    "                                        <ul>\n" +
    "                                            <li>\n" +
    "                                                {{ 'barnetillegg.barnetilegg.ikkebarneinntekt.false.vedlegginformasjon' | cmstekst}}\n" +
    "                                            </li>\n" +
    "                                        </ul>\n" +
    "                                    </div>\n" +
    "\n" +
    "                                </div>\n" +
    "                            </div>\n" +
    "                        </div>\n" +
    "                    </div>\n" +
    "\n" +
    "                    <div class=\"knapper-opprett\">\n" +
    "                        <input class=\"knapp-hoved-liten\" name=\"lagre\" type=\"submit\"\n" +
    "                               data-cmstekster=\"barnetillegg.nyttbarn.lagre\"\n" +
    "                               data-ng-if=\"leggerTilNyttBarnEllerEndrerBarn()\"\n" +
    "                               data-ng-click=\"lagreBarn(nyttBarnForm);\">\n" +
    "                        <input class=\"knapp-hoved-liten\" name=\"lagre\" type=\"submit\"\n" +
    "                               data-cmstekster=\"barnetillegg.nyttbarn.lagre\"\n" +
    "                               data-ng-if=\"endrerSystemregistrertBarn()\"\n" +
    "                               data-ng-click=\"lagreBarneFaktum(nyttBarnForm);\">\n" +
    "                        <a href=\"#/soknad\" data-cmstekster=\"barnetillegg.nyttbarn.avbryt\"></a>\n" +
    "                    </div>\n" +
    "\n" +
    "                </div>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>");
}]);

angular.module("../views/templates/barnetillegg.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/barnetillegg.html",
    "<div class=\"skjemaramme\">\n" +
    "    <div id=\"barnetilleggForm\" data-ng-form=\"barnetilleggForm\" class=\"skjemainnhold\"\n" +
    "         data-ng-controller=\"BarnetilleggCtrl\" novalidate>\n" +
    "\n" +
    "        <p class=\"informasjonstekst\" cmstekster=\"barnetillegg.informasjon\"></p>\n" +
    "\n" +
    "        <div class=\"spm-boks vertikal\" data-ng-repeat=\"b in barn\">\n" +
    "            <div data-ng-class=\"{gutt: erGutt(b), jente:erJente(b)}\" id=\"barnetillegg{{b.faktumId}}\" class=\"barn\">\n" +
    "                <a href=\"#\" class=\"lukk\" data-ng-show=\"erBrukerregistrert(b)\"\n" +
    "                   data-ng-click=\"slettBarn(b, $index, $event)\"> </a>\n" +
    "\n" +
    "                <div class=\"barnealder\">\n" +
    "                    <span data-cmstekster=\"aar\"></span>\n" +
    "                    <span class=\"alder robust\" data-ng-bind=\"b.properties.alder\"></span>\n" +
    "                </div>\n" +
    "                <div class=\"barneinfo\">\n" +
    "                    <div data-ng-bind=\"b.properties.sammensattnavn\"></div>\n" +
    "                    <div data-ng-bind=\"b.properties.fnr\"></div>\n" +
    "                    <div>{{ b.properties.fodselsdato | date:'dd.MM.yyyy'}}</div>\n" +
    "                    <div class=\"barnbosted\">\n" +
    "                        <span class=\"bosted\">bosted: </span>\n" +
    "                        <span data-ng-show=\"ingenLandRegistrert(b)\">Ingen land registrert</span>\n" +
    "                        <span data-ng-bind=\"b.properties.land\"></span>\n" +
    "                    </div>\n" +
    "                </div>\n" +
    "                <div class=\"barnecheckbox\">\n" +
    "                    <div data-ng-show=\"erSystemRegistrert(b)\">\n" +
    "                        <a href data-ng-click=\"sokbarnetillegg(b.faktumId, $event)\"\n" +
    "                           data-cmstekster=\"barnetillegg.barnetilegg.sporsmal\"\n" +
    "                           data-ng-if=\"barnetilleggIkkeRegistrert(b)\"></a>\n" +
    "                        <a href=\"#\" data-ng-if=\"barnetilleggErRegistrert(b)\"\n" +
    "                           data-ng-click=\"slettBarnetillegg(b, $index, $event)\"\n" +
    "                           data-cmstekster=\"barnetillegg.slettbarnetillegg\"></a>\n" +
    "                    </div>\n" +
    "\n" +
    "                    <div data-ng-if=\"barnetilleggErRegistrert(b)\">\n" +
    "                        <p class=\"svar-oppsummering\" data-cmstekster=\"barnetillegg.barnetilegg.sporsmal\"></p>\n" +
    "\n" +
    "                        <p class=\"svar-oppsummering\" data-ng-if=\"barnetHarIkkeInntekt(b)\"\n" +
    "                           data-cmstekster=\"barnetillegg.barnetilegg.ikkebarneinntekt.true\"></p>\n" +
    "\n" +
    "                        <p class=\"svar-oppsummering\" data-ng-if=\"barnetHarInntekt(b)\">\n" +
    "                            <span data-cmstekster=\"barnetillegg.barnetilegg.ikkebarneinntekt.false\"> </span>\n" +
    "                            <span>.</span>\n" +
    "                            <span data-cmstekster=\"barnetillegg.barnetilegg.barneinntekttall.sporsmal\"></span>\n" +
    "                            <span>{{ b.properties.barneinntekttall | currency }}</span>\n" +
    "                        </p>\n" +
    "                    </div>\n" +
    "                </div>\n" +
    "\n" +
    "                <div data-vedlegginfoboks data-ng-if=\"kreverVedlegg(b)\">\n" +
    "                    <ul>\n" +
    "                        <li data-ng-if=\"manglendeNorskBarn(b)\">\n" +
    "                            <p data-cmstekster=\"barnetillegg.nyttbarn.land.norge.vedlegginformasjon\"></p>\n" +
    "                        </li>\n" +
    "                        <li data-ng-if=\"barnetHarInntekt(b)\">\n" +
    "                            <p data-cmstekster=\"barnetillegg.barnetilegg.ikkebarneinntekt.false.vedlegginformasjon\"></p>\n" +
    "                        </li>\n" +
    "                    </ul>\n" +
    "                </div>\n" +
    "\n" +
    "                <div class=\"sentrert\" data-ng-show=\"erBrukerregistrert(b)\">\n" +
    "                    <ul class=\"liste-vannrett\">\n" +
    "                        <li>\n" +
    "                            <a href=\"#\" data-ng-click=\"slettBarn(b, $index, $event)\"\n" +
    "                               data-cmstekster=\"barnetillegg.slettbarn\"></a>\n" +
    "                        </li>\n" +
    "                        <li>\n" +
    "                            <a href=\"#\" data-ng-click=\"endreBarn(b.faktumId, $event)\"\n" +
    "                               data-cmstekster=\"barnetillegg.endrebarn\"></a>\n" +
    "                        </li>\n" +
    "                    </ul>\n" +
    "                </div>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"knapper-opprett ikke-fadebakgrunn\" id=\"legg-til-barn\">\n" +
    "            <p data-cmstekster=\"barnetillegg.nyttbarn.informasjon\"></p>\n" +
    "            <button class=\"knapp-leggtil-liten\" href=\"#/nyttbarn\" data-ng-click=\"leggTilBarn($event)\"\n" +
    "                    data-cmstekster=\"barnetillegg.nyttbarn\"></button>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-spmblokkferdig></div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "\n" +
    "");
}]);

angular.module("../views/templates/bekreftelse.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/bekreftelse.html",
    "<div data-ng-controller=\"BekreftelsesCtrl\">\n" +
    "    <div class=\"modalBoks\">\n" +
    "        <div class=\"vertikal\">\n" +
    "            <div class=\"begrensning\">\n" +
    "                <div class=\"sak-totredel\">\n" +
    "                    <div class=\"panel-standard-belyst bekreftelse\">\n" +
    "                        <h2 class=\"stor-ikon-infogronn-strek\" data-cmstekster=\"dagpenger.bekreftelse\">\n" +
    "                        </h2>\n" +
    "                        <p data-cmstekster=\"dagpenger.bekreftelse.informasjon\"></p>\n" +
    "\n" +
    "                        <div class=\"fremdriftsindikator\">\n" +
    "                            <img src=\"../img/ajaxloader/hvit/loader_hvit_64.gif\" alt=\"Fremdriftsindikator\"/>\n" +
    "                        </div>\n" +
    "                    </div>\n" +
    "                </div>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>");
}]);

angular.module("../views/templates/egen-naering.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/egen-naering.html",
    "<div class=\"skjemaramme\">\n" +
    "<div data-ng-form=\"egennaeringForm\" class=\"skjemainnhold\" data-ng-controller=\"EgennaeringCtrl\" data-novalidate>\n" +
    "<div data-form-errors></div>\n" +
    "<div data-nav-faktum=\"egennaering.driveregennaering\"\n" +
    "     data-navconfig\n" +
    "     data-booleanradio\n" +
    "     data-nokkel=\"egennaering.driveregennaering\">\n" +
    "\n" +
    "    <div data-navinfoboks>\n" +
    "        <p data-cmstekster=\"egennaering.driveregennaering.false.informasjon.1\"></p>\n" +
    "\n" +
    "        <p data-cmstekster=\"egennaering.driveregennaering.false.informasjon.2\"></p>\n" +
    "    </div>\n" +
    "\n" +
    "    <div data-ng-repeat=\"egennaering_drivergennaering_orgnummer in orgnummer\">\n" +
    "        <div class=\"orgnummer-repeat\">\n" +
    "            <div data-navorganisasjonsnummerfelt\n" +
    "                 data-nav-faktum=\"egennaering_drivergennaering_orgnummer\"\n" +
    "                 data-navconfig\n" +
    "                 data-navlabel=\"egennaering.driveregennaering.false.organisasjonsnummer\"\n" +
    "                 data-navfeilmelding=\"{ required: 'egennaering.driveregennaering.false.organisasjonsnummer.feilmelding', pattern: 'organisasjonsnummer.format.feilmelding'}\">\n" +
    "            </div>\n" +
    "        </div>\n" +
    "        <a href=\"javascript:void(0)\" aria-role=\"button\" class=\"orgnummer-slett\"\n" +
    "           data-ng-if=\"skalViseSlettKnapp($index)\"\n" +
    "           data-ng-click=\"slettOrg(egennaering_drivergennaering_orgnummer, $index)\"\n" +
    "           data-cmstekster=\"organisasjonsnummer.slett\"\n" +
    "           data-fokus></a>\n" +
    "    </div>\n" +
    "\n" +
    "    <a href=\"javascript:void(0)\" aria-role=\"button\" class=\"orgnummer-leggtil\" data-ng-click=\"leggTilOrgnr()\"\n" +
    "       data-cmstekster=\"organisasjonsnummer.leggtil\" data-leggtil-orgnr></a>\n" +
    "\n" +
    "    <div class=\"tekstfelt form-linje arbeidsmengde-container\">\n" +
    "        <label>\n" +
    "            <span data-cmstekster=\"egennaering.driveregennaering.arbeidsmengde\"></span>\n" +
    "            <input type=\"text\"\n" +
    "                   data-ng-model=\"faktum.value\"\n" +
    "                   data-nav-faktum=\"egennaering.driveregennaering.arbeidsmengde\"\n" +
    "                   data-navconfig\n" +
    "                   data-ng-required=\"true\"\n" +
    "                   data-error-messages=\"{required:'egennaering.driveregennaering.arbeidsmengde.feilmelding',\n" +
    "                   pattern:'regex.tall.komma.punktum'}\"\n" +
    "                   data-ng-pattern=\"/^(\\d+(?:[\\.\\,]\\d{0,2})?)$/\"\n" +
    "                   data-tekstfelt-patternvalidering\n" +
    "                   data-blur-validate/>\n" +
    "            <span class=\"timer\" data-cmstekster=\"egennaering.driveregennaering.arbeidsmengde.timer\"></span>\n" +
    "            <span class=\"melding\"></span>\n" +
    "        </label>\n" +
    "    </div>\n" +
    "</div>\n" +
    "\n" +
    "<div data-nav-faktum=\"egennaering.gardsbruk\"\n" +
    "     data-navconfig\n" +
    "     data-booleanradio\n" +
    "     data-nokkel=\"egennaering.gardsbruk\">\n" +
    "\n" +
    "<div data-navinfoboks>\n" +
    "    <p data-cmstekster=\"egennaering.gardsbruk.false.informasjon.1\">\n" +
    "\n" +
    "\n" +
    "    <p data-cmstekster=\"egennaering.gardsbruk.false.informasjon.2\">\n" +
    "\n" +
    "    <p data-cmstekster=\"egennaering.gardsbruk.false.informasjon.3\">\n" +
    "\n" +
    "</div>\n" +
    "\n" +
    "<div data-navorganisasjonsnummerfelt\n" +
    "     data-nav-faktum=\"egennaering.gardsbruk.false.organisasjonsnummer\"\n" +
    "     data-navconfig\n" +
    "     data-navlabel=\"egennaering.gardsbruk.false.organisasjonsnummer\"\n" +
    "     data-navfeilmelding=\"{ required: 'egennaering.gardsbruk.false.organisasjonsnummer.feilmelding',\n" +
    "          pattern: 'organisasjonsnummer.format.feilmelding'}\"></div>\n" +
    "\n" +
    "<div class=\"form-linje checkbox\" data-checkbox-validate>\n" +
    "    <h4 class=\"spm-sporsmal\">\n" +
    "        <span data-cmstekster=\"egennaering.gardsbruk.false.type.sporsmal\"></span>\n" +
    "    </h4>\n" +
    "\n" +
    "    <input type=\"hidden\" data-ng-model=\"harHuketAvTypeGardsbruk.value\"\n" +
    "           data-ng-required=\"erSynlig('egennaering.gardsbruk')\"\n" +
    "           data-error-messages=\"'egennaering.gardsbruk.false.type.feilmelding'\">\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig\n" +
    "         data-nav-faktum=\"egennaering.gardsbruk.false.type.dyr\"\n" +
    "         data-navlabel=\"egennaering.gardsbruk.false.type.dyr\"\n" +
    "         data-navendret=\"endreTypeGardsbruk()\"></div>\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig\n" +
    "         data-nav-faktum=\"egennaering.gardsbruk.false.type.jord\"\n" +
    "         data-navlabel=\"egennaering.gardsbruk.false.type.jord\"\n" +
    "         data-navendret=\"endreTypeGardsbruk()\"></div>\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig\n" +
    "         data-nav-faktum=\"egennaering.gardsbruk.false.type.skog\"\n" +
    "         data-navlabel=\"egennaering.gardsbruk.false.type.skog\"\n" +
    "         data-navendret=\"endreTypeGardsbruk()\"></div>\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig\n" +
    "         data-nav-faktum=\"egennaering.gardsbruk.false.type.annet\"\n" +
    "         data-navlabel=\"egennaering.gardsbruk.false.type.annet\"\n" +
    "         data-navendret=\"endreTypeGardsbruk()\"></div>\n" +
    "    <span class=\"melding\" data-cmstekster=\"egennaering.gardsbruk.false.type.feilmelding\"></span>\n" +
    "</div>\n" +
    "<div class=\"form-linje checkbox\" data-checkbox-validate>\n" +
    "    <h4 class=\"spm-sporsmal\">\n" +
    "        <span data-cmstekster=\"egennaering.gardsbruk.false.eier.sporsmal\"></span>\n" +
    "    </h4>\n" +
    "\n" +
    "    <input type=\"hidden\" data-ng-model=\"harHuketAvEierGardsbruk.value\"\n" +
    "           data-ng-required=\"erSynlig('egennaering.gardsbruk')\"\n" +
    "           data-error-messages=\"'egennaering.gardsbruk.false.eier.feilmelding'\">\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig\n" +
    "         data-nav-faktum=\"egennaering.gardsbruk.false.eier.jeg\"\n" +
    "         data-navlabel=\"egennaering.gardsbruk.false.eier.jeg\"\n" +
    "         data-navendret=\"endreEierGardsbruk()\"></div>\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig\n" +
    "         data-nav-faktum=\"egennaering.gardsbruk.false.eier.ektefelle\"\n" +
    "         data-navlabel=\"egennaering.gardsbruk.false.eier.ektefelle\"\n" +
    "         data-navendret=\"endreEierGardsbruk()\"></div>\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig\n" +
    "         data-nav-faktum=\"egennaering.gardsbruk.false.eier.annet\"\n" +
    "         data-navlabel=\"egennaering.gardsbruk.false.eier.annet\"\n" +
    "         data-navendret=\"endreEierGardsbruk()\"></div>\n" +
    "\n" +
    "    <span class=\"melding\" data-cmstekster=\"egennaering.gardsbruk.false.eier.feilmelding\"></span>\n" +
    "</div>\n" +
    "\n" +
    "<div class=\"form-linje andelsfordeling-container\" data-ng-class=\"{feil : prosentFeil()} \"\n" +
    "     data-ng-if=\"svartPaHvemEierGardsbruket()\">\n" +
    "    <h4 class=\"spm-sporsmal andelsfordeling-spm\">\n" +
    "        <span data-cmstekster=\"egennaering.gardsbruk.false.eierandel.sporsmal\"></span>\n" +
    "    </h4>\n" +
    "\n" +
    "    <input class=\"tekstfelt\" type=\"hidden\" data-ng-model=\"totalsumAndel.value\"\n" +
    "           data-ng-required=\"svartPaHvemEierGardsbruket()\"\n" +
    "           data-error-messages=\"'egennaering.gardsbruk.false.eierandel.feilmelding'\">\n" +
    "\n" +
    "    <div class=\"andels-fordeling\">\n" +
    "        <div class=\"tekstfelt form-linje\" data-ng-show=\"gardseier('egennaering.gardsbruk.false.eier.jeg')\">\n" +
    "            <label>\n" +
    "                <span data-cmstekster=\"egennaering.gardsbruk.false.eierandel.din\"></span>\n" +
    "                <input data-ng-model=\"faktum.value\"\n" +
    "                       type=\"text\"\n" +
    "                       data-ng-required=\"gardseier('egennaering.gardsbruk.false.eier.jeg')\"\n" +
    "                       data-error-messages=\"{required:'egennaering.gardsbruk.false.eierandel.din.feilmelding', pattern:'regex.tall'}\"\n" +
    "                       data-ng-pattern=\"/^(\\d+(?:[\\.\\,]\\d{0,2})?)$/\"\n" +
    "                       data-blur-validate\n" +
    "                       data-ng-blur=\"summererAndeleneTil100()\"\n" +
    "                       maxlength=\"3\"\n" +
    "                       data-tekstfelt-patternvalidering\n" +
    "                       data-nav-faktum=\"egennaering.gardsbruk.false.eierandel.din\"\n" +
    "                       data-navconfig/>\n" +
    "                <span class=\"egennaering-prosent\" data-cmstekster=\"egennaering.prosent\"></span>\n" +
    "                <span class=\"melding\"></span>\n" +
    "            </label>\n" +
    "        </div>\n" +
    "        <div class=\"tekstfelt form-linje\" data-ng-show=\"gardseier('egennaering.gardsbruk.false.eier.ektefelle')\">\n" +
    "            <label>\n" +
    "                <span data-cmstekster=\"egennaering.gardsbruk.false.eierandel.ektefelle\"></span>\n" +
    "                <input data-ng-model=\"faktum.value\"\n" +
    "                       type=\"text\"\n" +
    "                       data-ng-required=\"gardseier('egennaering.gardsbruk.false.eier.ektefelle')\"\n" +
    "                       data-error-messages=\"{required:'egennaering.gardsbruk.false.eierandel.ektefelle.feilmelding', pattern:'regex.tall'}\"\n" +
    "                       data-ng-pattern=\"/^(\\d+(?:[\\.\\,]\\d{0,2})?)$/\"\n" +
    "                       data-blur-validate\n" +
    "                       data-ng-blur=\"summererAndeleneTil100()\"\n" +
    "                       maxlength=\"3\"\n" +
    "                       data-tekstfelt-patternvalidering\n" +
    "                       data-nav-faktum=\"egennaering.gardsbruk.false.eierandel.ektefelle\"\n" +
    "                       data-navconfig/>\n" +
    "                <span class=\"egennaering-prosent\" data-cmstekster=\"egennaering.prosent\"></span>\n" +
    "                <span class=\"melding\"></span>\n" +
    "            </label>\n" +
    "        </div>\n" +
    "        <div class=\"tekstfelt form-linje\" data-ng-show=\"gardseier('egennaering.gardsbruk.false.eier.annet')\">\n" +
    "            <label>\n" +
    "                <span data-cmstekster=\"egennaering.gardsbruk.false.eierandel.annet\"></span>\n" +
    "                <input data-ng-model=\"faktum.value\"\n" +
    "                       type=\"text\"\n" +
    "                       data-ng-required=\"gardseier('egennaering.gardsbruk.false.eier.annet')\"\n" +
    "                       data-error-messages=\"{required:'egennaering.gardsbruk.false.eierandel.annet.feilmelding', pattern:'regex.tall'}\"\n" +
    "                       data-ng-pattern=\"/^(\\d+(?:[\\.\\,]\\d{0,2})?)$/\"\n" +
    "                       data-blur-validate\n" +
    "                       data-ng-blur=\"summererAndeleneTil100()\"\n" +
    "                       maxlength=\"3\"\n" +
    "                       data-tekstfelt-patternvalidering\n" +
    "                       data-nav-faktum=\"egennaering.gardsbruk.false.eierandel.annet\"\n" +
    "                       data-navconfig/>\n" +
    "                <span class=\"egennaering-prosent\" data-cmstekster=\"egennaering.prosent\"></span>\n" +
    "                <span class=\"melding\"></span>\n" +
    "            </label>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <span class=\"prosentmelding\" data-cmstekster=\"egennaering.gardsbruk.false.eierandel.feilmelding\"></span>\n" +
    "</div>\n" +
    "\n" +
    "\n" +
    "<div class=\"gardsbruk-timer\">\n" +
    "    <h4 class=\"spm-sporsmal\">\n" +
    "        <span data-cmstekster=\"egennaering.gardsbruk.false.arbeidsforbruk\"></span>\n" +
    "    </h4>\n" +
    "\n" +
    "    <div class=\"tekstfelt form-linje arbeidsmengde-container\">\n" +
    "        <label>\n" +
    "            <span data-cmstekster=\"egennaering.gardsbruk.false.timer\"></span>\n" +
    "            <input data-ng-model=\"faktum.value\"\n" +
    "                   type=\"text\"\n" +
    "                   data-ng-required=\"erSynlig('egennaering.gardsbruk')\"\n" +
    "                   data-error-messages=\"{required: 'egennaering.gardsbruk.false.timer.feilmelding', pattern: 'regex.tall.komma.punktum'}\"\n" +
    "                   data-ng-pattern=\"/^(\\d+(?:[\\.\\,]\\d{0,2})?)$/\"\n" +
    "                   data-blur-validate\n" +
    "                   data-tekstfelt-patternvalidering\n" +
    "                   data-nav-faktum=\"egennaering.gardsbruk.false.timer\"\n" +
    "                   data-navconfig/>\n" +
    "            <span class=\"melding\"></span>\n" +
    "        </label>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"tekstfelt form-linje\">\n" +
    "        <label>\n" +
    "            <span data-cmstekster=\"egennaering.gardsbruk.false.aar\"></span>\n" +
    "            <select id=\"egennaeringgardsbrukaar\"\n" +
    "                    data-ng-model=\"faktum.value\"\n" +
    "                    data-nav-faktum=\"egennaering.gardsbruk.false.aar\"\n" +
    "                    data-navconfig\n" +
    "                    data-error-messages=\"aarstall.feilmelding\"\n" +
    "                    data-ng-init=\"faktum.value = faktum.value || forrigeAar; lagreFaktum();\"\n" +
    "                    data-ng-options=\"aar for aar in aarstall\"\n" +
    "                    data-ng-change=\"lagreFaktum()\"/>\n" +
    "        </label>\n" +
    "    </div>\n" +
    "</div>\n" +
    "\n" +
    "<div data-navinfoboks>\n" +
    "    <p data-cmstekster=\"egennaering.gardsbruk.arbeid.informasjon\"></p>\n" +
    "</div>\n" +
    "\n" +
    "<div class=\"timeredegjoring\">\n" +
    "    <div data-navtextarea\n" +
    "         data-navconfig\n" +
    "         data-nav-faktum=\"egennaering.gardsbruk.false.timeredegjoring\"\n" +
    "         data-nokkel=\"egennaering.gardsbruk.false.timeredegjoring\"\n" +
    "         data-maxlengde=\"500\"\n" +
    "         data-navfeilmelding=\"egennaering.gardsbruk.false.timeredegjoring.feilmelding\"\n" +
    "         data-obligatorisk=\"true\"></div>\n" +
    "</div>\n" +
    "\n" +
    "<div data-vedlegginfoboks>\n" +
    "    <ul>\n" +
    "        <li>\n" +
    "            <p data-cmstekster=\"egennaering.gardsbruk.false.vedlegg\"></p>\n" +
    "            <a href=\"egennaering.gardsbruk.false.vedlegg.lenkeurl\"\n" +
    "               data-cmstekster=\"egennaering.gardsbruk.false.vedlegg.lenketekst\"></a>\n" +
    "        </li>\n" +
    "    </ul>\n" +
    "</div>\n" +
    "</div>\n" +
    "\n" +
    "<div data-nav-faktum=\"egennaering.fangstogfiske\"\n" +
    "     data-navconfig\n" +
    "     data-booleanradio\n" +
    "     data-nokkel=\"egennaering.fangstogfiske\">\n" +
    "\n" +
    "    <div data-navinfoboks>\n" +
    "        <p data-cmstekster=\"egennaering.fangstogfiske.false.arbeid.informasjon\"></p>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"form-linje boolean\">\n" +
    "        <h4 class=\"spm-sporsmal\" data-cmstekster=\"egennaering.fangstogfiske.false.arbeid.sporsmal\"></h4>\n" +
    "\n" +
    "        <div data-navradio\n" +
    "             data-navconfig\n" +
    "             data-nav-faktum=\"egennaering.fangstogfiske.false.arbeid\"\n" +
    "             data-value=\"true\"\n" +
    "             data-navlabel=\"egennaering.fangstogfiske.false.arbeid.true\"\n" +
    "             data-navfeilmelding=\"egennaering.fangstogfiske.false.arbeid.feilmelding\"></div>\n" +
    "\n" +
    "        <div data-navradio\n" +
    "             data-navconfig\n" +
    "             data-nav-faktum=\"egennaering.fangstogfiske.false.arbeid\"\n" +
    "             data-value=\"false\"\n" +
    "             data-navlabel=\"egennaering.fangstogfiske.false.arbeid.false\"\n" +
    "             data-navfeilmelding=\"egennaering.fangstogfiske.false.arbeid.feilmelding\"></div>\n" +
    "        <span class=\"melding\"></span>\n" +
    "    </div>\n" +
    "\n" +
    "    <div data-ng-if=\"erSynlig('egennaering.fangstogfiske.false.arbeid')\">\n" +
    "        <div class=\"fangstfiskeinntekt\">\n" +
    "            <div data-navtekst\n" +
    "                 data-nav-faktum=\"egennaering.fangstogfiske.false.arbeid.false.inntekt\"\n" +
    "                 data-navconfig\n" +
    "                 data-navlabel=\"egennaering.fangstogfiske.false.arbeid.false.inntekt\"\n" +
    "                 data-navfeilmelding=\"{required: 'egennaering.fangstogfiske.false.arbeid.false.inntekt.feilmelding', pattern: 'regex.tall.komma.punktum'}\"\n" +
    "                 data-regexvalidering=\"/^(\\d+(?:[\\.\\,]\\d{0,2})?)$/\">\n" +
    "            </div>\n" +
    "        </div>\n" +
    "        <div data-vedlegginfoboks>\n" +
    "            <ul>\n" +
    "                <li><p data-cmstekster=\"egennaering.fangstogfiske.false.arbeid.vedlegg\"></p></li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "<div data-spmblokkferdig></div>\n" +
    "</div>\n" +
    "</div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../views/templates/feilside.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/feilside.html",
    "<div data-nav-tittel=\"skjema.tittel\"></div>\n" +
    "<div data-stegindikator data-steg-liste=\"veiledning, skjema, vedlegg, sendInn\" data-aktiv-index=\"0\"></div>      \n" +
    "<div id=\"feilside\" data-ng-controller=\"FeilSideCtrl\">\n" +
    "    <div data-panelbelyst>\n" +
    "        <div class=\"feil\">\n" +
    "            <div class=\"utrop-sirkel-ikon\"></div>\n" +
    "            <h1 class=\"h1-strek\" data-cmstekster=\"feilmelding\"></h1>\n" +
    "            \n" +
    "            <div class=\"generellfeil\" data-ng-form=\"epostForm\" data-ng-controller=\"FortsettSenereCtrl\" data-novalidate>\n" +
    "\n" +
    "                <div class=\"form-linje\">\n" +
    "                    <p>{{ 'feilmelding.innsending.1' | cmstekst }}</p>\n" +
    "                </div>\n" +
    "\n" +
    "\n" +
    "                <div class=\"send-epost\">\n" +
    "                    <div class=\"input-boks\">\n" +
    "                        <div class=\"form-linje\">\n" +
    "                            <label>\n" +
    "                                <span data-cmstekster=\"dagpenger.fortsettSenere.epost.label\"> </span>\n" +
    "                                <input type=\"email\" name=\"epost\" role=\"textbox\" data-ng-model=\"epost.value\"\n" +
    "                                        data-error-messages=\"{required: 'dagpenger.fortsettSenere.epost.required.feilmelding', pattern: 'dagpenger.fortsettSenere.epost.pattern.feilmelding' }\"\n" +
    "                                        data-ng-required=\"true\" data-ng-pattern=\"/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$/\" data-blur-validate/>\n" +
    "                            </label>\n" +
    "                            <input type=\"submit\" class=\"knapp-hoved-liten\" data-cmstekster=\"dagpenger.fortsettSenere.epost.send\" role=\"button\" data-ng-click=\"forsettSenere(epostForm)\"/>\n" +
    "                            <span class=\"melding\"></span>\n" +
    "                        </div>\n" +
    "                    </div>\n" +
    "                </div>\n" +
    "\n" +
    "                <div class=\"form-linje\">\n" +
    "                    <p>\n" +
    "                        <span>{{ 'feilmelding.innsending.2' | cmstekst }}</span>\n" +
    "                        <a href=\"{{mineInnsendinger}}\">{{ 'feilmelding.mineinnsedinger.lenketekst' | cmstekst }}</a>\n" +
    "                    </p>\n" +
    "                </div>\n" +
    "           \n" +
    "           </div>\n" +
    "\n" +
    "\n" +
    "\n" +
    "\n" +
    "            <div class=\"knapper\">\n" +
    "                <a type=\"button\" class=\"knapp-hoved\" href=\"{{inngangsportenUrl}}\" data-cmstekster=\"feilmelding.lenketekst\"></a>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../views/templates/ferdigstilt.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/ferdigstilt.html",
    "<div data-ng-controller=\"FerdigstiltCtrl\">\n" +
    "    <div class=\"rad-belyst soknad\" data-sidetittel=\"sidetittel.skjema\">\n" +
    "        <section class=\"sak-hel\">\n" +
    "            <div class=\"panel-standard-belyst skjema\">\n" +
    "                <h2 class=\"stor strek-ikon-info-orange\" data-cmstekster=\"soknad.ferdigstilt\"></h2>\n" +
    "                <div class=\"egen-linje\">\n" +
    "                        <a href=\"{{mineHenveldelserUrl}}\" id=\"slettetMinehenvendelser\"\n" +
    "                            role=\"link\" data-cmstekster=\"slettet.videretilnav\"></a>\n" +
    "                </div>\n" +
    "            </div>\n" +
    "        </section>\n" +
    "    </div>\n" +
    "</div>");
}]);

angular.module("../views/templates/fortsettSenere.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/fortsettSenere.html",
    "<div data-panelbelyst id=\"fortsett-senere\" data-sidetittel=\"sidetittel.fortsett\">\n" +
    "	<div class=\"skjemaramme\">\n" +
    "		<div data-ng-controller=\"FortsettSenereCtrl\" data-ng-form=\"epostForm\" data-novalidate class=\"epost-input vertikal\">\n" +
    "			<h2 class=\"stor\" data-cmstekster=\"dagpenger.fortsettSenere.tittel\"></h2>\n" +
    "			<p data-cmstekster=\"dagpenger.fortsettSenere.info\"></p>\n" +
    "			<div class=\"send-epost\">\n" +
    "				<div class=\"input-boks\">\n" +
    "					<div class=\"form-linje\">\n" +
    "						<label>\n" +
    "							<span data-cmstekster=\"dagpenger.fortsettSenere.epost.label\"> </span>\n" +
    "							<input type=\"email\" name=\"epost\" role=\"textbox\" data-ng-model=\"epost.value\"\n" +
    "									data-error-messages=\"{required: 'dagpenger.fortsettSenere.epost.required.feilmelding', pattern: 'dagpenger.fortsettSenere.epost.pattern.feilmelding' }\"\n" +
    "									data-ng-required=\"true\" data-ng-pattern=\"/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$/\" data-blur-validate/>\n" +
    "						</label>\n" +
    "						<input type=\"submit\" class=\"knapp-hoved-liten\" data-cmstekster=\"dagpenger.fortsettSenere.epost.send\" role=\"button\" data-ng-click=\"forsettSenere(epostForm)\"/>\n" +
    "						<span class=\"melding\"></span>\n" +
    "					</div>\n" +
    "				</div>\n" +
    "			</div>\n" +
    "\n" +
    "			<div>\n" +
    "				<p data-cmstekster=\"dagpenger.fortsettSenere.info.frister\"></p>\n" +
    "				<p data-cmstekster=\"dagpenger.fortsettSenere.info.konsekvenser\"></p>\n" +
    "			</div>\n" +
    "			<div class=\"lenker-dot\">\n" +
    "				<a href=\"#/soknad\" id=\"tilOversikt\" role=\"link\" data-cmstekster=\"dagpenger.fortsettSenere.tilbake\"></a>\n" +
    "				<a href=\"{{inngangsportenUrl}}\" id=\"dittnav\" role=\"link\" data-cmstekster=\"dagpenger.fortsettSenere.dittNav\"></a>\n" +
    "			</div>\n" +
    "		</div>\n" +
    "	</div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../views/templates/fritekst.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/fritekst.html",
    "<div class=\"skjemaramme\">\n" +
    "    <div data-ng-form=\"fritekstForm\" class=\"skjemainnhold vertikal\" data-novalidate>\n" +
    "        <div data-form-errors></div>\n" +
    "        <div data-ng-form=\"frivilligForm\" class=\"textarea-form\" data-novalidate>\n" +
    "            <div id=\"frivillig-textarea\">\n" +
    "                <div data-navtextarea\n" +
    "                     data-ng-model=\"faktum.value\"\n" +
    "                     data-navconfig\n" +
    "                     data-nav-faktum=\"fritekst\"\n" +
    "                     data-nokkel=\"fritekst\"\n" +
    "                     data-maxlengde=\"500\"\n" +
    "                     maxlength=\"500\"\n" +
    "                     data-navfeilmelding=\"'fritekst.feilmelding'\">\n" +
    "                  </div>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>");
}]);

angular.module("../views/templates/gjenoppta/skjema-ferdig.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/gjenoppta/skjema-ferdig.html",
    "");
}]);

angular.module("../views/templates/gjenoppta/skjema-sendt.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/gjenoppta/skjema-sendt.html",
    "");
}]);

angular.module("../views/templates/gjenoppta/skjema-under-arbeid.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/gjenoppta/skjema-under-arbeid.html",
    "");
}]);

angular.module("../views/templates/gjenoppta/skjema-validert.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/gjenoppta/skjema-validert.html",
    "");
}]);

angular.module("../views/templates/ikkekvalifisert.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/ikkekvalifisert.html",
    "<p> Du er dessverre ikke kvalifisert til å få dagpenger </p>\n" +
    "<a href=\"#/soknad\">Tilbake</a>");
}]);

angular.module("../views/templates/informasjonsside.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/informasjonsside.html",
    "<div data-nav-tittel=\"skjema.tittel\"></div>\n" +
    "\n" +
    "<div data-stegindikator data-steg-liste=\"veiledning, skjema, vedlegg, sendInn\" data-aktiv-index=\"0\"></div>\n" +
    "\n" +
    "<div id=\"informasjonsside\" data-sidetittel=\"sidetittel.informasjon\" data-ng-cloak>\n" +
    "	<div data-panelbelyst>\n" +
    "        <div class=\"feil\" data-ng-show=\"tpsSvarerIkke()\">\n" +
    "        	<div class=\"utrop-sirkel-ikon\"></div>\n" +
    "        	<h1 data-cmstekster=\"feilmelding\"></h1>\n" +
    "       		<p class=\"mini-strek\">{{ 'feilmelding.tps' | cmstekst }}</p>\n" +
    "			<div class=\"knapper\">\n" +
    "       			<a type=\"button\" class=\"knapp-hoved\" href=\"{{inngangsportenUrl}}\" data-cmstekster=\"feilmelding.lenketekst\"></a>\n" +
    "       		</div>\n" +
    "       	</div>\n" +
    "       	<div data-ng-show=\"tpsSvarer()\">\n" +
    "	        <h2 data-ng-show=\"kravForDagpengerOppfylt()\" class=\"stor strek-ikon-info-orange\" data-cmstekster=\"dagpenger.informasjonsside.tittel\"></h2>\n" +
    "	    	\n" +
    "			<div id=\"utslagskriterier\" data-ng-if=\"kravForDagpengerIkkeOppfylt()\">\n" +
    "				<div class=\"utslagskriterie\" data-ng-if=\"ikkeBosattINorge()\">\n" +
    "					<h2 class=\"stor strek-ikon-info-orange\" data-cmstekster=\"dagpenger.informasjonsside.ikkebosattinorge.tittel\"></h2>\n" +
    "					\n" +
    "					<p class=\"label\" data-cmstekster=\"utslagskriterier.utslag.bosted.label\"></p>\n" +
    "					<div class=\"adresse\">\n" +
    "						<div data-ng-repeat=\"linje in hentAdresseLinjer()\">\n" +
    "                            <span>{{ linje }}</span>\n" +
    "                        </div>\n" +
    "					</div>\n" +
    "			\n" +
    "					<ul>\n" +
    "						<li>\n" +
    "							<p data-cmstekster=\"utslagskriterier.utslag.bosted.liste.1\"></p>\n" +
    "						</li>\n" +
    "						<li>\n" +
    "							<p data-cmstekster=\"utslagskriterier.utslag.bosted.liste.2\"></p>\n" +
    "						</li>\n" +
    "					</ul>\n" +
    "				</div>\n" +
    "\n" +
    "				<div class=\"utslagskriterie\" data-ng-if=\"ikkeGyldigAlder()\">\n" +
    "					<h2 class=\"stor strek-ikon-info-orange\" data-cmstekster=\"dagpenger.informasjonsside.ikkegyldigalder.tittel\"></h2>\n" +
    "					<ul>\n" +
    "						<li>\n" +
    "							<span data-cmstekster=\"utslagskriterier.utslag.alder.1\"></span>\n" +
    "							<a data-cmstekster=\"utslagskriterier.utslag.alder.2.lenketekst\" href=\"{{alderspensjonUrl}}\"></a>\n" +
    "						</li>\n" +
    "					</ul>\n" +
    "				</div>\n" +
    "\n" +
    "                <div class=\"utslagskriterie\" data-ng-if=\"ikkeRegistrertArbeidssoker()\">\n" +
    "                    <h2 class=\"stor strek-ikon-info-orange\" data-cmstekster=\"dagpenger.informasjonsside.ikkegyldigarbeidssoker.tittel\"></h2>\n" +
    "                    <ul>\n" +
    "                        <li>\n" +
    "                            <span data-cmstekster=\"utslagskriterier.utslag.reellarbeidssoker\"></span>\n" +
    "                            <a href=\"{{reelArbeidsokerUrl}}\" data-cmstekster=\"utslagskriterier.utslag.reellarbeidssoker.registert\"></a>\n" +
    "                        </li>\n" +
    "                    </ul>\n" +
    "                </div>\n" +
    "			</div>\n" +
    "\n" +
    "			<div class=\"viktig-info\" data-ng-show=\"kravForDagpengerOppfylt()\">\n" +
    "				<ul class=\"informasjonsliste\">\n" +
    "					<li>\n" +
    "						<p class=\"medium\" data-cmstekster=\"dagpenger.informasjonsside.informasjon.liste.1\"></p>\n" +
    "					</li>\n" +
    "					<li>\n" +
    "						<p class=\"medium\" data-cmstekster=\"dagpenger.informasjonsside.informasjon.liste.2\"></p>\n" +
    "						<a href=\"{{dagpengerBrosjyreUrl}}\" data-cmstekster=\"dagpenger.informasjonsside.informasjon.liste.2.lenketekst\"> </a>\n" +
    "					</li>\n" +
    "                    <li data-ng-if=\"registrertArbeidssokerUkjent()\">\n" +
    "                        <p class=\"medium\" data-cmstekster=\"dagpenger.informasjonsside.informasjon.liste.3\"></p>\n" +
    "                    </li>\n" +
    "				</ul>\n" +
    "\n" +
    "\n" +
    "				<form data-ng-if=\"oppsummering != true\">\n" +
    "					<div class=\"nav-checkbox\">\n" +
    "						<input id=\"lestBrosjyre\" data-ng-model=\"utslagskriterier.harlestbrosjyre\" type=\"checkbox\"	/>\n" +
    "						<label for=\"lestBrosjyre\" data-cmstekster=\"dagpenger.informasjonsside.lestbrosjyre.sporsmal\"></label>\n" +
    "					</div>\n" +
    "				</form>\n" +
    "				\n" +
    "				<div id=\"oppsummering\" data-ng-if=\"oppsummering == true\">\n" +
    "					<span class=\"sjekket\"></span>\n" +
    "                    <span>{{ 'dagpenger.informasjonsside.lestbrosjyre.sporsmal' | cmstekst }}</span>\n" +
    "				</div>\n" +
    "\n" +
    "\n" +
    "				<div class=\"brosjyre-info\" data-ng-show=\"skalViseBrosjyreMelding\">\n" +
    "					<div data-navinfoboks>|\n" +
    "						<span data-cmstekster=\"dagpenger.informasjonsside.lestbrosjyre.feilmelding\"> </span>\n" +
    "					</div>\n" +
    "				</div>\n" +
    "			</div>\n" +
    "\n" +
    "\n" +
    "			<div class=\"utslagskriterier-knapper\" data-ng-show=\"kravForDagpengerOppfylt()\">\n" +
    "				<div data-ng-show=\"soknadErStartet()\">\n" +
    "					<input type=\"button\" class=\"knapp-hoved\" data-cmstekster=\"utslagskriterier.utslag.fortsettSoknad\" data-ng-click=\"forsettSoknadDersomBrosjyreLest()\" data-fremdriftsindikator/>\n" +
    "				</div>\n" +
    "				<div data-ng-show=\"soknadErIkkeStartet()\">\n" +
    "					<input type=\"button\" class=\"knapp-hoved\" data-cmstekster=\"utslagskriterier.utslag.fortsett\" data-ng-click=\"startSoknadDersomBrosjyreLest()\" data-fremdriftsindikator/>\n" +
    "					<a href=\"{{inngangsportenUrl}}\" data-cmstekster=\"utslagskriterier.utslag.avbryt.lenketekst\"/>\n" +
    "				</div>\n" +
    "			</div>\n" +
    "\n" +
    "			<div class=\"utslagskriterier-knapper\" data-ng-show=\"kravForDagpengerIkkeOppfylt()\">\n" +
    "				<a type=\"button\" class=\"knapp-hoved\" href=\"{{inngangsportenUrl}}\" data-cmstekster=\"utslagskriterier.utslag.avbryt.lenketekst\"/>\n" +
    "				<p>\n" +
    "					<a href=\"#\" data-cmstekster=\"utslagskriterier.utslag.fortsettlikevel\" data-ng-click=\"fortsettLikevel($event)\" data-fremdriftsindikator/>\n" +
    "					<span data-cmstekster=\"utslagskriterier.utslag.infoAvslag\"> </span>\n" +
    "				</p>\n" +
    "			</div>\n" +
    "		</div>\n" +
    "	</div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../views/templates/kvittering-fortsettsenere.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/kvittering-fortsettsenere.html",
    "<div data-panelbelyst id=\"kvittering\">\n" +
    "	<div data-ng-controller=\"FortsettSenereKvitteringCtrl\">\n" +
    "        <h2 class=\"stor\" data-cmstekster=\"dagpenger.fortsettSenere.kvittering.tittel\"></h2>\n" +
    "    	<p data-cmstekster=\"dagpenger.fortsettSenere.kvittering.tekst\"></p>\n" +
    "        <span> {{epost.value}}</span>\n" +
    "    	<a href=\"#/fortsettsenere\" data-cmstekster=\"dagpenger.fortsettSenere.kvittering.sendpaanyttlink\"></a>\n" +
    "    	<p data-cmstekster=\"dagpenger.fortsettSenere.kvittering.frist\"></p>\n" +
    "    	<p data-cmstekster=\"dagpenger.fortsettSenere.kvittering.konsekvens-vente\"></p>\n" +
    "    	<a href=\"#/soknad\" data-cmstekster=\"dagpenger.fortsettSenere.fortsettlink\"></a>\n" +
    "    	<a href=\"{{inngangsportenUrl}}\" id=\"dittnav\" role=\"link\" data-cmstekster=\"dagpenger.fortsettSenere.dittNav\"></a>\n" +
    "    </div>\n" +
    "</div>\n" +
    "\n" +
    "\n" +
    "");
}]);

angular.module("../views/templates/kvittering-innsendt.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/kvittering-innsendt.html",
    "<panelbelyst id=\"kvittering\">\n" +
    "	<h2 class=\"stor\" data-cmstekster=\"dagpenger.kvittering.tittel\"></h2>\n" +
    "	<p data-cmstekster=\"dagpenger.kvittering.info\"></p>\n" +
    "</panelbelyst>\n" +
    "");
}]);

angular.module("../views/templates/opplasting.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/opplasting.html",
    "<div data-nav-tittel=\"skjema.tittel\"></div>\n" +
    "<div stegindikator steg-liste=\"veiledning, skjema, vedlegg, sendInn\" aktiv-index=\"2\"></div>\n" +
    "\n" +
    "<div id=\"opplasting\" class=\"soknad rad-belyst\" data-sidetittel=\"sidetittel.opplasting\" data-ng-controller=\"OpplastingVedleggCtrl\">\n" +
    "    <div class=\"begrensning sak-halv\">\n" +
    "        <div class=\"panel uten-ramme\">\n" +
    "            <h1 class=\"stor-ikon-vedlegg-strek\" ><span data-ng-if=\"vedlegg.vedleggId\">{{vedlegg.tittel}}</span><span\n" +
    "                    data-ng-if=\"vedlegg.navn\">: {{vedlegg.navn}}</span></h1>\n" +
    "\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <form id=\"opplastingform\" method=\"POST\" enctype=\"multipart/form-data\" data-file-upload=\"options\" data-ng-controller=\"OpplastingCtrl\">\n" +
    "        <div class=\"begrensning sak-trekvart\">\n" +
    "            <div id=\"opplastingFeilmelding\" class=\"opplastingfeil\" style=\"width: 73%;margin: 0 auto; \" data-ng-if=\"data.opplastingFeilet\" data-ng-bind=\"data.opplastingFeilet\">dette er en feil</div>\n" +
    "            <section class=\"panel-standard oversikt\">\n" +
    "                <ul class=\"opplastingliste clearfix\">\n" +
    "                    <li data-ng-repeat=\"file in queue\" data-ng-class=\"{'lasteropp': file.$processing()}\" data-fil-feil>\n" +
    "                        <a class=\"lukk\" data-ng-if=\"!!file.vedleggId\" data-ng-click=\"file.$destroy()\" data-ng-controller=\"SlettOpplastingCtrl\" href=\"javascript:void(0);\" data-aria-label=\"Slett siden\"></a>\n" +
    "                        <img data-async-image=\"../rest/soknad/{{file.soknadId}}/vedlegg/{{file.vedleggId}}/thumbnail\" data-ng-if=\"!!file.vedleggId\">\n" +
    "                    </li>\n" +
    "                    <li class=\"leggtil\">\n" +
    "                        <input id=\"leggtil\" type=\"file\" name=\"files[]\" class=\"vekk\" multiple accept=\"image/jpeg,image/png,application/pdf\" data-fokus>\n" +
    "                        <label for=\"leggtil\" class=\"robust\" data-cmstekster=\"opplasting.leggtil\"></label>\n" +
    "                    </li>\n" +
    "                </ul>\n" +
    "            </section>\n" +
    "        </div>\n" +
    "        <div class=\"rad uten-ramme\">\n" +
    "            <div class=\"begrensning sak-totredel\">\n" +
    "                <div class=\"form-linje\" data-ng-class=\"{feil: skalViseFeilmelding === true}\">\n" +
    "                    <span class=\"melding\" data-cmstekster=\"opplasting.feilmelding.manglerVedlegg\"></span>\n" +
    "                </div>\n" +
    "                <a href=\"javascript:void(0);\" data-redirect=\"#/vedlegg\" class=\"knapp-hoved\" data-cmstekster=\"opplasting.ferdig\" data-ng-click=\"leggVed()\"\n" +
    "                   data-fremdriftsindikator></a>\n" +
    "                <a href=\"javascript:void(0);\" data-ng-href=\"#/vedlegg\" class=\"avbryt\" data-cmstekster=\"opplasting.avbryt\"></a>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "    </form>\n" +
    "</div>\n" +
    "\n" +
    "\n" +
    "");
}]);

angular.module("../views/templates/oppsummering.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/oppsummering.html",
    "<div id=\"oppsummering\" data-ng-controller=\"OppsummeringCtrl\">\n" +
    "    <div data-stegindikator data-steg-liste=\"veiledning, skjema, vedlegg, sendInn\" data-aktiv-index=\"3\"></div>\n" +
    "    <div data-panelbelyst data-sidetittel=\"sidetittel.skjema\">\n" +
    "        <div class=\"oppsummering\" data-ng-bind-html=\"oppsummeringHtml\"></div>\n" +
    "        <form data-ng-submit=\"sendSoknad()\">\n" +
    "\n" +
    "            <div class=\"checkbox form-linje\" data-ng-class=\"{feilstyling : skalViseFeilmelding.value === true}\"\n" +
    "                 data-checkbox-validate>\n" +
    "                <div class=\"dagpengerbrosjyre\">\n" +
    "                    <span class=\"sjekket\"></span>\n" +
    "                    <span>{{ 'dagpenger.informasjonsside.lestbrosjyre.sporsmal' | cmstekst }}</span>\n" +
    "                </div>\n" +
    "\n" +
    "                <input id=\"bekreftelseOpplysninger\" type=\"checkbox\" data-ng-model=\"harbekreftet.value\">\n" +
    "                <label for=\"bekreftelseOpplysninger\" data-cmstekster=\"dagpenger.oppsummering.bekreftOpplysninger\"></label>\n" +
    "                <span class=\"melding\" data-cmstekster=\"dagpenger.oppsummering.bekreftelse.feilmelding\"></span>\n" +
    "            </div>\n" +
    "            <div class=\"oppsummering-knapp\">\n" +
    "                <input class=\"knapp-hoved\" type=\"submit\" value=\"{{ 'dagpenger.oppsummering.send' | cmstekst }}\">\n" +
    "            </div>\n" +
    "            <div data-sist-lagret data-navtilbakelenke=\"tilbake.til.vedlegg.lenke\"></div>\n" +
    "        </form>\n" +
    "    </div>\n" +
    "</div>");
}]);

angular.module("../views/templates/personalia.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/personalia.html",
    "<div class=\"skjemaramme\">\n" +
    "    <div data-ng-form=\"personaliaForm\" class=\"skjemainnhold\" data-ng-controller=\"PersonaliaCtrl\" data-novalidate>\n" +
    "        <div class=\"informasjon\">\n" +
    "            <span class=\"informasjonstekst\" data-cmstekster=\"personalia.intro\"></span>\n" +
    "            <a target=\"_blank\" data-cmstekster=\"personalia.intro.tekst\" href=\"{{brukerprofilUrl}}\"></a>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-ng-show=\"harHentetPersonalia()\">\n" +
    "            <div class=\"personalia\">\n" +
    "                <div class=\"alderOgKjonn\" data-ng-class=\"{mann: erMann(), kvinne: erKvinne()}\">\n" +
    "                    <span data-cmstekster=\"aar\"></span>\n" +
    "                    <span class=\"alder robust\">{{ personalia.alder }}</span>\n" +
    "                </div>\n" +
    "                <div class=\"personInfo\">\n" +
    "                    <div class=\"navnOgFnr\">\n" +
    "                        <span>{{ personalia.navn }}</span>\n" +
    "                        <span>{{ personalia.fnr }}</span>\n" +
    "                    </div>\n" +
    "\n" +
    "                    <div data-ng-include=\"'../views/templates/adresse.html'\"></div>\n" +
    "                </div>\n" +
    "\n" +
    "                <div data-ng-if=\"erUtenlandskStatsborger()\">\n" +
    "                    <div class=\"statsborgerskap\" data-navinfoboks >\n" +
    "                            {{ 'personalia.utenlandskstatsborger.statsborgerskap.informasjon' | cmstekst }}\n" +
    "                        </div>\n" +
    "                        <div class=\"ekstra-spm-boks\" data-vedlegginfoboks>\n" +
    "                            <ul>\n" +
    "                                <li>\n" +
    "                                    {{ 'personalia.utenlandskstatsborger.statsborgerskap.dokumentasjon' | cmstekst}}\n" +
    "                                </li>\n" +
    "                            </ul>\n" +
    "                        </div>\n" +
    "                    </div>\n" +
    "                </div>\n" +
    "            </div>\n" +
    "              <div data-spmblokkferdig></div>\n" +
    "        </div>\n" +
    "\n" +
    "    </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../views/templates/reellarbeidssoker/reell-arbeidssoker.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/reellarbeidssoker/reell-arbeidssoker.html",
    "<div class=\"skjemaramme\">\n" +
    "    <div data-ng-form=\"reellarbeidssokerForm\" class=\"skjemainnhold vertikal\" data-ng-controller=\"ReellarbeidssokerCtrl\"\n" +
    "         novalidate>\n" +
    "\n" +
    "        <div data-form-errors></div>\n" +
    "\n" +
    "        <div data-nav-faktum=\"reellarbeidssoker.villigdeltid\"\n" +
    "             data-ng-model=\"faktum.value\"\n" +
    "             data-navconfig\n" +
    "             data-booleanradio\n" +
    "             data-nokkel=\"reellarbeidssoker.villigdeltid\">\n" +
    "            <div data-ng-if=\"erUnder60Aar()\" data-navinfoboks>\n" +
    "                <span data-cmstekster=\"reellarbeidssoker.villigdeltid.false.informasjon.under60\"></span>\n" +
    "            </div>\n" +
    "            <div data-ng-if=\"erOver59Aar()\" data-navinfoboks>\n" +
    "                <span data-cmstekster=\"reellarbeidssoker.villigdeltid.false.informasjon.over60\"></span>\n" +
    "            </div>\n" +
    "\n" +
    "            <div class=\"form-linje checkbox\" data-ng-if=\"erUnder60Aar()\" data-checkbox-validate>\n" +
    "                <input type=\"hidden\" data-ng-model=\"harHuketAvCheckboksDeltid.value\"\n" +
    "                       data-nav-faktum=\"reellarbeidssoker.villigdeltid\"\n" +
    "                       data-ng-required=\"faktum.value == 'false' && erUnder60Aar()\"\n" +
    "                       data-error-messages=\"'reellarbeidssoker.villigdeltid.false.minstEnCheckboksErAvhuketForDeltid.feilmelding'\">\n" +
    "\n" +
    "                <div data-navcheckbox\n" +
    "                     data-navconfig\n" +
    "                     data-nav-faktum=\"reellarbeidssoker.villigdeltid.reduserthelse\"\n" +
    "                     data-navlabel=\"reellarbeidssoker.reduserthelse\"\n" +
    "                     data-navendret=\"endreDeltidsAarsaker()\"></div>\n" +
    "\n" +
    "                <div data-navcheckbox\n" +
    "                     data-navconfig\n" +
    "                     data-nav-faktum=\"reellarbeidssoker.villigdeltid.omsorgbarnunder1aar\"\n" +
    "                     data-navlabel=\"reellarbeidssoker.omsorgbarnunder1aar\"\n" +
    "                     data-navendret=\"endreDeltidsAarsaker()\"></div>\n" +
    "\n" +
    "                <div data-navcheckbox\n" +
    "                     data-navconfig\n" +
    "                     data-nav-faktum=\"reellarbeidssoker.villigdeltid.eneansvarbarnunder5skoleaar\"\n" +
    "                     data-navlabel=\"reellarbeidssoker.eneansvarbarnunder5skoleaar\"\n" +
    "                     data-navendret=\"endreDeltidsAarsaker()\"></div>\n" +
    "\n" +
    "                <div data-navcheckbox\n" +
    "                     data-navconfig\n" +
    "                     data-nav-faktum=\"reellarbeidssoker.villigdeltid.eneansvarbarnopptil18aar\"\n" +
    "                     data-navlabel=\"reellarbeidssoker.eneansvarbarnopptil18aar\"\n" +
    "                     data-navendret=\"endreDeltidsAarsaker()\"></div>\n" +
    "\n" +
    "                <div data-navcheckbox\n" +
    "                     data-navconfig\n" +
    "                     data-nav-faktum=\"reellarbeidssoker.villigdeltid.omsorgansvar\"\n" +
    "                     data-navlabel=\"reellarbeidssoker.omsorgansvar\"\n" +
    "                     data-navendret=\"endreDeltidsAarsaker()\"></div>\n" +
    "\n" +
    "                <div data-navcheckbox\n" +
    "                     data-nav-faktum=\"reellarbeidssoker.villigdeltid.annensituasjon\"\n" +
    "                     data-navconfig\n" +
    "                     data-navlabel=\"reellarbeidssoker.annensituasjon\"\n" +
    "                     data-navendret=\"endreDeltidsAarsaker()\"></div>\n" +
    "\n" +
    "                <div data-navinfoboks data-ng-if=\"harValgtAnnetUnntakDeltid()\">\n" +
    "                    <p data-cmstekster=\"reellarbeidssoker.unntak.dokumentasjon\"></p>\n" +
    "                </div>\n" +
    "\n" +
    "                <span class=\"melding\"\n" +
    "                      data-cmstekster=\"reellarbeidssoker.villigdeltid.false.minstEnCheckboksErAvhuketForDeltid.feilmelding\"></span>\n" +
    "            </div>\n" +
    "            <div data-ng-if=\"erUnder60Aar()\">\n" +
    "                <p class=\"informasjonstekst\" data-cmstekster=\"reellarbeidssoker.villigdeltid.false.informasjon2\"></p>\n" +
    "            </div>\n" +
    "\n" +
    "            <div class=\"tekstfelt form-linje arbeidsmengde-container\">\n" +
    "                <label>\n" +
    "                    <span data-cmstekster=\"reellarbeidssoker.maksimalarbeidstid\"></span>\n" +
    "                    <input type=\"text\"\n" +
    "                           data-ng-model=\"faktum.value\"\n" +
    "                           data-nav-faktum=\"reellarbeidssoker.villigdeltid.maksimalarbeidstid\"\n" +
    "                           data-navconfig\n" +
    "                           data-ng-required=\"true\"\n" +
    "                           data-error-messages=\"{ required: 'reellarbeidssoker.maksimalarbeidstid.feilmelding',\n" +
    "                           pattern: 'reellarbeidssoker.maksimalarbeidstid.pattern.feilmelding'}\"\n" +
    "                           data-ng-pattern=\"/^(\\d+(?:[\\.\\,]\\d)?)$/\"\n" +
    "                           data-tekstfelt-patternvalidering\n" +
    "                           data-blur-validate />\n" +
    "                    <span class=\"timer\" data-cmstekster=\"egennaering.gardsbruk.false.timer\"></span>\n" +
    "                    <span class=\"melding\"></span>\n" +
    "                </label>\n" +
    "            </div>\n" +
    "\n" +
    "            <div class=\"ekstra-spm-boks\" data-vedlegginfoboks data-ng-show=\"trengerUtalelseFraFagpersonellDeltid()\">\n" +
    "                <ul>\n" +
    "                    <li>\n" +
    "                        <span data-cmstekster=\"reellarbeidssoker.utalelsefagpersonell.vedlegginformasjon\"></span>\n" +
    "                    </li>\n" +
    "                </ul>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-nav-faktum=\"reellarbeidssoker.villigpendle\"\n" +
    "             data-navconfig\n" +
    "             data-booleanradio\n" +
    "             data-nokkel=\"reellarbeidssoker.villigpendle\">\n" +
    "            <div data-ng-if=\"erUnder60Aar()\" data-navinfoboks>\n" +
    "                <span data-cmstekster=\"reellarbeidssoker.villigpendle.false.informasjon.under60\"></span>\n" +
    "            </div>\n" +
    "            <div data-ng-if=\"erOver59Aar()\" data-navinfoboks>\n" +
    "                <span data-cmstekster=\"reellarbeidssoker.villigpendle.false.informasjon.over60\"></span>\n" +
    "            </div>\n" +
    "\n" +
    "            <div class=\"form-linje checkbox\" data-ng-if=\"erUnder60Aar()\" data-checkbox-validate>\n" +
    "                <input type=\"hidden\" data-ng-model=\"harHuketAvCheckboksPendle.value\"\n" +
    "                       data-nav-faktum=\"reellarbeidssoker.villigpendle\"\n" +
    "                       data-ng-required=\"faktum.value == 'false' && erUnder60Aar()\"\n" +
    "                       data-error-messages=\"'reellarbeidssoker.villigpendle.false.minstEnCheckboksErAvhuketForPendle.feilmelding'\"/>\n" +
    "\n" +
    "                <div data-navcheckbox\n" +
    "                     data-navconfig\n" +
    "                     data-nav-faktum=\"reellarbeidssoker.villigpendle.reduserthelse\"\n" +
    "                     data-navlabel=\"reellarbeidssoker.pendlereduserthelse\"\n" +
    "                     data-navendret=\"endrePendleAarsaker()\"></div>\n" +
    "\n" +
    "                <div data-navcheckbox\n" +
    "                     data-navconfig\n" +
    "                     data-nav-faktum=\"reellarbeidssoker.villigpendle.omsorgbarnunder1aar\"\n" +
    "                     data-navlabel=\"reellarbeidssoker.pendleomsorgbarnunder1aar\"\n" +
    "                     data-navendret=\"endrePendleAarsaker()\"></div>\n" +
    "\n" +
    "                <div data-navcheckbox\n" +
    "                     data-navconfig\n" +
    "                     data-nav-faktum=\"reellarbeidssoker.villigpendle.eneansvarbarnunder5skoleaar\"\n" +
    "                     data-navlabel=\"reellarbeidssoker.pendleeneansvarbarnunder5skoleaar\"\n" +
    "                     data-navendret=\"endrePendleAarsaker()\"></div>\n" +
    "\n" +
    "                <div data-navcheckbox\n" +
    "                     data-navconfig\n" +
    "                     data-nav-faktum=\"reellarbeidssoker.villigpendle.eneansvarbarnopptil18aar\"\n" +
    "                     data-navlabel=\"reellarbeidssoker.pendleeneansvarbarnopptil18aar\"\n" +
    "                     data-navendret=\"endrePendleAarsaker()\"></div>\n" +
    "\n" +
    "                <div data-navcheckbox\n" +
    "                     data-navconfig\n" +
    "                     data-nav-faktum=\"reellarbeidssoker.villigpendle.omsorgansvar\"\n" +
    "                     data-navlabel=\"reellarbeidssoker.pendleomsorgansvar\"\n" +
    "                     data-navendret=\"endrePendleAarsaker()\"></div>\n" +
    "\n" +
    "                <div data-navcheckbox\n" +
    "                     data-navconfig\n" +
    "                     data-nav-faktum=\"reellarbeidssoker.villigpendle.annensituasjon\"\n" +
    "                     data-navlabel=\"reellarbeidssoker.pendleannensituasjon\"\n" +
    "                     data-navendret=\"endrePendleAarsaker()\"></div>\n" +
    "\n" +
    "                <div data-navinfoboks data-ng-if=\"harValgtAnnetUnntakPendle()\">\n" +
    "                    <p data-cmstekster=\"reellarbeidssoker.unntak.dokumentasjon\"></p>\n" +
    "                </div>\n" +
    "\n" +
    "                <span class=\"melding\"\n" +
    "                      data-cmstekster=\"reellarbeidssoker.villigpendle.false.minstEnCheckboksErAvhuketForPendle.feilmelding\"></span>\n" +
    "            </div>\n" +
    "\n" +
    "            <div class=\"ekstra-spm-boks\" data-vedlegginfoboks data-ng-show=\"trengerUtalelseFraFagpersonellPendle()\">\n" +
    "                <ul>\n" +
    "                    <li>\n" +
    "                        <span data-cmstekster=\"reellarbeidssoker.utalelsefagpersonell.vedlegginformasjon\"></span>\n" +
    "                    </li>\n" +
    "                </ul>\n" +
    "            </div>\n" +
    "\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"helse\"\n" +
    "             data-booleanradio\n" +
    "             data-navconfig\n" +
    "             data-nav-faktum=\"reellarbeidssoker.villighelse\"\n" +
    "             data-nokkel=\"reellarbeidssoker.villighelse\"\n" +
    "             data-hjelpetekst>\n" +
    "\n" +
    "            <div data-navinfoboks>\n" +
    "                <span data-cmstekster=\"reellarbeidssoker.villighelse.false.vedlegginformasjon\"></span>\n" +
    "            </div>\n" +
    "             \n" +
    "            <div data-navtextarea\n" +
    "                 data-navconfig\n" +
    "                 data-nav-faktum=\"reellarbeidssoker.villighelse.fritekst\"\n" +
    "                 data-nokkel=\"reellarbeidssoker.helsefritekst\"\n" +
    "                 data-maxlengde=\"500\"\n" +
    "                 data-navfeilmelding=\"reellarbeidssoker.helsefritekst.feilmelding\"\n" +
    "                 data-obligatorisk=\"true\"></div>\n" +
    "\n" +
    "            \n" +
    "            <div class=\"ekstra-spm-boks\" data-vedlegginfoboks data-ng-show=\"kanIkkeTaAlleTyperArbeid()\">\n" +
    "                <ul>\n" +
    "                    <li>\n" +
    "                        <span data-cmstekster=\"reellarbeidssoker.kanikkealletyperarbeid.vedlegginformasjon\"></span>\n" +
    "                    </li>\n" +
    "                </ul>\n" +
    "            </div>\n" +
    "\n" +
    "        </div>\n" +
    "\n" +
    "\n" +
    "        <div data-booleanradio\n" +
    "             data-navconfig\n" +
    "             data-nav-faktum=\"reellarbeidssoker.villigjobb\"\n" +
    "             data-nokkel=\"reellarbeidssoker.villigjobb\">\n" +
    "\n" +
    "            <div data-navinfoboks>\n" +
    "                <span data-cmstekster=\"reellarbeidssoker.villigjobb.false.informasjon\"></span>\n" +
    "            </div>\n" +
    "            <div class=\"sentrert\">\n" +
    "                <a href=\"#/avbryt\"\n" +
    "                   data-cmstekster=\"reellarbeidssoker.villigjobb.false.avbryt\"></a>\n" +
    "            </div>\n" +
    "\n" +
    "        </div>\n" +
    "\n" +
    "\n" +
    "        <div data-spmblokkferdig></div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "\n" +
    "\n" +
    "");
}]);

angular.module("../views/templates/soknadSlettet.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/soknadSlettet.html",
    "<div id=\"slettet\" class=\"soknad avbryt-boks\" data-sidetittel=\"sidetittel.slettet\">\n" +
    "    <div class=\"rad-belyst inforad\" data-ng-controller=\"SlettetCtrl\">\n" +
    "        <div class=\"begrensning sak-totredel\">\n" +
    "            <section class=\"panel-standard oversikt\">\n" +
    "                <h1 class=\"strek-ikon-slettet\" data-cmstekster=\"slettet.tittel\"></h1>\n" +
    "\n" +
    "                <p class=\"info\" data-cmstekster=\"slett.informasjon\"></p>\n" +
    "\n" +
    "                <div class=\"slett-soknad-stor\">\n" +
    "                    <div class=\"lenker-dot\">\n" +
    "                        <ul class=\"liste-vannrett\" role=\"navigation\">\n" +
    "                            <li><a href=\"{{skjemaVeilederUrl}}\" id=\"slettetSkjemaveileder\"\n" +
    "                                   data-cmstekster=\"slettet.skjemaveileder\" role=\"link\"></a></li>\n" +
    "                            <li><a href=\"{{mineHenveldelserBaseUrl}}\" id=\"slettetMinehenvendelser\"\n" +
    "                                   role=\"link\" data-cmstekster=\"slettet.videretilnav\"></a></li>\n" +
    "                        </ul>\n" +
    "                    </div>\n" +
    "                </div>\n" +
    "            </section>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>");
}]);

angular.module("../views/templates/soknadliste.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/soknadliste.html",
    "<a id=\"dagpenger\" href=\"#/utslagskriterier\">Dagpenger</a>\n" +
    "");
}]);

angular.module("../views/templates/utdanning/utdanning.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/utdanning/utdanning.html",
    "<div class=\"skjemaramme\">\n" +
    "    <div data-ng-form=\"utdanningForm\" class=\"skjemainnhold vertikal\" data-ng-controller=\"UtdanningCtrl\" data-novalidate>\n" +
    "        <div data-form-errors></div>\n" +
    "\n" +
    "        <div class=\"spm boolean form-linje\">\n" +
    "            <h4 class=\"spm-sporsmal\" data-cmstekster=\"utdanning.informasjon\"></h4>\n" +
    "\n" +
    "            <div data-navradio\n" +
    "                 data-value=\"ikkeUtdanning\"\n" +
    "                 data-navconfig\n" +
    "                 data-nav-faktum=\"utdanning\"\n" +
    "                 data-navlabel=\"utdanning.svar.ikkeUtdanning\"\n" +
    "                 data-navfeilmelding=\"utdanning.feilmelding\">\n" +
    "            </div>\n" +
    "\n" +
    "            <div data-navradio\n" +
    "                 data-navconfig\n" +
    "                 data-nav-faktum=\"utdanning\"\n" +
    "                 data-value=\"avsluttetUtdanning\"\n" +
    "                 data-navlabel=\"utdanning.svar.avsluttetUtdanning\"\n" +
    "                 data-navfeilmelding=\"utdanning.feilmelding\">\n" +
    "            </div>\n" +
    "\n" +
    "            <div class=\"spm\" data-ng-if=\"hvis('utdanning', 'avsluttetUtdanning')\">\n" +
    "                <div data-navinfoboks>\n" +
    "                    <p data-cmstekster=\"utdanning.svar.under.utdanning.avsluttet.informasjon\"></p>\n" +
    "                </div>\n" +
    "\n" +
    "                <div class=\"ekstra-spm-boks\" data-vedlegginfoboks>\n" +
    "                    <ul>\n" +
    "                        <li><p data-cmstekster=\"utdanning.svar.under.utdanning.avsluttet.dokumentere\"></p></li>\n" +
    "                    </ul>\n" +
    "                </div>\n" +
    "            </div>\n" +
    "\n" +
    "            <div data-navradio\n" +
    "                 data-navconfig\n" +
    "                 data-nav-faktum=\"utdanning\"\n" +
    "                 data-value=\"underUtdanning\"\n" +
    "                 data-navlabel=\"utdanning.svar.underUtdanning\"\n" +
    "                 data-navfeilmelding=\"utdanning.feilmelding\">\n" +
    "            </div>\n" +
    "\n" +
    "\n" +
    "            <span class=\"melding\"> </span>\n" +
    "\n" +
    "            <div class=\"ekstra-info spm form-linje checkbox\" data-ng-if=\"hvis('utdanning', 'underUtdanning')\"\n" +
    "                 data-checkbox-validate>\n" +
    "                <h4 class=\"spm-sporsmal\" data-cmstekster=\"utdanning.sporsmal\"></h4>\n" +
    "\n" +
    "                <input type=\"hidden\"\n" +
    "                       data-ng-model=\"harHuketAvCheckboks.value\"\n" +
    "                       data-ng-required=\"hvis('utdanning', 'underUtdanning')\"\n" +
    "                       data-error-messages=\"'utdanning.minstEnAvhuket.feilmelding'\">\n" +
    "\n" +
    "                <div id=\"underUtdanningKveld\"\n" +
    "                     data-navcheckbox\n" +
    "                     data-navconfig\n" +
    "                     data-nav-faktum=\"utdanning.kveld\"\n" +
    "                     data-navlabel=\"utdanning.svar.under.utdanning.kveld\"\n" +
    "                     data-navendret=\"endreUtdanning(utdanningForm)\">\n" +
    "                    <div data-ng-include=\"'../views/templates/utdanning/utdanningKveldTemplate.html'\"></div>\n" +
    "                </div>\n" +
    "\n" +
    "                <div data-navcheckbox\n" +
    "                     data-navconfig\n" +
    "                     data-nav-faktum=\"utdanning.kortvarig\"\n" +
    "                     data-navlabel=\"utdanning.svar.under.utdanning.kortvarig\"\n" +
    "                     data-navendret=\"endreUtdanning(utdanningForm)\">\n" +
    "                    <div data-ng-include=\"'../views/templates/utdanning/utdanningKortvarigTemplate.html'\"></div>\n" +
    "                </div>\n" +
    "\n" +
    "                <div data-navcheckbox\n" +
    "                     data-navconfig\n" +
    "                     data-nav-faktum=\"utdanning.kortvarigflere\"\n" +
    "                     data-navlabel=\"utdanning.svar.under.utdanning.kortvarigflere\"\n" +
    "                     data-navendret=\"endreUtdanning(utdanningForm)\">\n" +
    "\n" +
    "                    <div data-ng-include=\"'../views/templates/utdanning/utdanningKortvarigFlereTemplate.html'\"></div>\n" +
    "                </div>\n" +
    "\n" +
    "                <div data-navcheckbox\n" +
    "                     data-navconfig\n" +
    "                     data-nav-faktum=\"utdanning.norsk\"\n" +
    "                     data-navlabel=\"utdanning.svar.under.utdanning.norsk\"\n" +
    "                     data-navendret=\"endreUtdanning(utdanningForm)\">\n" +
    "\n" +
    "                    <div data-ng-include=\"'../views/templates/utdanning/utdanningNorskTemplate.html'\"></div>\n" +
    "                </div>\n" +
    "\n" +
    "                <div data-navcheckbox\n" +
    "                     data-navconfig\n" +
    "                     data-nav-faktum=\"utdanning.introduksjon\"\n" +
    "                     data-navlabel=\"utdanning.svar.under.utdanning.introduksjon\"\n" +
    "                     data-navendret=\"endreUtdanning(utdanningForm)\">\n" +
    "                </div>\n" +
    "\n" +
    "\n" +
    "                <div class=\"annetvalg-nei form-linje\" data-nav-faktum=\"underUtdanningAnnet\">\n" +
    "                    <input id=\"underUtdanningNeiCheckbox\" data-ng-model=\"faktum.value\" type=\"checkbox\"\n" +
    "                           data-ng-change=\"endreUtdannelseAnnet(utdanningForm)\" data-boolean-verdi/>\n" +
    "                    <label for=\"underUtdanningNeiCheckbox\"\n" +
    "                           data-cmstekster=\"utdanning.svar.under.utdanning.nei\"></label>\n" +
    "                    <span class=\"utdanning-nei-melding\"\n" +
    "                          data-cmstekster=\"utdanning.harValgtUtdanning.feilmelding\"></span>\n" +
    "                </div>\n" +
    "\n" +
    "                <div class=\"ekstra-info\" data-ng-if=\"hvis('underUtdanningAnnet')\">\n" +
    "                    <div data-navinfoboks>\n" +
    "                        <span data-cmstekster=\"utdanning.unntak.informasjon\"></span>\n" +
    "                    </div>\n" +
    "                </div>\n" +
    "                <span class=\"melding\" data-cmstekster=\"utdanning.minstEnAvhuket.feilmelding\"></span>\n" +
    "\n" +
    "            </div>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-spmblokkferdig></div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../views/templates/utdanning/utdanningKortvarigFlereTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/utdanning/utdanningKortvarigFlereTemplate.html",
    "<div data-navinfoboks>\n" +
    "    <span data-cmstekster=\"utdanning.svar.under.utdanning.kortvarigflere.informasjon\"></span>\n" +
    "</div>\n" +
    "\n" +
    "<div data-navtekst\n" +
    "     data-navconfig\n" +
    "     data-nav-faktum=\"utdanning.kortvarigflere.sted\"\n" +
    "     data-navlabel=\"utdanning.utdanningssted.navn\"\n" +
    "     data-navfeilmelding=\"'utdanning.error.kortvarigflere.utdanningsstednavn'\"\n" +
    "     data-ng-required=\"true\">\n" +
    "</div>\n" +
    "\n" +
    "\n" +
    "<div data-navtekst\n" +
    "     data-navconfig\n" +
    "     data-nav-faktum=\"utdanning.kortvarigflere.navn\"\n" +
    "     data-navlabel=\"utdanning.utdanning.navn\"\n" +
    "     data-navfeilmelding=\"'utdanning.error.kortvarigflere.utdanningsnavn'\"\n" +
    "     data-ng-required=\"true\">\n" +
    "</div>\n" +
    "\n" +
    "<div class=\"varighet\" data-nav-faktum=\"utdanning.kortvarigflere.varighet\"\n" +
    "     data-nav-property=\"['varighetFra', 'varighetTil']\">\n" +
    "    <div data-nav-dato-intervall\n" +
    "         data-fra-dato=\"navproperties.varighetFra\"\n" +
    "         data-til-dato=\"navproperties.varighetTil\"\n" +
    "         data-label=\"utdanning.varighet\"\n" +
    "         data-er-fremtidigdato-tillatt=\"true\"\n" +
    "         data-er-required=\"true\"\n" +
    "         data-lagre=\"lagreFaktum()\">\n" +
    "    </div>\n" +
    "</div>\n" +
    "\n" +
    "<div data-vedlegginfoboks>\n" +
    "    <ul>\n" +
    "        <li><p data-cmstekster=\"utdanning.svar.under.utdanning.dokumentasjon\"></p></li>\n" +
    "    </ul>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../views/templates/utdanning/utdanningKortvarigTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/utdanning/utdanningKortvarigTemplate.html",
    "<div data-navinfoboks>\n" +
    "    <span data-cmstekster=\"utdanning.svar.under.utdanning.kortvarig.informasjon\"></span>\n" +
    "</div>\n" +
    "\n" +
    "<div data-navtekst\n" +
    "     data-navconfig\n" +
    "     data-nav-faktum=\"utdanning.kortvarig.sted\"\n" +
    "     data-navlabel=\"utdanning.utdanningssted.navn\"\n" +
    "     data-navfeilmelding=\"'utdanning.error.kortvarig.utdanningsstednavn'\"\n" +
    "     data-ng-required=\"true\">\n" +
    " </div>\n" +
    "\n" +
    "<div data-navtekst\n" +
    "     data-navconfig\n" +
    "     data-nav-faktum=\"utdanning.kortvarig.navn\"\n" +
    "     data-navlabel=\"utdanning.utdanning.navn\"\n" +
    "     data-navfeilmelding=\"'utdanning.error.kortvarig.utdanningsnavn'\"\n" +
    "     data-ng-required=\"true\">\n" +
    "</div>\n" +
    "\n" +
    "<div class=\"varighet\" data-nav-faktum=\"utdanning.kortvarig.varighet\"\n" +
    "     data-nav-property=\"['varighetFra', 'varighetTil']\">\n" +
    "    <div data-nav-dato-intervall\n" +
    "         data-fra-dato=\"navproperties.varighetFra\"\n" +
    "         data-til-dato=\"navproperties.varighetTil\"\n" +
    "         data-label=\"utdanning.varighet\"\n" +
    "         data-er-fremtidigdato-tillatt=\"true\"\n" +
    "         data-er-required=\"true\"\n" +
    "         data-lagre=\"lagreFaktum()\">\n" +
    "    </div>\n" +
    "</div>\n" +
    "\n" +
    "<div data-vedlegginfoboks>\n" +
    "    <ul>\n" +
    "        <li><p data-cmstekster=\"utdanning.svar.under.utdanning.dokumentasjon\"></p></li>\n" +
    "    </ul>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../views/templates/utdanning/utdanningKveldTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/utdanning/utdanningKveldTemplate.html",
    "\n" +
    "\n" +
    "<div data-navinfoboks>\n" +
    "    <span data-cmstekster=\"utdanning.svar.under.utdanning.kveld.undervisning.folges.info\"></span>\n" +
    "</div>\n" +
    "\n" +
    "<div data-nav-faktum=\"utdanning.kveld.folges\"\n" +
    "     data-booleanradio\n" +
    "     data-nokkel=\"utdanning.svar.under.utdanning.kveld.undervisning.folges\">\n" +
    "</div>\n" +
    "\n" +
    "\n" +
    "\n" +
    "<div data-navinfoboks>\n" +
    "    <span data-cmstekster=\"utdanning.progresjon.informasjon\"></span>\n" +
    "</div>\n" +
    "<div data-nav-faktum=\"utdanning.kveld.progresjonUnder50\"\n" +
    "     data-er-required=\"false\"\n" +
    "     data-booleanradio\n" +
    "     data-nokkel=\"utdanning.progresjon\">\n" +
    "\n" +
    "    <div data-navinfoboks>\n" +
    "        <span data-cmstekster=\"utdanning.paabegyntUnder6mnd.informasjon\"></span>\n" +
    "    </div>\n" +
    "    <div data-nav-faktum=\"utdanning.kveld.PaabegyntUnder6mnd\"\n" +
    "         data-er-required=\"false\"\n" +
    "         data-booleanradio\n" +
    "         data-nokkel=\"utdanning.paabegyntUnder6mnd\">\n" +
    "\n" +
    "        <div data-navinfoboks>\n" +
    "            <span data-cmstekster=\"utdanning.unntak.under6mnd\"></span>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "\n" +
    "<div data-navtekst\n" +
    "     data-navconfig\n" +
    "     data-nav-faktum=\"utdanning.kveld.sted\"\n" +
    "     data-navlabel=\"utdanning.utdanningssted.navn\"\n" +
    "     data-navfeilmelding=\"'utdanning.error.kveld.utdanningsstednavn'\"\n" +
    "     data-ng-required></div>\n" +
    "\n" +
    "<div data-navtekst\n" +
    "     data-navconfig\n" +
    "     data-nav-faktum=\"utdanning.kveld.navn\"\n" +
    "     data-navlabel=\"utdanning.utdanning.navn\"\n" +
    "     data-navfeilmelding=\"'utdanning.error.kveld.utdanningsnavn'\"\n" +
    "     data-ng-required></div>\n" +
    "\n" +
    "<div class=\"varighet\" data-nav-faktum=\"utdanning.kveld.varighet\" data-nav-property=\"['varighetFra', 'varighetTil']\">\n" +
    "    <div data-nav-dato-intervall\n" +
    "         data-fra-dato=\"navproperties.varighetFra\"\n" +
    "         data-til-dato=\"navproperties.varighetTil\"\n" +
    "         data-label=\"utdanning.varighet\"\n" +
    "         data-er-fremtidigdato-tillatt=\"true\"\n" +
    "         data-er-required=\"true\"\n" +
    "         data-lagre=\"lagreFaktum()\">\n" +
    "    </div>\n" +
    "</div>\n" +
    "\n" +
    "<div data-vedlegginfoboks>\n" +
    "    <ul>\n" +
    "        <li><p data-cmstekster=\"utdanning.svar.under.utdanning.dokumentasjon\"></p></li>\n" +
    "    </ul>\n" +
    "</div>");
}]);

angular.module("../views/templates/utdanning/utdanningNorskTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/utdanning/utdanningNorskTemplate.html",
    "<div data-navtekst\n" +
    "     data-navconfig\n" +
    "     data-nav-faktum=\"utdanning.norsk.sted\"\n" +
    "     data-navlabel=\"utdanning.utdanningssted.navn\"\n" +
    "     data-navfeilmelding=\"'utdanning.error.norsk.utdanningsstednavn'\"\n" +
    "     data-ng-required>\n" +
    "</div>\n" +
    "\n" +
    "<div class=\"varighet\" data-nav-faktum=\"utdanning.norsk.varighet\" data-nav-property=\"['varighetFra', 'varighetTil']\">\n" +
    "    <div data-nav-dato-intervall\n" +
    "         data-fra-dato=\"navproperties.varighetFra\"\n" +
    "         data-til-dato=\"navproperties.varighetTil\"\n" +
    "         data-label=\"utdanning.varighet\"\n" +
    "         data-er-fremtidigdato-tillatt=\"true\"\n" +
    "         data-er-required=\"true\"\n" +
    "         data-lagre=\"lagreFaktum()\">\n" +
    "    </div>\n" +
    "</div>\n" +
    "\n" +
    "<div data-vedlegginfoboks>\n" +
    "    <ul>\n" +
    "        <li><p data-cmstekster=\"utdanning.svar.under.utdanning.dokumentasjon\"></p></li>\n" +
    "    </ul>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../views/templates/utdanningsinformasjon-template.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/utdanningsinformasjon-template.html",
    "\n" +
    "");
}]);

angular.module("../views/templates/vedlegg.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/vedlegg.html",
    "<div data-nav-tittel=\"skjema.tittel\"></div>\n" +
    "<div data-stegindikator steg-liste=\"veiledning, skjema, vedlegg, sendInn\" data-aktiv-index=\"2\"></div>\n" +
    "\n" +
    "<div data-ng-form=\"vedleggForm\" id=\"vedlegg\" class=\"soknad\" data-sidetittel=\"sidetittel.opplasting\"\n" +
    "     data-ng-controller=\"VedleggCtrl\" data-trigg-bolker data-tab-autoscroll>\n" +
    "    <div class=\"rad-belyst inforad\">\n" +
    "        <div class=\"begrensning sak-totredel\">\n" +
    "            <section class=\"panel-standard \">\n" +
    "                <h2 class=\"stor-strek-ikon-slett\" data-cmstekster=\"vedlegg.tittel\"></h2>\n" +
    "\n" +
    "                <p class=\"info\" data-cmstekster=\"vedlegg.info1\"></p>\n" +
    "\n" +
    "                <p class=\"info\" data-cmstekster=\"vedlegg.info2\"></p>\n" +
    "\n" +
    "                <p class=\"info\" data-cmstekster=\"vedlegg.info3\"></p>\n" +
    "\n" +
    "                <div data-form-errors></div>\n" +
    "            </section>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <section>\n" +
    "        <div data-accordion data-close-others=\"false\" data-ng-if=\"forventninger != null\">\n" +
    "            <div class=\"vedlegg\">\n" +
    "                <div data-accordion-group\n" +
    "                     data-ng-repeat=\"forventning in forventninger | orderBy: forventning.opprettetDato\"\n" +
    "                     class=\"spm-blokk vertikal\"\n" +
    "                     id=\"vedlegg_{{forventning.vedleggId}}\">\n" +
    "                    <div data-accordion-heading>\n" +
    "                        <div class=\"bolk-fokus\">\n" +
    "                            <div class=\"vedlegg-bolk\"\n" +
    "                                 data-ng-class=\"{behandlet: !vedleggEr(forventning, 'VedleggKreves'),\n" +
    "                                 lastetopp: vedleggEr(forventning, 'LastetOpp'),\n" +
    "                                 ekstraVedlegg: !ekstraVedleggFerdig(forventning)}\">\n" +
    "                                <div class=\"mini behandlet\" data-ng-if=\"vedleggEr(forventning, 'LastetOpp')\"\n" +
    "                                     data-cmstekster=\"vedlegg.behandlet.lastetopp\"></div>\n" +
    "                                <div class=\"mini behandlet\" data-ng-if=\"vedleggEr(forventning, 'SendesSenere')\"\n" +
    "                                     data-cmstekster=\"vedlegg.behandlet.sendessenere\"></div>\n" +
    "                                <div class=\"mini behandlet\" data-ng-if=\"vedleggEr(forventning, 'SendesIkke')\"\n" +
    "                                     data-cmstekster=\"vedlegg.behandlet.sendesikke\"></div>\n" +
    "\n" +
    "                                <div class=\"flipp\"></div>\n" +
    "                                <h2 class=\"stor\">\n" +
    "                                    <span>{{forventning.tittel}}</span>\n" +
    "                                    <span data-ng-if=\"forventning.navn\">: {{forventning.navn}}</span>\n" +
    "                                </h2>\n" +
    "                            </div>\n" +
    "                        </div>\n" +
    "                    </div>\n" +
    "                    <div data-div class=\"skjemaramme begrensning sak-totredel\" data-ng-controller=\"validervedleggCtrl\">\n" +
    "                        <div class=\"panel-standard\">\n" +
    "                            <h4 data-ng-if=\"!erEkstraVedlegg(forventning)\"\n" +
    "                                data-cmstekster=\"vedlegg.faktum.maabekrefte\"></h4>\n" +
    "                            <h4 data-ng-if=\"erEkstraVedlegg(forventning)\"\n" +
    "                                data-cmstekster=\"vedlegg.faktum.maabekrefte.annetvedlegg\"></h4>\n" +
    "\n" +
    "                            <div class=\"form-linje tekstfelt\" data-ng-if=\"erEkstraVedlegg(forventning)\">\n" +
    "                                <input type=\"hidden\" data-ng-required=\"true\" data-ng-model=\"filVedlagt\"\n" +
    "                                       data-error-messages=\"'vedlegg.annet.ikkelastetopp.feilmelding'\"/>\n" +
    "                                <label>\n" +
    "                                    <span data-cmstekster=\"vedlegg.annet.beskrivelse.sporsmal\"></span>\n" +
    "                                    <input type=\"text\" maxlength=\"25\" data-ng-maxlength=\"25\"\n" +
    "                                           data-ng-minlength=\"3\"\n" +
    "                                           data-ng-required=\"true\" data-ng-model=\"forventning.navn\"\n" +
    "                                           data-ng-blur=\"lagreVedlegg(forventning)\"\n" +
    "                                           data-blur-validate\n" +
    "                                           data-error-messages=\"{required:'vedlegg.annet.navn.feilmelding', minlength:'vedlegg.annet.navn.lengde.feilmelding'}\">\n" +
    "                                </label>\n" +
    "                                <span class=\"melding\" data-cmstekster=\"vedlegg.annet.navn.feilmelding\"></span>\n" +
    "                            </div>\n" +
    "                            <h4 data-ng-if=\"!erEkstraVedlegg(forventning)\" class=\"info-liste\"\n" +
    "                                data-cmshtml=\"{{soknadData.skjemaNummer}}.vedlegg.{{forventning.skjemaNummer}}.bekrefte\"></h4>\n" +
    "\n" +
    "                            <div class=\"spm-knapper send-valg checkboxAsRadio form-linje boolean\"\n" +
    "                                 data-ng-if=\"!vedleggEr(forventning, 'LastetOpp')\"\n" +
    "                                 data-ng-class=\"{feil: skalViseFeil.value === true && validert.value === true}\">\n" +
    "\n" +
    "                                <div class=\"valgbokser\" data-ng-if=\"vedleggEr(forventning, 'VedleggKreves')\"\n" +
    "                                     data-ng-show=\"false\">\n" +
    "                                </div>\n" +
    "                                <div class=\"egen-linje\" data-ng-if=\"forventning.urls.URL != ''\">\n" +
    "                                    <a href=\"{{forventning.urls.URL}}\">{{forventning.tittel}}</a>\n" +
    "                                </div>\n" +
    "                                <div class=\"lastOpp\">\n" +
    "                                    <a data-ng-href=\"#/opplasting/{{forventning.vedleggId}}\" class=\"knapp-link\"\n" +
    "                                       data-ng-click=\"endreInnsendingsvalg(forventning, 'VedleggKreves');\">{{'vedlegg.lastopp'\n" +
    "                                        | cmstekst}}</a>\n" +
    "                                    <a href=\"javascript:void(0)\" aria-role=\"button\"\n" +
    "                                       data-ng-if=\"erEkstraVedlegg(forventning)\"\n" +
    "                                       data-ng-click=\"slettAnnetVedlegg(forventning);\" name=\"Slett\"\n" +
    "                                       data-fokus-slett-annet data-cmstekster=\"vedlegg.annet.slett\">{{'vedlegg.slett' |\n" +
    "                                        cmstekst}}</a>\n" +
    "\n" +
    "                                </div>\n" +
    "                                <div data-ng-if=\"!erEkstraVedlegg(forventning)\">\n" +
    "                                    <h4 data-cmstekster=\"vedlegg.faktum.andreInnsendingsvalg\"></h4>\n" +
    "\n" +
    "                                    <input id=\"{{$index}}ettersendRadio\"\n" +
    "                                           data-ng-model=\"forventning.innsendingsvalg\"\n" +
    "                                           type=\"radio\" value=\"SendesSenere\"\n" +
    "                                           data-ng-click=\"endreInnsendingsvalg(forventning, 'SendesSenere')\">\n" +
    "                                    <label for=\"{{$index}}ettersendRadio\"\n" +
    "                                           data-cmstekster=\"vedlegg.faktum.ettersend\"></label>\n" +
    "\n" +
    "                                    <input id=\"{{$index}}ikkesendRadio\"\n" +
    "                                           data-ng-model=\"forventning.innsendingsvalg\"\n" +
    "                                           type=\"radio\"\n" +
    "                                           value=\"SendesIkke\"\n" +
    "                                           data-ng-click=\"endreInnsendingsvalg(forventning, 'SendesIkke')\">\n" +
    "                                    <label for=\"{{$index}}ikkesendRadio\"\n" +
    "                                           data-cmstekster=\"vedlegg.faktum.ikkesend\"></label>\n" +
    "                                    <input type=\"hidden\" class=\"hidden-vedlegg\" data-ng-model=\"hiddenFelt.value\"\n" +
    "                                           data-ng-required=\"true\"\n" +
    "                                           data-error-messages=\"'{{forventning.tittel}}'\">\n" +
    "                                </div>\n" +
    "                                <span class=\"melding\" data-cmstekster=\"vedlegg.annet.inlinefeilmelding\"\n" +
    "                                      data-ng-if=\"erEkstraVedlegg(forventning)\"></span>\n" +
    "                                <span class=\"melding\" data-cmstekster=\"vedlegg.inlinefeilmelding\"\n" +
    "                                      data-ng-if=\"!erEkstraVedlegg(forventning)\"></span>\n" +
    "                            </div>\n" +
    "                            <div data-ng-if=\"vedleggEr(forventning, 'LastetOpp')\">\n" +
    "                                <div class=\"forhandsvisning\">\n" +
    "                                    <div data-bildenavigering data-vedlegg=\"forventning\"\n" +
    "                                         data-nav-template=\"bildenavigeringTemplateLiten.html\"></div>\n" +
    "                                    <div class=\"knapper\">\n" +
    "                                        <a href=\"javascript:void(0)\" aria-role=\"button\" data-cmstekster=\"vedlegg.slett\"\n" +
    "                                           data-ng-click=\"slettVedlegg(forventning)\"\n" +
    "                                           data-cmshtml=\"vedlegg.slettvedlegg\"></a>\n" +
    "                                    </div>\n" +
    "                                </div>\n" +
    "                            </div>\n" +
    "                        </div>\n" +
    "                    </div>\n" +
    "                </div>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "    </section>\n" +
    "    <div class=\"rad-belyst leggtilekstra\">\n" +
    "        <div class=\"begrensning sak-totredel\">\n" +
    "            <section class=\"panel-standard oversikt\">\n" +
    "                <a href=\"javascript:void(0)\" aria-role=\"button\" class=\"knapp-stor\"\n" +
    "                   data-cmstekster=\"vedlegg.leggtilekstravedlegg\"\n" +
    "                   data-ng-click=\"nyttAnnetVedlegg()\" data-apne-annet-vedlegg></a>\n" +
    "            </section>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"rad ferdig-skjema\">\n" +
    "        <div class=\"begrensning sak-totredel\">\n" +
    "            <section class=\"panel-standard oversikt\">\n" +
    "                <a href=\"javascript:void(0)\" aria-role=\"button\" id=\"til-oppsummering\" class=\"knapp-hoved\"\n" +
    "                   data-ng-click=\"validerVedlegg(vedleggForm)\"\n" +
    "                   data-cmstekster=\"vedlegg.ferdig\"\n" +
    "                   data-fremdriftsindikator=\"grå\" role=\"button\"></a>\n" +
    "            </section>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div data-sist-lagret data-navtilbakelenke=\"tilbake.til.soknad.lenke\"></div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../views/templates/verneplikt.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/verneplikt.html",
    "<div class=\"skjemaramme\">\n" +
    "    <div data-ng-form=\"vernepliktForm\" class=\"skjemainnhold\" data-ng-controller=\"VernepliktCtrl\" data-novalidate>\n" +
    "        <div data-form-errors></div>\n" +
    "        <div data-nav-faktum=\"ikkeavtjentverneplikt\"\n" +
    "             data-booleanradio\n" +
    "             data-nokkel=\"ikkeavtjentverneplikt\">\n" +
    "\n" +
    "            <div data-navinfoboks>\n" +
    "                <span data-cmstekster=\"ikkeavtjentverneplikt.opplasting\"></span>\n" +
    "            </div>\n" +
    "\n" +
    "            <div data-vedlegginfoboks>\n" +
    "                <ul>\n" +
    "                    <li><p data-cmstekster=\"ikkeavtjentverneplikt.opplasting\"></p></li>\n" +
    "                </ul>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "        <div data-spmblokkferdig></div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../views/templates/visvedlegg.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/visvedlegg.html",
    "<div data-ng-controller=\"VisVedleggCtrl\" id=\"visvedlegg\">\n" +
    "    <div data-bildenavigering data-selvstendig=\"true\" data-vedlegg=\"vedlegg\" data-ng-if=\"vedlegg\"></div>\n" +
    "</div>");
}]);

angular.module("../views/templates/ytelser.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../views/templates/ytelser.html",
    "<div class=\"skjemaramme\">\n" +
    "<div data-ng-form=\"ytelserForm\" class=\"skjemainnhold vertikal\" data-ng-controller=\"YtelserCtrl\" data-novalidate>\n" +
    "<div data-form-errors></div>\n" +
    "\n" +
    "<div class=\"spm form-linje checkbox\" data-checkbox-validate>\n" +
    "    <input type=\"hidden\" data-ng-model=\"harHuketAvCheckboksYtelse.value\" data-ng-required=\"true\"\n" +
    "       data-error-messages=\"'ytelser.minstEnCheckboksErAvhuket.feilmelding'\">\n" +
    "    <h4 class=\"spm-sporsmal\">\n" +
    "        <span data-cmstekster=\"ytelser.sporsmal\"></span>\n" +
    "    </h4>\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig data-nav-faktum=\"offentligTjenestepensjon\"\n" +
    "         data-navlabel=\"ytelser.offentligTjenestepensjon\" data-navendret=\"endreYtelse(ytelserForm)\">\n" +
    "\n" +
    "        <div data-navinfoboks>\n" +
    "            <p data-cmstekster=\"ytelser.offentligTjenestepensjonUtbetaler.informasjon\"></p>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-navtekst\n" +
    "             data-navconfig data-nav-faktum=\"offentligTjenestepensjonUtbetaler\"\n" +
    "             data-navlabel=\"ytelser.offentligTjenestepensjonUtbetaler\"\n" +
    "             data-navfeilmelding=\"'ytelser.offentligTjenestepensjonUtbetaler.feilmelding'\"></div>\n" +
    "\n" +
    "        <div data-vedlegginfoboks>\n" +
    "            <ul>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"ytelser.vedlegg\"></p>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig data-nav-faktum=\"privatTjenestepensjon\"\n" +
    "         data-navlabel=\"ytelser.privatTjenestepensjon\" data-navendret=\"endreYtelse(ytelserForm)\">\n" +
    "\n" +
    "        <div data-navinfoboks>\n" +
    "            <p data-cmstekster=\"ytelser.privatTjenestepensjonUtbetaler.informasjon\"></p>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-navtekst\n" +
    "             data-navconfig data-nav-faktum=\"privatTjenestepensjonUtbetaler\"\n" +
    "             data-navlabel=\"ytelser.privatTjenestepensjonUtbetaler\"\n" +
    "             data-navfeilmelding=\"'ytelser.privatTjenestepensjonUtbetaler.feilmelding'\"></div>\n" +
    "\n" +
    "        <div data-vedlegginfoboks>\n" +
    "            <ul>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"ytelser.vedlegg\"></p>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig data-nav-faktum=\"stonadFisker\"\n" +
    "         data-navlabel=\"ytelser.stonadFisker\" data-navendret=\"endreYtelse(ytelserForm)\">\n" +
    "\n" +
    "        <div data-navinfoboks>\n" +
    "            <p data-cmstekster=\"ytelser.stonadFisker.utbetaler.informasjon\"></p>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-navtekst\n" +
    "             data-navconfig data-nav-faktum=\"ytelser.stonadFisker.utbetaler\"\n" +
    "             data-navlabel=\"ytelser.stonadFisker.utbetaler\"\n" +
    "             data-navfeilmelding=\"'ytelser.stonadFisker.utbetaler.feilmelding'\"></div>\n" +
    "\n" +
    "        <div data-vedlegginfoboks>\n" +
    "            <ul>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"ytelser.vedlegg\"></p>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig\n" +
    "         data-nav-faktum=\"garantilott\"\n" +
    "         data-navlabel=\"ytelser.garantilott\" data-navendret=\"endreYtelse(ytelserForm)\">\n" +
    "\n" +
    "\n" +
    "        <div data-navinfoboks>\n" +
    "            <p data-cmstekster=\"ytelser.garantilottUtbetaler.informasjon\"></p>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-navtekst\n" +
    "             data-navconfig\n" +
    "             data-nav-faktum=\"garantilottUtbetaler\"\n" +
    "             data-navlabel=\"ytelser.garantilottUtbetaler\"\n" +
    "             data-navfeilmelding=\"'ytelser.garantilottUtbetaler.feilmelding'\"></div>\n" +
    "\n" +
    "        <div data-vedlegginfoboks>\n" +
    "            <ul>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"ytelser.vedlegg\"></p>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig data-nav-faktum=\"etterlonn\"\n" +
    "         data-navlabel=\"ytelser.etterlonn\" data-navendret=\"endreYtelse(ytelserForm)\">\n" +
    "\n" +
    "        <div data-navinfoboks>\n" +
    "            <p data-cmstekster=\"ytelser.etterlonnUtbetaler.informasjon\"></p>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-navtekst\n" +
    "             data-navconfig data-nav-faktum=\"etterlonnUtbetaler\"\n" +
    "             data-navlabel=\"ytelser.etterlonnUtbetaler\"\n" +
    "             data-navfeilmelding=\"'ytelser.etterlonnUtbetaler.feilmelding'\"></div>\n" +
    "\n" +
    "        <div data-vedlegginfoboks>\n" +
    "            <ul>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"ytelser.vedlegg\"></p>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig data-nav-faktum=\"vartpenger\"\n" +
    "         data-navlabel=\"ytelser.vartpenger\" data-navendret=\"endreYtelse(ytelserForm)\">\n" +
    "\n" +
    "        <div data-navinfoboks>\n" +
    "            <p data-cmstekster=\"ytelser.vartpengerUtbetaler.informasjon\"></p>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-navtekst\n" +
    "             data-navconfig data-nav-faktum=\"vartpengerUtbetaler\"\n" +
    "             data-navlabel=\"ytelser.vartpengerUtbetaler\"\n" +
    "             data-navfeilmelding=\"'ytelser.vartpengerUtbetaler.feilmelding'\"></div>\n" +
    "\n" +
    "        <div data-vedlegginfoboks>\n" +
    "            <ul>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"ytelser.vedlegg\"></p>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig\n" +
    "         data-nav-faktum=\"dagpengerEOS\"\n" +
    "         data-navlabel=\"ytelser.dagpengerEOS\" data-navendret=\"endreYtelse(ytelserForm)\">\n" +
    "\n" +
    "        <div data-navinfoboks>\n" +
    "            <p data-cmstekster=\"ytelser.dagpengerEOSUtbetaler.informasjon\"></p>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"land\">\n" +
    "            <div nav-select\n" +
    "                 nav-faktum=\"dagpengerEOSUtbetalerLand\"\n" +
    "                 data-navconfig\n" +
    "                 data-label=\"ytelser.dagpengerEOSUtbetaler\"\n" +
    "                 data-options=\"land.result\"\n" +
    "                 data-er-required=\"hvisHarDagpengerEOS()\"\n" +
    "                 data-required-feilmelding=\"ytelser.dagpengerEOSUtbetaler.feilmelding\"\n" +
    "                 data-ugyldig-feilmelding=\"ytelser.dagpengerEOSUtbetaler.ugyldig.feilmelding\">\n" +
    "            </div>\n" +
    "        </div>\n" +
    "        <div data-vedlegginfoboks>\n" +
    "            <ul>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"ytelser.vedlegg\"></p>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig\n" +
    "         data-nav-faktum=\"annenYtelse\"\n" +
    "         data-navlabel=\"ytelser.annenYtelse\"\n" +
    "         data-navendret=\"endreYtelse(ytelserForm)\">\n" +
    "\n" +
    "        <div data-navinfoboks>\n" +
    "            <p data-cmstekster=\"ytelser.annenYtelseUtbetaler.informasjon\"></p>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-navtekst\n" +
    "             data-navconfig\n" +
    "             data-nav-faktum=\"annenYtelseType\"\n" +
    "             data-navlabel=\"ytelser.annenYtelseType\"\n" +
    "             data-navfeilmelding=\"'ytelser.annenYtelseType.feilmelding'\"></div>\n" +
    "        <div data-navtekst\n" +
    "             data-navconfig\n" +
    "             data-nav-faktum=\"annenYtelseUtbetaler\"\n" +
    "             data-navlabel=\"ytelser.annenYtelseUtbetaler\"\n" +
    "             data-navfeilmelding=\"'ytelser.annenYtelseUtbetaler.feilmelding'\"></div>\n" +
    "\n" +
    "        <div data-vedlegginfoboks>\n" +
    "            <ul>\n" +
    "                <li>\n" +
    "                    <p data-cmstekster=\"ytelser.vedlegg\"></p>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div>\n" +
    "        <div class=\"annetvalg-nei form-linje\" data-nav-faktum=\"ingenYtelse\">\n" +
    "            <input id=\"ingenYtelseCheckbox\" data-ng-model=\"faktum.value\" type=\"checkbox\"\n" +
    "                   name=\"ingenYtelse\" data-ng-change=\"endreIngenYtelse()\" data-boolean-verdi>\n" +
    "            <label for=\"ingenYtelseCheckbox\" data-cmstekster=\"ytelser.ingenYtelse\"></label>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <span class=\"melding\" data-cmstekster=\"ytelser.minstEnCheckboksErAvhuket.feilmelding\"></span>\n" +
    "</div>\n" +
    "<div class=\"spm form-linje checkbox\" data-checkbox-validate>\n" +
    "    <input type=\"hidden\" data-ng-model=\"harHuketAvCheckboksNavYtelse.value\" data-ng-required=\"true\"\n" +
    "           data-error-messages=\"'ytelser.minstEnCheckboksErAvhuket.navYtelserfeilmelding'\">\n" +
    "    <h4 class=\"spm-sporsmal\">\n" +
    "        <span data-cmstekster=\"ytelser.nav.informasjon\"></span>\n" +
    "        <span data-cmstekster=\"ytelser.nav.sporsmal\"></span>\n" +
    "    </h4>\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig\n" +
    "         data-nav-faktum=\"sykepenger\"\n" +
    "         data-navlabel=\"ytelser.nav.sykepenger\"\n" +
    "         data-navendret=\"endreNavYtelse(ytelserForm)\"></div>\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig\n" +
    "         data-nav-faktum=\"aap\"\n" +
    "         data-navlabel=\"ytelser.nav.aap\"\n" +
    "         data-navendret=\"endreNavYtelse(ytelserForm)\"></div>\n" +
    "\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig\n" +
    "         data-nav-faktum=\"uforetrygd\"\n" +
    "         data-navlabel=\"ytelser.nav.uforetrygd\"\n" +
    "         data-navendret=\"endreNavYtelse(ytelserForm)\"></div>\n" +
    "\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig\n" +
    "         data-nav-faktum=\"svangerskapspenger\"\n" +
    "         data-navlabel=\"ytelser.nav.svangerskapspenger\"\n" +
    "         data-navendret=\"endreNavYtelse(ytelserForm)\"></div>\n" +
    "\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig\n" +
    "         data-nav-faktum=\"foreldrepenger\"\n" +
    "         data-navlabel=\"ytelser.nav.foreldrepenger\"\n" +
    "         data-navendret=\"endreNavYtelse(ytelserForm)\"></div>\n" +
    "\n" +
    "\n" +
    "    <div data-navcheckbox\n" +
    "         data-navconfig\n" +
    "         data-nav-faktum=\"ventelonn\"\n" +
    "         data-navlabel=\"ytelser.nav.ventelonn\"\n" +
    "         data-navendret=\"endreNavYtelse(ytelserForm)\"></div>\n" +
    "\n" +
    "\n" +
    "    <div class=\"annetvalg-nei form-linje\" data-nav-faktum=\"ingennavytelser\">\n" +
    "        <input id=\"ingennavytelserCheckbox\" data-ng-model=\"faktum.value\" type=\"checkbox\"\n" +
    "               name=\"ingennavytelser\" data-ng-change=\"endreIngenNavYtelse(ytelserForm)\" data-boolean-verdi/>\n" +
    "        <label for=\"ingennavytelserCheckbox\" data-cmstekster=\"ytelser.nav.ingen\"></label>\n" +
    "    </div>\n" +
    "    <span class=\"melding\" data-cmstekster=\"ytelser.minstEnCheckboksErAvhuket.navYtelserfeilmelding\"></span>\n" +
    "</div>\n" +
    "\n" +
    "<div>\n" +
    "    <h4 class=\"spm-sporsmal\">\n" +
    "        <span data-cmstekster=\"ytelser.goder.info\"></span>\n" +
    "    </h4>\n" +
    "\n" +
    "    <div data-nav-faktum=\"ikkeavtale\"\n" +
    "         data-navconfig\n" +
    "         data-booleanradio\n" +
    "         data-nokkel=\"ytelser.ikkeavtale\">\n" +
    "\n" +
    "        <div data-navinfoboks>\n" +
    "            <p data-cmstekster=\"ytelser.ikkeavtale.avtale.informasjon.1\"></p>\n" +
    "\n" +
    "            <p data-cmstekster=\"ytelser.ikkeavtale.avtale.informasjon.2\"></p>\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-navtekst\n" +
    "             data-navconfig\n" +
    "             data-nav-faktum=\"ytelser.ikkeavtale.avtale\"\n" +
    "             data-navlabel=\"ytelser.ikkeavtale.avtale.sporsmal\"\n" +
    "             data-navfeilmelding=\"'ytelser.ikkeavtale.avtale.feilmelding'\">\n" +
    "        </div>\n" +
    "\n" +
    "        <div data-vedlegginfoboks>\n" +
    "            <ul>\n" +
    "                <li><p data-cmstekster=\"ytelser.avtale.dokumentasjon\"></p></li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <!-- <span class=\"melding\"></span> -->\n" +
    "</div>\n" +
    "<div data-spmblokkferdig></div>\n" +
    "\n" +
    "</div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../js/app/directives/bildenavigering/bildenavigeringTemplateLiten.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/app/directives/bildenavigering/bildenavigeringTemplateLiten.html",
    "<div class=\"bildenavigering\" data-ng-cloak>\n" +
    "    <a class=\"pil-venstre-hvit\" data-ng-click=\"naviger(-1)\" data-ng-if=\"vedlegg.antallSider > 1\" href=\"javascript:void(0);\"></a>\n" +
    "    <a class=\"bilde\" alt=\"Forstørr\" data-ng-href=\"#visVedlegg/{{vedlegg.vedleggId}}\">\n" +
    "        <img data-ng-repeat=\"bilde in range(vedlegg.antallSider)\"\n" +
    "             data-ng-src=\"../rest/soknad/{{vedlegg.soknadId}}/vedlegg/{{vedlegg.vedleggId}}/thumbnail?side={{bilde}}&ts={{ hentTimestamp}}\"\n" +
    "             alt=\"Forhåndsvisning\"\n" +
    "             data-ng-if=\"sideErSynlig(bilde)\">\n" +
    "        <span>{{vedlegg.navn}} ({{side + 1}}/{{vedlegg.antallSider}})</span>\n" +
    "    </a>\n" +
    "    <a class=\"pil-hoyre-hvit\" data-ng-click=\"naviger(1)\" data-ng-if=\"vedlegg.antallSider > 1\" href=\"javascript:void(0);\"></a>\n" +
    "\n" +
    "</div>\n" +
    "");
}]);

angular.module("../js/app/directives/bildenavigering/bildenavigeringTemplateStor.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/app/directives/bildenavigering/bildenavigeringTemplateStor.html",
    "<div class=\"bildenavigering\" data-ng-cloak>\n" +
    "    <div class=\"nav\" data-ng-if=\"vedlegg.vedleggId\">\n" +
    "        <a class=\"pil-venstre-sort\" data-ng-click=\"naviger(-1)\" data-ng-if=\"vedlegg.antallSider > 1\" href=\"javascript:void(0);\">\n" +
    "        </a><div class=\"bilde\" data-ng-click=\"naviger(1)\">\n" +
    "        <img data-ng-repeat=\"bilde in range(vedlegg.antallSider)\"\n" +
    "             data-ng-src=\"../rest/soknad/{{vedlegg.soknadId}}/vedlegg/{{vedlegg.vedleggId}}/thumbnail?side={{bilde}}\"\n" +
    "             alt=\"Forhåndsvisning\"\n" +
    "             data-ng-hide=\"!sideErSynlig(bilde)\">\n" +
    "        <a class=\"tilbake\" onclick=\"history.back()\" data-cmstekster=\"vedlegg.forhandsvisning.tilbake\" href=\"javascript:void(0);\"></a>\n" +
    "    </div><a class=\"pil-hoyre-sort\" data-ng-click=\"naviger(1)\" data-ng-if=\"vedlegg.antallSider > 1\" href=\"javascript:void(0);\"></a>\n" +
    "    </div>\n" +
    "    <div class=\"header\">\n" +
    "        <div>{{vedlegg.navn}} ({{side + 1}}/{{vedlegg.antallSider}})</div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../js/app/directives/dagpenger/arbeidsforholdformTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/app/directives/dagpenger/arbeidsforholdformTemplate.html",
    "<form name=\"endreArbeidsforholdForm\" data-ng-submit=\"aksjon(af)\">\n" +
    "	<label for=\"arbeidsgiver_navn\">Navn på arbeidsgiver</label>\n" +
    "    <input id=\"arbeidsgiver_navn\" type=\"text\" data-ng-model=\"af.navn\" required/>\n" +
    "    \n" +
    "    <label for=\"arbeidsgiver_land\">Land</label>\n" +
    "    <select id=\"arbeidsgiver_land\" data-ng-model=\"af.land\" data-ng-options=\"item for item in landService.result\" required>\n" +
    "    	<option value=\"1\">--Velg land--</option>\n" +
    "    </select>\n" +
    "    \n" +
    "    <div class=\"varighet\">\n" +
    "        <label>Fra:</label>\n" +
    "        <input type=\"text\" name=\"varighetFra\" ui-date ui-date-format ng-model=\"af.varighetFra\" data-ng-change=\"validateTilFraDato(af)\"  required >\n" +
    "        <label>Til:</label>\n" +
    "        <span data-ng-show=\"datoError\" style=\"color:red\">Tildato må være etter fradato</span>\n" +
    "        <input type=\"text\" name=\"varighetTil\" ui-date ui-date-format ng-model=\"af.varighetTil\" data-ng-change=\"validateTilFraDato(af)\" required >\n" +
    "    </div>\n" +
    "    \n" +
    "    <label>Årsaken til at du sluttet i jobben</label>\n" +
    "    <select id=\"sluttaarsak_id\" type=\"text\" data-ng-model=\"sluttaarsak\" data-ng-options=\"t.navn for t in templates\" required>\n" +
    "    	<option value=\"1\">--Velg sluttårsak--</option>\n" +
    "	</select>\n" +
    "    <div data-ng-show=\"sluttaarsak.navn\" data-ng-include data-src=\"sluttaarsak.url\"></div>\n" +
    "\n" +
    "    <input data-cmstekster=\"arbeidsforhold.lagre\" type=\"submit\" id=\"arbeidsgiverLagre\"/>\n" +
    "    <a href data-cmstekster=\"arbeidsforhold.avbryt\" data-ng-click=\"avbrytEndringAvArbeidsforhold()\"></a>\n" +
    "</form>\n" +
    "");
}]);

angular.module("../js/app/directives/feilmeldinger/feilmeldingerTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/app/directives/feilmeldinger/feilmeldingerTemplate.html",
    "<ul class=\"form-errors\" data-ng-show=\"skalViseFeilmeldinger()\">\n" +
    "    <h3 class=\"medium\">{{ 'feilmelding.boks.overskrift' | cmstekst }}</h3>\n" +
    "    <li class=\"form-error\" data-ng-repeat=\"f in feilmeldinger | fiksRekkefolge\" data-ng-click=\"scrollTilElementMedFeil(f)\" data-ng-class=\"{klikkbar: erKlikkbarFeil(f)}\">\n" +
    "        {{ f.feil }}\n" +
    "    </li>\n" +
    "</ul>");
}]);

angular.module("../js/app/directives/feilmeldinger/stickyFeilmeldingTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/app/directives/feilmeldinger/stickyFeilmeldingTemplate.html",
    "<div class=\"sticky-feilmelding\" data-ng-show=\"skalVises()\">\n" +
    "    <div>\n" +
    "        <div class=\"antall-feil-innholder\">\n" +
    "            <span class=\"vise-tekst\" data-cmstekster=\"skjema.feilmelding.antall.feilmeldinger\"></span>\n" +
    "            <span class=\"antall-feil\"> {{feil.antallFeilMedKlasseFeil + feil.antallFeilMedKlasseFeilstyling}}</span>\n" +
    "        </div>\n" +
    "        <span class=\"navigeringsknapper\">\n" +
    "            <a class=\"forrige\" data-cmstekster=\"skjema.feilmelding.gaatil.forrige\" data-ng-click=\"forrige()\"\n" +
    "               data-ng-class=\"{deaktiverFeilmelding: skalDeaktivereForrigeKnapp()}\"> Forrige </a>\n" +
    "            <a class=\"neste\" data-cmstekster=\"skjema.feilmelding.gaatil.neste\" data-ng-click=\"neste()\"\n" +
    "               data-ng-class=\"{deaktiverFeilmelding: skalDeaktivereNesteKnapp()}\"> Neste </a>\n" +
    "        </span>\n" +
    "    </div>\n" +
    "</div>");
}]);

angular.module("../js/app/directives/markup/navinfoboksTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/app/directives/markup/navinfoboksTemplate.html",
    "<div class=\"panel-mini-belyst infoboks\">\n" +
    "    <p data-ng-transclude class=\"mini utrop-sirkel-ikon\">\n" +
    "    </p>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../js/app/directives/markup/panelStandardBelystTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/app/directives/markup/panelStandardBelystTemplate.html",
    "<div class=\"rad-belyst\">\n" +
    "    <div class=\"begrensning sak-totredel\">\n" +
    "        <div class=\"panel-standard-belyst\">\n" +
    "            <div class=\"ng-transclude\"></div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../js/app/directives/markup/vedlegginfoboksTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/app/directives/markup/vedlegginfoboksTemplate.html",
    "<div class=\"vedlegginfoboks\">\n" +
    "    <div class=\"mini ikon-vedlegg-strek\">\n" +
    "        <p class=\"leggved\" data-cmstekster=\"vedlegg.leggved\"></p>\n" +
    "        <p data-ng-transclude></p>\n" +
    "        <p class=\"lastopp\" data-cmstekster=\"vedlegg.infoboks.lastopp\"></p>\n" +
    "    </div>\n" +
    "</div>");
}]);

angular.module("../js/app/directives/sporsmalferdig/spmblokkFerdigTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/app/directives/sporsmalferdig/spmblokkFerdigTemplate.html",
    "<div class=\"spm-knapper\">\n" +
    "    <button class=\"knapp-liten\" data-ng-click=\"validerOgGaaTilNeste()\">{{ knappTekst | cmstekst }}</button>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../js/app/directives/stegindikator/stegIndikatorTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/app/directives/stegindikator/stegIndikatorTemplate.html",
    "<div class=\"rad nettotopp\">\n" +
    "	<div class=\"begrensning\">\n" +
    "		<ul class=\"stegindikator\">\n" +
    "		    <li data-ng-repeat=\"steg in data.liste\" ng-class=\"{aktiv: $index == {{ aktivIndex }}}\">\n" +
    "		        <a class=\"steg\" href=\"{{ hentLenke($index) }}\" data-ng-if=\"erKlikkbar($index)\">\n" +
    "                    <span class=\"tekst\">{{  'stegindikator.' + steg | cmstekst }}</span>\n" +
    "                    <span class=\"stegnummer\">{{ $index }}</span>\n" +
    "                </a>\n" +
    "                <span class=\"steg\" data-ng-if=\"erIkkeKlikkbar($index)\">\n" +
    "                    <span class=\"tekst\">{{  'stegindikator.' + steg | cmstekst }}</span>\n" +
    "                    <span class=\"stegnummer\">{{ $index }}</span>\n" +
    "                </span>\n" +
    "		    </li>\n" +
    "		</ul>\n" +
    "	</div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../js/app/directives/stickybunn/stickyBunnTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/app/directives/stickybunn/stickyBunnTemplate.html",
    "<div data-ng-class=\"{ikkevises: tastatur === true}\">\n" +
    "    <div class=\"sticky-bunn\">\n" +
    "        <div>\n" +
    "            <span data-ng-if=\"soknadHarBlittLagret()\">\n" +
    "                <span data-cmstekster=\"sistLagret.lagret\"/>\n" +
    "                <span>\n" +
    "                    {{ hentSistLagretTid() | date:'short' }}\n" +
    "                </span>\n" +
    "            </span>\n" +
    "            <span data-cmstekster=\"sistLagret.aldriLagret\" data-ng-if=\"soknadHarAldriBlittLagret()\"/>\n" +
    "        </div>\n" +
    "        <ul class=\"liste-vannrett\">\n" +
    "            <li data-ng-if=\"navtilbakelenke !== 'ingenlenke'\">\n" +
    "                <a href=\"{{ lenke.value}}\" data-cmstekster=\"{{ navtilbakelenke }}\"></a>\n" +
    "            </li>\n" +
    "            <li>\n" +
    "                <a href=\"#/fortsettsenere\" data-cmstekster=\"fortsettSenere\"></a>\n" +
    "            </li>\n" +
    "            <li>\n" +
    "                <a href=\"#/avbryt\" data-cmstekster=\"avbryt.soknad\"></a>\n" +
    "            </li>\n" +
    "        </ul>\n" +
    "    </div>\n" +
    "    <div id=\"sticky-bunn-anchor\"></div>\n" +
    "</div>");
}]);

angular.module("../js/common/directives/accordion/accordionGroupTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/common/directives/accordion/accordionGroupTemplate.html",
    "<section class=\"accordion-group\" data-ng-class=\"{open: isOpen}\">\n" +
    "    <div class=\"accordion-heading\">\n" +
    "        <a class=\"accordion-toggle\"\n" +
    "           id=\"{{ id }}-heading\"\n" +
    "           aria-owns=\"{{ id }}-body\"\n" +
    "           aria-controls=\"{{ id }}-body\"\n" +
    "           href=\"javascript:void(0)\" tabindex=\"0\"\n" +
    "           data-ng-click=\"isOpen = !isOpen\"\n" +
    "           data-nav-aria-expanded=\"isOpen\"\n" +
    "           data-accordion-transclude=\"heading\">\n" +
    "            <div class=\"flipp\">\n" +
    "                <span>+</span>\n" +
    "            </div>\n" +
    "            <h2 class=\"stor\">\n" +
    "                {{heading}}\n" +
    "            </h2>\n" +
    "        </a>\n" +
    "    </div>\n" +
    "    <div class=\"accordion-body\"\n" +
    "         id=\"{{ id }}-body\"\n" +
    "         role=\"group\"\n" +
    "         aria-labelledby=\"{{ id }}-heading\"\n" +
    "         data-nav-aria-hidden=\"!isOpen\"\n" +
    "         data-nav-aria-expanded=\"isOpen\"\n" +
    "         data-collapse=\"!isOpen\">\n" +
    "        <div class=\"accordion-inner\" data-ng-show=\"isOpen\" data-ng-transclude></div>\n" +
    "    </div>\n" +
    "</section>\n" +
    "");
}]);

angular.module("../js/common/directives/accordion/accordionTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/common/directives/accordion/accordionTemplate.html",
    "<div class=\"accordion\" data-ng-transclude>\n" +
    "</div>");
}]);

angular.module("../js/common/directives/booleanradio/booleanradioTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/common/directives/booleanradio/booleanradioTemplate.html",
    "<div class='spm form-linje boolean'>\n" +
    "    <h4 class=\"spm-sporsmal\" data-cmstekster=\"{{ sporsmal }}\"></h4>\n" +
    "\n" +
    "    <div data-navradio\n" +
    "         data-navconfig\n" +
    "         data-value='true'\n" +
    "         data-navlabel='{{ trueLabel }}'>\n" +
    "    </div>\n" +
    "\n" +
    "    <div data-navradio\n" +
    "         data-navconfig\n" +
    "         data-value='false'\n" +
    "         data-navlabel='{{ falseLabel }}'>\n" +
    "    </div>\n" +
    "    <span class=\"melding\"></span>\n" +
    "\n" +
    "    <div data-ng-if=\"vis()\" data-ng-show=\"skalViseTranscludedInnhold()\">\n" +
    "            <div class=\"ng-transclude nav-boolean ekstra-spm-boks\" >\n" +
    "            </div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../js/common/directives/datepicker/doubleDatepickerTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/common/directives/datepicker/doubleDatepickerTemplate.html",
    "<div class=\"datepicker-intervall\">\n" +
    "    <div class=\"fra-dato\"\n" +
    "         data-nav-dato\n" +
    "         data-ng-model=\"fraDato\"\n" +
    "         data-er-required=\"fradatoRequired\"\n" +
    "         data-er-fremtidigdato-tillatt=\"erFremtidigdatoTillatt\"\n" +
    "         data-label=\"{{ fraLabel }}\"\n" +
    "         data-required-error-message=\"{{ fraFeilmelding }}\"\n" +
    "         data-til-dato=\"tilDato\"\n" +
    "         data-til-dato-feil=\"tilDatoFeil\"\n" +
    "         data-lagre=\"lagre()\">\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"til-dato\"\n" +
    "         data-nav-dato\n" +
    "         data-ng-model=\"tilDato\"\n" +
    "         data-er-required=\"tildatoRequired\"\n" +
    "         data-er-fremtidigdato-tillatt=\"erFremtidigdatoTillatt\"\n" +
    "         data-label=\"{{ tilLabel }}\"\n" +
    "         data-required-error-message=\"{{ tilFeilmelding }}\"\n" +
    "         data-fra-dato=\"fraDato\"\n" +
    "         data-til-dato-feil=\"tilDatoFeil\"\n" +
    "         data-lagre=\"lagre()\">\n" +
    "    </div>\n" +
    "</div>");
}]);

angular.module("../js/common/directives/datepicker/singleDatepickerTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/common/directives/datepicker/singleDatepickerTemplate.html",
    "<div class=\"form-linje datepicker\" data-ng-class=\"{feil: harFeil(), fokus: harFokus, nav: navDatepicker()}\">\n" +
    "    <span class=\"caretPosition\"></span>\n" +
    "    <label>\n" +
    "        <span data-cmstekster=\"{{ label }}\"></span>\n" +
    "        <span class=\"vekk\" data-cmstekster=\"dato.format\"></span>\n" +
    "        <span data-ng-show=\"navDatepicker()\">\n" +
    "            <span class=\"datepicker-input\">\n" +
    "                <input type=\"text\"\n" +
    "                       data-ng-model=\"ngModel\"\n" +
    "                       data-ng-blur=\"blur()\"\n" +
    "                       data-ng-focus=\"focus()\"\n" +
    "                       data-ng-required=\"erRequired\"\n" +
    "                       data-error-messages=\"'{{ requiredErrorMessage }}'\"\n" +
    "                       data-dato-mask>\n" +
    "                <span class=\"mask\"></span>\n" +
    "                <input type=\"hidden\" data-ng-model=\"ngModel\">\n" +
    "                <span class=\"apne-datepicker\" data-ng-click=\"toggleDatepicker()\"></span>\n" +
    "            </span>\n" +
    "        </span>\n" +
    "        <span data-ng-show=\"vanligDatepicker()\">\n" +
    "            <input type=\"date\"\n" +
    "                   data-ng-model=\"ngModel\"\n" +
    "                   data-ng-required=\"erRequired\"\n" +
    "                   data-ng-blur=\"blur()\"\n" +
    "                   data-ng-focus=\"focus()\"\n" +
    "                   data-error-messages=\"'{{ requiredErrorMessage }}'\">\n" +
    "        </span>\n" +
    "    </label>\n" +
    "\n" +
    "    <span class=\"melding\" data-ng-if=\"harRequiredFeil()\" data-cmstekster=\"{{ requiredErrorMessage }}\"></span>\n" +
    "    <span class=\"melding\" data-ng-if=\"harTilDatoFeil()\" data-cmstekster=\"dato.tilDato.feilmelding\"></span>\n" +
    "    <span class=\"melding\" data-ng-if=\"harFormatteringsFeil()\" data-cmstekster=\"dato.format.feilmelding\"></span>\n" +
    "    <span class=\"melding\" data-ng-if=\"erIkkeGyldigDato()\" data-cmstekster=\"dato.ikkeGyldigDato.feilmelding\"></span>\n" +
    "    <span class=\"melding\" data-ng-if=\"erUloveligFremtidigDato()\" data-cmstekster=\"dato.ugyldigFremtidig.feilmelding\"></span>\n" +
    "</div>");
}]);

angular.module("../js/common/directives/hjelpetekst/hjelpetekstTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/common/directives/hjelpetekst/hjelpetekstTemplate.html",
    "<div class=\"hjelpetekst\" data-ng-click=\"stoppKlikk($event)\">\n" +
    "    <a href=\"javascript:void(0);\" class=\"infoikon\" data-ng-click=\"toggleHjelpetekst()\" aria-role=\"button\"></a>\n" +
    "    <div class=\"hjelpetekst-tooltip\" data-ng-if=\"visHjelp\" data-nav-hjelpetekst-tooltip>\n" +
    "        <div class=\"tittel\">\n" +
    "            <h3 class=\"liten-strek\">{{ tittel }}</h3>\n" +
    "            <a class=\"lukk liten\" href=\"javascript:void(0)\" data-ng-click=\"lukk()\" aria-role=\"button\">{{ 'lukk' | cmstekst }}</a>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"tekst\" data-nav-scroll>\n" +
    "            <p class=\"mini\">{{ tekst }}</p>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>");
}]);

angular.module("../js/common/directives/navinput/navbuttonspinnerTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/common/directives/navinput/navbuttonspinnerTemplate.html",
    "<div>\n" +
    "	<input type=\"{{ type }}\" class=\"{{ klasse }}\" data-ng-class=\"{true: 'hide', false: 'show'}[laster]\" data-cmstekster=\"{{ nokkel }}\" data-ng-click=\"click()\"/>\n" +
    "	<img data-ng-class=\"{true: 'show', false: 'hide'}[laster]\" src=\"../img/ajaxloader/hvit/loader_hvit_48.gif\"/>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../js/common/directives/navinput/navcheckboxTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/common/directives/navinput/navcheckboxTemplate.html",
    "<div class=\"nav-checkbox\">\n" +
    "    <input id=\"{{ navlabel }}\" data-ng-model=\"faktum.value\" type=\"checkbox\" data-boolean-verdi\n" +
    "           data-ng-change=\"lagreFaktum(); endret()\"/>\n" +
    "    <label for=\"{{ navlabel }}\" data-cmstekster=\"{{ navlabel }}\"></label>\n" +
    "\n" +
    "    <div data-nav-hjelpetekstelement data-ng-if=\"hvisHarHjelpetekst()\" data-tittel=\"{{ hjelpetekst.tittel }}\"\n" +
    "         data-tekst=\"{{ hjelpetekst.tekst }}\"></div>\n" +
    "\n" +
    "    <div class=\"ng-transclude ekstra-spm-boks\" data-ng-if=\"hvisHuketAv()\" data-ng-show=\"hvisHarTranscludedInnhold()\"></div>\n" +
    "</div>");
}]);

angular.module("../js/common/directives/navinput/navorgnrfeltTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/common/directives/navinput/navorgnrfeltTemplate.html",
    "<div class=\"form-linje tekstfelt orgnummer\" >\n" +
    "        <label>\n" +
    "            <span data-cmstekster=\"{{ navlabel }}\"></span>\n" +
    "            <input data-ng-model=\"faktum.value\" type=\"text\" value=\"{{ value }}\" data-ng-required=\"erSynlig()\"\n" +
    "                   data-error-messages=\"{{ navfeilmelding }}\" data-blur-validate\n" +
    "                   placeholder=\"123456789\" data-ng-pattern=\"/[0-9]*/\" maxlength=\"9\" orgnr-validate>\n" +
    "        </label>\n" +
    "        <span class=\"melding\"></span>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../js/common/directives/navinput/navradioTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/common/directives/navinput/navradioTemplate.html",
    "<div class=\"nav-radio-knapp\">\n" +
    "    <input class=\"sendsoknad-radio\"\n" +
    "           id=\"{{ navlabel }}\"\n" +
    "           name=\"{{ name }}\"\n" +
    "           type=\"radio\"\n" +
    "           value=\"{{ value }}\"\n" +
    "           data-ng-model=\"faktum.value\"\n" +
    "           data-ng-required=\"true\"\n" +
    "           data-error-messages=\"'{{ navfeilmelding }}'\"\n" +
    "           data-ng-change=\"lagreFaktum()\"\n" +
    "           data-click-validate>\n" +
    "\n" +
    "    <label for='{{ navlabel }}' data-cmstekster=\"{{ navlabel }}\"></label>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../js/common/directives/navinput/navtekstTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/common/directives/navinput/navtekstTemplate.html",
    "<div class=\"tekstfelt form-linje\">\n" +
    "    <label>\n" +
    "        <span data-cmstekster=\"{{ navlabel }}\"></span>\n" +
    "        <input data-ng-model=\"faktum.value\" type=\"text\" value=\"{{ value }}\" data-ng-required=\"true\"\n" +
    "               data-error-messages=\"{{ navfeilmelding }}\" data-ng-pattern=\"{{regexvalidering}}\" data-blur-validate data-minlength=\"{{navminlength}}\" data-maxlength=\"{{navmaxlength}}\"\n" +
    "               maxlength=\"{{inputfeltmaxlength}}\"  data-tekstfelt-patternvalidering/>\n" +
    "        <span class=\"melding\"></span>\n" +
    "    </label>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../js/common/directives/navtextarea/navtextareaObligatoriskTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/common/directives/navtextarea/navtextareaObligatoriskTemplate.html",
    "<div class=\"form-linje textarea-container\">\n" +
    "    <label>\n" +
    "        <span data-cmstekster=\"{{sporsmal}}\"></span>\n" +
    "        <textarea data-ng-name=\"faktum.key\"\n" +
    "                  data-ng-model=\"faktum.value\"\n" +
    "                  data-error-messages=\"'{{ feilmelding }}'\"\n" +
    "                  data-ng-required=\"hvisSynlig()\"\n" +
    "                  data-ng-trim=\"false\"\n" +
    "                  aria-controls=\"{{sporsmal}}\"\n" +
    "                  data-validate-textarea\n" +
    "                  data-ng-blur=\"mistetFokus()\"></textarea>\n" +
    "\n" +
    "        <span class=\"egen-linje\" data-ng-show=\"harFokusOgFeil() === true\">\n" +
    "                    <span class=\"tellertekst\" data-ng-class=\"{negativtekstteller: counter < 0 }\" aria-live=\"polite\"\n" +
    "                          id=\"{{sporsmal}}\"> {{counter}} </span>\n" +
    "                    <span data-cmstekster=\"fritekst.negativtellertekst\" data-ng-if=\"counter < 0\"></span>\n" +
    "                    <span data-cmstekster=\"fritekst.tellertekst\" data-ng-if=\"counter >= 0\"></span>\n" +
    "                </span>\n" +
    "    </label>\n" +
    "    <span class=\"melding\"></span>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../js/common/directives/navtextarea/navtextareaTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/common/directives/navtextarea/navtextareaTemplate.html",
    "<div class=\"form-linje textarea-container\" data-ng-class=\"{feil: feil}\">\n" +
    "    <label>\n" +
    "        <span data-cmstekster=\"{{sporsmal}}\"></span>\n" +
    "        <textarea data-ng-model=\"faktum.value\"\n" +
    "                  data-ng-name=\"faktum.key\"\n" +
    "                  data-error-messages=\"'{{ feilmelding }}'\"\n" +
    "                  data-ng-trim=\"false\"\n" +
    "                  aria-controls=\"{{sporsmal}}\"\n" +
    "                  data-validate-textarea\n" +
    "                  data-ng-blur=\"mistetFokus()\"></textarea>\n" +
    "\n" +
    "        <span class=\"egen-linje\" data-ng-show=\"harFokusOgFeil()\">\n" +
    "            <span class=\"tellertekst\" data-ng-class=\"{negativtekstteller: counter < 0 }\"  aria-live=\"polite\"> {{counter}} </span>\n" +
    "            <span data-cmstekster=\"fritekst.negativtellertekst\" data-ng-if=\"counter < 0\"></span>\n" +
    "            <span data-cmstekster=\"fritekst.tellertekst\" data-ng-if=\"counter >= 0\"></span>\n" +
    "        </span>\n" +
    "    </label>\n" +
    "    <span class=\"melding\" data-cmstekster=\"fritekst.feilmelding\"></span>\n" +
    "</div>");
}]);

angular.module("../js/common/directives/select/selectTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/common/directives/select/selectTemplate.html",
    "<div class=\"nav-select form-linje\" data-ng-class=\"{open: selectOpen, feil: harFeil()}\">\n" +
    "    <div data-ng-show=\"navSelect()\">\n" +
    "        <label>\n" +
    "            <span data-cmstekster=\"{{ label }}\"></span>\n" +
    "            <span class=\"select-input clearfix\">\n" +
    "                <span class=\"pil\" data-ng-click=\"apneSelectboks($event)\"></span>\n" +
    "                <input type=\"text\"\n" +
    "                       data-ng-model=\"inputVerdi\"\n" +
    "                       data-ui-keyup=\"{enter: 'enter($event)', esc: 'escape()'}\"\n" +
    "                       data-ui-keydown=\"{up: 'navigateUp($event)', down: 'navigateDown($event)', tab: 'tab($event)'}\"\n" +
    "                       data-ng-required=\"erRequired\"\n" +
    "                       data-error-messages=\"'{{ requiredFeilmelding }}'\"\n" +
    "                       data-ng-click=\"klikk($event)\">\n" +
    "            </span>\n" +
    "            <span data-ng-if=\"harRequiredFeil()\" class=\"melding\" data-cmstekster=\"{{ requiredFeilmelding }}\"/>\n" +
    "            <span data-ng-if=\"inneholderIkkeSkrevetTekst()\" class=\"melding\"\n" +
    "                  data-cmstekster=\"{{ ugyldigFeilmelding }}\"/>\n" +
    "        </label>\n" +
    "        <ul data-ng-show=\"skalViseListen()\">\n" +
    "            <li data-bindonce data-ng-repeat=\"option in vistListeFiltrert\"\n" +
    "                data-ng-click=\"valgtElement($event, option.value)\"\n" +
    "                data-value=\"{{ option.value }}\"\n" +
    "                data-ng-bind-html=\"option.displayText\"\n" +
    "                data-ng-class=\"{harFokus: harFokus(option.value)}\"/>\n" +
    "        </ul>\n" +
    "    </div>\n" +
    "    <div class=\"vanlig-select\" data-ng-if=\"vanligSelect()\">\n" +
    "        <label>\n" +
    "            <span data-cmstekster=\"{{ label }}\"></span>\n" +
    "            <select data-ng-model=\"faktum.value\" data-ng-options=\"opt.value as opt.text for opt in orginalListe\"></select>\n" +
    "        </label>\n" +
    "    </div>\n" +
    "</div>\n" +
    "");
}]);

angular.module("../js/common/directives/tittel/tittelTemplate.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("../js/common/directives/tittel/tittelTemplate.html",
    "<div class=\"rad tittel\">\n" +
    "    <div class=\"begrensning\">\n" +
    "        <h1 class=\"diger hoved-tittel\" data-cmstekster=\"{{tittel}}\"></h1>\n" +
    "    </div>\n" +
    "</div>");
}]);

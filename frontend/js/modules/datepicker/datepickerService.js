angular.module('nav.datepicker.service', [])
    .factory('datepickerInputService', function(datepickerInputKeys, datepickerNonInputKeys) {
        var allowedKeys = datepickerInputKeys.concat(datepickerNonInputKeys);

        return {
            isValidInput: function(keyCode, currentLength, maxLength, caretPosition) {
                function isAllowedPeriod() {
                    return (keyCode === 190 && (caretPosition === 2 || caretPosition === 5) || keyCode !== 190);
                }

                return allowedKeys.contains(keyCode) && (datepickerNonInputKeys.contains(keyCode) || currentLength < maxLength) && isAllowedPeriod();
            },
            addPeriodAtRightIndex: function(input, oldInput, caretPosition) {
                var start = caretPosition - (input.length - oldInput.length);
                var slutt = caretPosition;

                for (var i = start; i < slutt && i < input.length; i++) {
                    if (i === 1 || i === 4) {
                        if (input[i + 1] === '.') {
                            caretPosition++;
                            i++;
                        } else {
                            input = input.splice(i + 1, 0, '.');
                            caretPosition++;
                            i++;
                            slutt++;
                        }
                    }
                }
                return [input, caretPosition];
            }
        };
    })
    .factory('maskService', function(cmsService) {
        return {
            getMaskText: function(text) {
                function skalViseDatoFormatFraOgMedDag() {
                    return antallPunktum === 0 && text.length < 3;
                }

                function skalViseDatoFormatFraOgMedMaaned() {
                    var dagTekst = text.substring(0, text.indexOf('.'));
                    return antallPunktum === 1 && dagTekst.length < 3 && maanedTekst.length < 4;
                }

                function skalBareViseDatoFormatMedAar() {
                    var dagOgMaanedTekst = text.substring(0, text.lastIndexOf('.'));
                    return antallPunktum === 2 && dagOgMaanedTekst.length < 6 && aarTekst.length < 5;
                }

                var maskText = '';
                var initialMaskText = cmsService.getTrustedHtml('dato.format');
                var antallPunktum = text.match(/\./g) === null ? 0 : text.match(/\./g).length;
                var maanedTekst = text.substring(text.indexOf('.'), text.length);
                var aarTekst = text.substring(text.lastIndexOf('.'), text.length);
                if (skalViseDatoFormatFraOgMedDag()) {
                    maskText = initialMaskText.substring(text.length, initialMaskText.length);
                } else if (skalViseDatoFormatFraOgMedMaaned()) {
                    maskText = initialMaskText.substring(2 + maanedTekst.length, initialMaskText.length);
                } else if (skalBareViseDatoFormatMedAar()) {
                    maskText = initialMaskText.substring(5 + aarTekst.length, initialMaskText.length);
                }

                return maskText;
            }
        };
    })
    .factory('deviceService', function() {
        return {
            isTouchDevice: function() {
                return erTouchDevice() && getIEVersion() < 0;
            }
        };
    })
    .factory('cssService', function() {
        return {
            getComputedStyle: function(element, property) {
                var value = window.getComputedStyle(element[0], null).getPropertyValue(property)
                if (value.indexOf('px') !== -1) {
                    value = parseInt(value.substring(0, value.length - 2));
                }

                return value;
            }
        };
    });
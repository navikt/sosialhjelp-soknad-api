if (!Array.prototype.last) {
	Array.prototype.last = function () {
		return this[this.length - 1];
	};
}

if (!Array.prototype.contains) {
	Array.prototype.contains = function (val) {
		return $.inArray(val, this) > -1;
	};
}

// Returnerer index til ett objekt som inneholder value (ikke nødvendigvis første)
if (!Array.prototype.indexByValue) {
	Array.prototype.indexByValue = function (val) {
		return this.indexOf($.grep(this, function (obj) {
			for (var key in obj) {
				if (obj[key] === val) {
					return this;
				}
			}
			return false;
		})[0]);
	};
}

// Returnerer index til ett objekt som inneholder value (ikke nødvendigvis første)
if (!Array.prototype.indexByFieldValue) {
	Array.prototype.indexByFieldValue = function (attrName, val) {
        return this.map(function(element) {
            return element[attrName];
        }).indexOf(val);
	};
}

String.prototype.splice = function (idx, rem, str) {
	return (this.slice(0, idx) + str + this.slice(idx + Math.abs(rem)));
};

String.prototype.toCamelCase = function() {
    return this.toLowerCase().replace(/\_([a-z])/g, function(all, match) {
        return match.toUpperCase();
    });
};

function erSoknadStartet() {
    return location.href.indexOf("sendsoknad/soknad/")>0;
}
function erEttersending() {
    return location.href.indexOf("ettersending")>0;
}

function getBehandlingIdFromUrl() {
	return location.pathname.split('/').last();
}

function getBehandlingsIdFromUrlForEttersending() {
    // Hack for å hente ut behandlingID
    var url = window.location.href;
    var hashIdx = url.indexOf('#/');
    var behandlingsIdStart = url.substring(0, hashIdx).lastIndexOf('/') + 1;
    return url.substring(behandlingsIdStart, hashIdx);
}

function redirectTilSide(side) {
    var baseUrl = window.location.href.substring(0, window.location.href.indexOf('/sendsoknad'));
    redirectTilUrl(baseUrl + side);
}

function redirectTilUrl(url) {
    window.location.href = url;
}

function sjekkOmGittEgenskapTilObjektErTrue(objekt) {
	if (objekt) {
		return checkTrue(objekt.value);
	}
	return false;
}

function sjekkOmGittEgenskapTilObjektErVerdi(objekt, verdi) {
	if (objekt) {
		return checkThat(objekt.value, verdi);
	}
return false;
}

function checkThat(element, verdi) {
	if (element === undefined || element === null) {
		return false;
	}
	return element.toString() === verdi;
}
function checkTrue(element) {
	if (element === undefined || element === null) {
		return false;
	}
	return element.toString() === 'true';
}

function scrollToElement(element, offset) {
	var animationSpeed = 200;
	var scrollPos = Math.max(element.offset().top - offset, 0);
	$('body, html').scrollToPos(scrollPos, animationSpeed);
}

function verdiErIkkeTom(verdi) {
	return verdi !== undefined && verdi !== null && verdi.length > 0;
}

function harAttributt(scope, objekt, attributt) {
	var capitalizedAttr = capitalizeFirstLetter(attributt);

    var attr;

	if (objekt.hasOwnProperty(attributt)) {
		attr = objekt[attributt];
	} else if (objekt.hasOwnProperty('ng' + capitalizedAttr)) {
        attr =  objekt['ng' + capitalizedAttr];
	} else {
		return false;
	}

    if (attributt !== "required") {
        return attr;
    }

    var isRequired = scope.$eval(objekt['ng' + capitalizedAttr]);

    if (isRequired === undefined) {
        isRequired = scope.$eval(objekt['ng' + capitalizedAttr].toString());
    }

    return isRequired;
}

function capitalizeFirstLetter(str) {
	return str.charAt(0).toUpperCase() + str.slice(1);
}

function stringContainsNotCaseSensitive(str, query) {
	return str.toLowerCase().indexOf(query.toLowerCase());
}

(function ($) {
	$.fn.scrollToPos = function (position, speed) {
		if (speed === undefined) {
			speed = 'fast';
		}
		$(this).animate({
			scrollTop: position
		}, speed);
	};
})(jQuery);

function fadeBakgrunnsfarge(element, scope, rgb1, rgb2, rgb3) {
	var backgroundColour = [rgb1, rgb2, rgb3].join(',') + ',';
	var transparency = 0;
	var timeout = setInterval(function () {
		if (transparency >= 0) {
			transparency -= 0.015;
			element[0].style.backgroundColor = 'rgba(' + backgroundColour + transparency + ')';
		} else {
			element.removeAttr('style');
			clearInterval(timeout);
			scope.$apply();
		}
	}, 20);
}

function fadeFeilmelding(element, melding, feilmeldingsklasse, scope) {
	var backgroundColour = [254, 230, 230].join(',') + ',';
	var borderColour = [254, 230, 230].join(',') + ',';
	var meldingColour = [195, 0, 0].join(',') + ',';
	var transparency = 1;
	var timeout = setInterval(function () {
        if (transparency >= 0) {
			transparency -= 0.015;
			element[0].style.backgroundColor = 'rgba(' + backgroundColour + transparency + ')';
			element[0].style.borderColor = 'rgba(' + borderColour + transparency + ')';
			melding[0].style.color = 'rgba(' + meldingColour + transparency + ')';
		} else {
			element.removeClass(feilmeldingsklasse);
            element.removeAttr('style');
			melding.removeAttr('style');
			clearInterval(timeout);
			scope.$apply();
		}
	}, 20);
}
function fadeAktivFeilmelding(element, melding, feilmeldingsklasse, scope) {
    var backgroundColour = [250, 214, 214].join(',') + ',';
    var borderColour = [195, 0, 0].join(',') + ',';
    var meldingColour = [160, 4, 4].join(',') + ',';
    var transparency = 1;
    var timeout = setInterval(function () {
        if (transparency >= 0) {
            transparency -= 0.015;
            element[0].style.backgroundColor = 'rgba(' + backgroundColour + transparency + ')';
            element[0].style.borderColor = 'rgba(' + borderColour + transparency + ')';
            melding[0].style.color = 'rgba(' + meldingColour + transparency + ')';
        } else {
            element.removeClass(feilmeldingsklasse);
            element.removeAttr('style');
            melding.removeAttr('style');
            clearInterval(timeout);
            scope.$apply();
        }
    }, 20);
}
function fadeContrastFeilmelding(element, melding, feilmeldingsklasse, scope) {
    var backgroundColour = [250, 214, 214].join(',') + ',';
    var borderColour = [250, 110, 140].join(',') + ',';
    var transparency = 1;
    var timeout = setInterval(function () {
        if (transparency >= 0) {
            transparency -= 0.015;
            element[0].style.backgroundColor = 'rgba(' + backgroundColour + transparency + ')';
            element[0].style.borderColor = 'rgba(' + borderColour + transparency + ')';
        } else {
            element.removeClass(feilmeldingsklasse);
            element.removeAttr('style');
            melding.removeAttr('style');
            clearInterval(timeout);
            scope.$apply();
        }
    }, 20);
}

function reverserNorskDatoformat(datoString) {
	var re = new RegExp(/^\d\d\.\d\d\.\d\d\d\d$/);
	if (re.test(datoString) && erGyldigDato(datoString)) {
		return datoString.split('.').reverse().join('-');
	} else {
		return '';
	}
}

function erFremtidigDato(year, month, day) {
	var dato = new Date();
	dato.setMonth(month-1);
	dato.setDate(day);
	dato.setFullYear(year);
	dato.setHours(0);
	dato.setMilliseconds(0);
	dato.setSeconds(0);
	dato.setMinutes(0);

	var dagensDato =  new Date();
	var temp = new Date();
	temp.setMonth(dagensDato.getMonth());
	temp.setDate(dagensDato.getDate());
	temp.setFullYear(dagensDato.getFullYear());
	temp.setHours(0);
	temp.setMilliseconds(0);
	temp.setSeconds(0);
	temp.setMinutes(0);

	var morgenDagensDatoMillis = temp.setTime(temp.getTime() + 86400000);

	if(dato.getTime() >= morgenDagensDatoMillis) {
		return true;		
	}
	return false;
}

// stackoverflow.com/questions/5812220/test-if-date-is-valid
function erGyldigDato(datoString) {
	var bits = datoString.split('.');
	var aar = bits[2];
	var maaned = bits[1];
	var dag = bits[0];
	var dagerIMaaned = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

	// Skuddår
	if (((aar % 4) === 0 && aar % 100) || (aar % 400) === 0) {
		dagerIMaaned[1] = 29;
	}
	return dag <= dagerIMaaned[--maaned];
}

function konverterTallTilStringMedToSiffer(tall) {
	var tallMedToSiffer = '0' + tall;
	return tallMedToSiffer.slice(-2);
}

function hentCaretPosisjon(element) {
	var domElement = element[0];
	var posisjon = 0;

	if (document.selection) {
		domElement.focus();
		var oSel = document.selection.createRange();
		oSel.moveStart('character', -domElement.value.length);
		posisjon = oSel.text.length;
	} else if (domElement.selectionStart || domElement.selectionStart === '0') {
		posisjon = domElement.selectionStart;
	}
	return posisjon;
}

function settCaretPosisjon(element, posisjon) {
	var domElement = element[0];

	if (document.selection) {
		domElement.focus();
		var oSel = document.selection.createRange();
		oSel.moveStart('character', posisjon);
	} else if (domElement.selectionStart || domElement.selectionStart === '0') {
		domElement.selectionStart = posisjon;
		domElement.selectionEnd = posisjon;
	}
}

function settFokusTilNesteElement(inputElement) {
    var fokuserbareElementer = $('input, a, select, button, textarea').filter(function () {
		return $(this).css('display') !== 'none';
	});

    fokuserbareElementer.eq(fokuserbareElementer.index(inputElement) + 1).focus();
}

function erMobil() {
    return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
}

function erTouchDevice() {
    return 'ontouchstart' in window || 'onmsgesturechange' in window;
}

function getIEVersion() {
    var version = -1;
    var ua = navigator.userAgent;
    if (navigator.appName === 'Microsoft Internet Explorer') {
        var re = new RegExp("MSIE ([0-9]{1,}[.0-9]{0,})");

        if (re.exec(ua) !== null) {
            version = parseInt(RegExp.$1);
        }
    } else if (navigator.appName === 'Netscape') {
        var reIe11  = new RegExp("Trident/.*rv:([0-9]{1,}[\\.0-9]{0,})");
        if (reIe11.exec(ua) !== null){
            version = parseFloat(RegExp.$1 );
        }
    }


    return version;
}

function isNotNullOrUndefined(obj) {
    return obj !== undefined && obj !== null;
}

function trimWhitespaceIString(str) {
    return str.replace(/\s+/g, '');
}

function setCookie(name, value, expire) {
    var cookieString = name + "=" + JSON.stringify(value) + ";";
    if(expire) {
        var timer = new Date();
        timer.setTime(timer.getTime() + (expire*60*60*1000));
        cookieString += "expires=" + timer.toUTCString() + ";";
    }
    document.cookie = cookieString + "path=/;";
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i=0; i<ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1);
        if (c.indexOf(name) != -1) return JSON.parse(c.substring(name.length,c.length));
    }
    return "";
}

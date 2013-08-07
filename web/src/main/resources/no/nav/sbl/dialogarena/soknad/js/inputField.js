function saveInputOnChange(json) {
    var component = $('#' + json.selector);

    component.change(function() {
        saveInput(json.callbackUrl, getValue());
    });

    function getValue() {
        if (component.is(':checkbox')) {
            return component.is(':checked');
        } else {
            return component.val();
        }
    }
}


function saveInputOnRadiobuttonChange(json) {
    var components = $('#' + json.selector + " input");

    components.change(function() {
        if ($(this).is(':checked')) {
            var value = $(this).next('label').children('span').text().toLowerCase();
            saveInput(json.callbackUrl, value);
        }
    });
}

function saveInput(callbackUrl, value) {
    Wicket.Ajax.post({u: callbackUrl, ep: {'value': value}});
}
function saveInputOnFocusOut(json) {
    var component = $('#' +  json.componentId);

    component.change(function() {
        Wicket.Ajax.post({u: json.callbackUrl, ep: {'value': getValue()}});
    });

    function getValue() {
        if (component.is(':checkbox')) {
            return component.is(':checked');
        } else {
            return component.val();
        }
    }
}

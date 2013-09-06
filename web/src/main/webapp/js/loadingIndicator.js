/**
 * Legges til en loading gif før gitt komponent
 *
 * @param elementId ID til komponenten som skal skjules
 * @param indicatorIdPostfix String som legges til på slutten av ID-en til komponenten som lasteindikatoren hører til
 * @param loadingIndicatorPath path til lasteindikatoren
 */
function addLoadingIndicatorToComponent(elementId, indicatorIdPostfix, loadingIndicatorPath) {
    var component = $('#' + elementId);
    var position = component.position();
    var loadingComponentHtml = '<img class="loadingIndicator" id="' + component.attr('id') + indicatorIdPostfix + '" src="' + loadingIndicatorPath + '" />';

    if (!component.prev().hasClass("loadingIndicator")) {
        component.before(loadingComponentHtml);
        var loadingComponent = $('#' + component.attr('id') + indicatorIdPostfix);
        loadingComponent.position(position);
        loadingComponent.hide();
    }
}

/**
 * Viser lasteikon når det lastes opp en fil til ett input-element som er søsken med gitt element
 *
 * @param buttonId ID til knappen som skal byttes ut med et lasteikon
 * @param loadingIndicatorId ID til lasteikonet
 */
function addShowLoadingIndicatorOnSubmitFile(buttonId, loadingIndicatorId) {
    $('#' + buttonId).siblings("input[type=file]").change(function () {
        $('#' + buttonId).hide();
        $('#' + loadingIndicatorId).show();
    });
}

/**
 * Viser lasteikon når en knapp klikkes
 *
 * @param buttonId ID til knappen
 * @param loadingIndicatorId ID til lasteikonet
 */
function addOnClickShowLoadingIndicator(buttonId, loadingIndicatorId) {
    $('#' + buttonId).click(function () {
        $(this).hide();
        $('#' + loadingIndicatorId).show();
    });
}


/**
 * Skjuler lasteikonet og viser knappen igjen
 *
 * @param buttonId knappen som vises
 */
function hideLoadingIndicator(buttonId) {
    $('#' + buttonId).show();
    $('#' + buttonId).siblings(".loadingIndicator").hide();
}
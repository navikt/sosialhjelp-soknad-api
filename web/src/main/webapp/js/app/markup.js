function lagRad() {
    var rad =  $('<div/>')
        .addClass('rad');

    $('<div/>').addClass('begrensning').appendTo(rad);

    return rad;
}

function lagKnappeRad() {
    var rad = lagRad();
    var begrensning = rad.children().first();

    var avbryt = $('<a/>')
        .attr('id', 'avbryt')
        .attr('href', '#/avbryt')
        .text('Avbryt');

    var fortsettSenere = $('<a/>')
        .attr('id', 'fortsettSenere')
        .attr('href', '#/fortsettsenere')
        .text('Fortsett senere');

    begrensning.append($('<section/>').append(avbryt).append(fortsettSenere));
    return rad;
}
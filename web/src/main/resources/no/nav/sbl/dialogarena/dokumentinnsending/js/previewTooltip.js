
function addPreviewTooltip(json) {
    // Variables
    var numberOfPages = json.antallSider - 1;
    var page = 0;
    var urlTemplate = window.location.origin + '/dokumentinnsending/preview/s/' + json.dokumentId + '/${page}/thumb.png';

    var loadingIcon = $(new Image());
    loadingIcon.attr('src', window.location.origin + '/dokumentinnsending/img/ajaxloader/hvit/loader_hvit_48.gif');

    var image = new Image();
    $(image).addClass('ferdig');

    var pageNumberElement = $('<span/>');

    addTooltip($('#' + json.componentId));


    // ---------------- Functions ------------------

    function addTooltip(component) {
        var options = {
            html: true,
            animation: false,
            delay: 0,
            trigger: 'click',
            placement: 'right',
            title: function() {
                return setupTooltipMarkup(json);
            }
        };

        $(component).addClass("visTooltip");
        $(component).tooltip(options);
        $(component).click(function() {
            $('.visTooltip').each(function() {
                if (this !== component[0]) {
                    $(this).tooltip('hide');
                }
            });
            return false;
        });
    }

    function setupTooltipMarkup(json) {
        var ramme = $('<div/>')
            .addClass('tooltip-ramme');

        var lukkButton = $('<a/>')
                .addClass('symbol-lukk lukk-tooltip')
                .attr('role', 'button');
        ramme.append(lukkButton);
        onClickCloseTooltip(lukkButton);
        ramme.append(addPreviewHeader(json.dokumentNavn));
        ramme.append(addPreviewBody(json));
        return ramme;
    }

    function addPreviewHeader(dokumentNavn) {
        var header = $('<div/>').addClass('header ikon-container-relative');
        var name = $('<div/>')
            .addClass('filnavn mini')
            .text(dokumentNavn);
        header.append(name);
        truncatePreviewTitle(name);
        return header;
    }

    function addPreviewBody(json) {
        var preview = $('<div/>')
            .addClass('forhandsvisning');

        preview.append(addPreviewImage());
        preview.append(addDocumentNavigation());
        preview.append(addFileInformation(json));
        return preview;
    }

    function addPreviewImage() {
        var previewImage = $('<div/>')
            .addClass('forhandsvisning-bilde');
        previewImage.append(image)
        setPage();

        return previewImage;
    }

    function addDocumentNavigation() {
        var navigation = $('<div/>')
            .addClass('sideVelger');

        var previous = $('<a/>')
            .addClass('symbol-pil-venstre')
            .attr('role', 'button')
            .click(function() {
                page = page - 1 < 0 ? numberOfPages : page - 1;
                setPage();
            });

        var next = $('<a/>')
            .addClass('symbol-pil-hoyre')
            .attr('role', 'button')
            .click(function() {
                page = page + 1 > numberOfPages ? 0 : page + 1;
                setPage();
            });

        navigation.append(previous);
        navigation.append(next);

        return navigation;
    }

    function addFileInformation() {
        var fileInfo = $('<div/>')
            .addClass('filinfo mini');

        var displayedPage = $('<div/>');

        displayedPage.append($('<span/>')
                .text(json.side + ' '));

        displayedPage.append(pageNumberElement);

        displayedPage.append($('<span/>')
                .text(json.av  + ' '));

        displayedPage.append($('<span/>')
                .text(numberOfPages + 1));

        fileInfo.append(displayedPage);
        return fileInfo;
    }

    function setPage() {
        pageNumberElement.text(page + 1  + ' ');

        if (loadingIcon.is(':hidden')) {
            $(image).replaceWith(loadingIcon);
        }
        image.onload = function () {
            loadingIcon.replaceWith($(image));
        }
        image.src = urlTemplate.replace('${page}', page);
    }

    function truncatePreviewTitle(element) {
        var truncateTitleInterval = setInterval(function() {
            if (element.is(':visible')) {
                truncateString(element, 3);
                clearInterval(truncateTitleInterval);
            }
        }, 100);

        setTimeout(function() {
            clearInterval(truncateTitleInterval);
        }, 3000);
    }
}
package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;

public class BaseLoadingBehavior extends Behavior {

    public enum Colour {
        HVIT("hvit"),
        GRAA("graa"),
        ROD("roed", "rod"),
        SVART("svart");

        private String colourPackage;
        private String colourFilename;

        Colour(String colourPackage) {
            this(colourPackage, colourPackage);
        }

        Colour(String colourPackage, String colourFilename) {
            this.colourPackage = colourPackage;
            this.colourFilename = colourFilename;
        }

        public String getGifPath() {
            return String.format("/dokumentinnsending/img/ajaxloader/%s/loader_%s_48.gif", colourPackage, colourFilename);
        }
    }

    protected static final String INDICATOR_ID_POSTFIX = "indicator";
    private String loadingIndicatorPath;
    private String javascript;
    protected Component button;

    public BaseLoadingBehavior() {
        this(Colour.HVIT, null);
    }

    public BaseLoadingBehavior(Colour colour, Component button) {
        loadingIndicatorPath = colour.getGifPath();
        this.button = button;
    }

    @Override
    public void onConfigure(Component component) {
        javascript = "";
        super.onConfigure(component);
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        addJavascript(String.format("addLoadingIndicatorToComponent('%s', '%s', '%s');", getButtonMarkupId(component), INDICATOR_ID_POSTFIX, loadingIndicatorPath));
        response.render(OnLoadHeaderItem.forScript(javascript));
        super.renderHead(component, response);
    }

    protected String getButtonMarkupId(Component component) {
        String markupId;
        if (button != null) {
            markupId = button.getMarkupId();
        } else {
            markupId = component.getMarkupId();
        }
        return markupId;
    }

    protected void addJavascript(String javascript) {
        if (!javascript.endsWith(";")) {
            javascript.concat(";");
        }
        this.javascript = this.javascript.concat(javascript);
    }
}

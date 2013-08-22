package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;

public class ShowTextTooltipBehavior extends JsonBehavior {
    String tooltip;
    String selector;

    public ShowTextTooltipBehavior(String tooltip, String selector) {
        this.tooltip = tooltip;
        this.selector = selector;
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        String tooltipParent = String.format("%s", selector);

        JSONObject jsonObject = new JSONObject();
        setJsonObject(jsonObject, "componentSelector", tooltipParent);
        setJsonObject(jsonObject, "content", getContent());
        response.render(OnLoadHeaderItem.forScript(String.format("addTextTooltip(%s);", jsonObject.toString())));
        super.renderHead(component, response);
    }

    private String getContent() {
        // Wrapper innholdet i en div med kryss for å lukke tooltip
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div class=\"tooltip-ramme-strek\">");
        stringBuilder.append("" + tooltip);
        stringBuilder.append("<a class=\"symbol-lukk lukk-tooltip\" href=\"#\"/>");
        stringBuilder.append("</div>");
        return stringBuilder.toString();
    }
}

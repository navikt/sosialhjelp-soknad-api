package no.nav.sbl.dialogarena.websoknad.servlet;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.model.StringResourceModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/enonic")
public class EnonicController {

    private Map<String, String> tekster = new LinkedHashMap<>();

    @RequestMapping(value = "/{side}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Map<String, String> hentTekster(@PathVariable String side) {

        List<String> nokler = NokkelHenter.hentNokler(side);

        for (String nokkel : nokler) {
            if (StringUtils.isNotBlank(nokkel)) {
                tekster.put(nokkel, new StringResourceModel(nokkel, null).getString());
            }
        }

        return tekster;
    }
}

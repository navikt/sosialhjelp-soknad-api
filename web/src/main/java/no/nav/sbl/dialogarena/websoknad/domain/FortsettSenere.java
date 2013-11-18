package no.nav.sbl.dialogarena.websoknad.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FortsettSenere {
	String param;
	String action;
	String epost;
	
	public FortsettSenere(String param, String action, String epost) {
		this.param = param;
		this.action = action;
		this.epost = epost;
	}
	
	public String getEpost() {
		return epost;
	}
	
	public String getAction() {
		return action;
	}
	
	public String getParam() {
		return param;
	}
	
	public void setAction(String action) {
		this.action = action;
	}
	
	public void setEpost(String epost) {
		this.epost = epost;
	}
	
	public void setParam(String param) {
		this.param = param;
	}
}

package no.nav.sbl.dialogarena.service.context;

public final class InntektEllerUtgiftType {

    private final String type;
    private final String tittel;
    
    
    public InntektEllerUtgiftType(String type, String tittel) {
        this.type = type;
        this.tittel = tittel;
    }
    
    public String getType() {
        return type;
    }
    
    public String getTittel() {
        return tittel;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tittel == null) ? 0 : tittel.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        InntektEllerUtgiftType other = (InntektEllerUtgiftType) obj;
        if (tittel == null) {
            if (other.tittel != null)
                return false;
        } else if (!tittel.equals(other.tittel))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}

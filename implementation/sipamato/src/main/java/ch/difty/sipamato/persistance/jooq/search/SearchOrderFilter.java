package ch.difty.sipamato.persistance.jooq.search;

import ch.difty.sipamato.entity.filter.SipamatoFilter;

public class SearchOrderFilter extends SipamatoFilter {

    private static final long serialVersionUID = 1L;

    private String nameMask;
    private Integer owner;
    private Boolean global;
    private Integer ownerIncludingGlobal;

    public String getNameMask() {
        return nameMask;
    }

    public void setNameMask(String nameMask) {
        this.nameMask = nameMask;
    }

    public Integer getOwner() {
        return owner;
    }

    public void setOwner(Integer owner) {
        this.owner = owner;
    }

    public Boolean getGlobal() {
        return global;
    }

    public void setGlobal(Boolean global) {
        this.global = global;
    }

    public Integer getOwnerIncludingGlobal() {
        return ownerIncludingGlobal;
    }

    public void setOwnerIncludingGlobal(Integer owner) {
        this.ownerIncludingGlobal = owner;
    }

}
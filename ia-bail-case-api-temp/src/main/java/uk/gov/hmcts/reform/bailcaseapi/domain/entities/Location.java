package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

public class Location {
    private String id;
    private String name;

    public Location(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setLocationName(String name) {
        this.name = name;
    }
}

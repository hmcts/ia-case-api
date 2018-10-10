package uk.gov.hmcts.reform.iacaseapi.shared.domain.entities;

public class GroundOfAppeal {

    private String ground;

    private GroundOfAppeal() {
        // noop -- for deserializer
    }

    public GroundOfAppeal(String ground) {
        if (ground == null) {
            throw new IllegalArgumentException("ground");
        }

        this.ground = ground;
    }

    public String getGround() {
        if (ground == null) {
            throw new IllegalStateException("ground");
        }

        return ground;
    }

    public void setGround(String ground) {
        if (ground == null) {
            throw new IllegalArgumentException("ground");
        }

        this.ground = ground;
    }
}

package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;

public class GroundForAppeal {

    private Optional<String> ground = Optional.empty();
    private Optional<String> explanation = Optional.empty();

    public Optional<String> getGround() {
        return ground;
    }

    public Optional<String> getExplanation() {
        return explanation;
    }

    public void setGround(String ground) {
        this.ground = Optional.ofNullable(ground);
    }

    public void setExplanation(String explanation) {
        this.explanation = Optional.ofNullable(explanation);
    }
}

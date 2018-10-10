package uk.gov.hmcts.reform.iacaseapi.shared.domain.entities;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.IdValue;

public class HearingRequirements {

    private Optional<String> appellantAttending = Optional.empty();
    private Optional<String> appellantGivingOralEvidence = Optional.empty();
    private Optional<String> anyWitnesses = Optional.empty();
    private Optional<List<IdValue<String>>> witnesses = Optional.empty();
    private Optional<String> interpreterRequired = Optional.empty();
    private Optional<List<IdValue<InterpreterRequirement>>> interpreters = Optional.empty();
    private Optional<String> adjustmentsApply = Optional.empty();
    private Optional<List<String>> adjustments = Optional.empty();
    private Optional<String> adjustmentsOther = Optional.empty();

    private HearingRequirements() {
        // noop -- for deserializer
    }

    public HearingRequirements(
        String appellantAttending,
        String appellantGivingOralEvidence,
        String anyWitnesses,
        List<IdValue<String>> witnesses,
        String interpreterRequired,
        List<IdValue<InterpreterRequirement>> interpreters,
        String adjustmentsApply,
        List<String> adjustments,
        String adjustmentsOther
    ) {
        this.appellantAttending = Optional.ofNullable(appellantAttending);
        this.appellantGivingOralEvidence = Optional.ofNullable(appellantGivingOralEvidence);
        this.anyWitnesses = Optional.ofNullable(anyWitnesses);
        this.witnesses = Optional.ofNullable(witnesses);
        this.interpreterRequired = Optional.ofNullable(interpreterRequired);
        this.interpreters = Optional.ofNullable(interpreters);
        this.adjustmentsApply = Optional.ofNullable(adjustmentsApply);
        this.adjustments = Optional.ofNullable(adjustments);
        this.adjustmentsOther = Optional.ofNullable(adjustmentsOther);
    }

    public Optional<String> getAppellantAttending() {
        return appellantAttending;
    }

    public Optional<String> getAppellantGivingOralEvidence() {
        return appellantGivingOralEvidence;
    }

    public Optional<String> getAnyWitnesses() {
        return anyWitnesses;
    }

    public Optional<List<IdValue<String>>> getWitnesses() {
        return witnesses;
    }

    public Optional<String> getInterpreterRequired() {
        return interpreterRequired;
    }

    public Optional<List<IdValue<InterpreterRequirement>>> getInterpreters() {
        return interpreters;
    }

    public Optional<String> getAdjustmentsApply() {
        return adjustmentsApply;
    }

    public Optional<List<String>> getAdjustments() {
        return adjustments;
    }

    public Optional<String> getAdjustmentsOther() {
        return adjustmentsOther;
    }

    public void setAppellantAttending(String appellantAttending) {
        this.appellantAttending = Optional.ofNullable(appellantAttending);
    }

    public void setAppellantGivingOralEvidence(String appellantGivingOralEvidence) {
        this.appellantGivingOralEvidence = Optional.ofNullable(appellantGivingOralEvidence);
    }

    public void setAnyWitnesses(String anyWitnesses) {
        this.anyWitnesses = Optional.ofNullable(anyWitnesses);
    }

    public void setWitnesses(List<IdValue<String>> witnesses) {
        this.witnesses = Optional.ofNullable(witnesses);
    }

    public void setInterpreterRequired(String interpreterRequired) {
        this.interpreterRequired = Optional.ofNullable(interpreterRequired);
    }

    public void setInterpreters(List<IdValue<InterpreterRequirement>> interpreters) {
        this.interpreters = Optional.ofNullable(interpreters);
    }

    public void setAdjustmentsApply(String adjustmentsApply) {
        this.adjustmentsApply = Optional.ofNullable(adjustmentsApply);
    }

    public void setAdjustments(List<String> adjustments) {
        this.adjustments = Optional.ofNullable(adjustments);
    }

    public void setAdjustmentsOther(String adjustmentsOther) {
        this.adjustmentsOther = Optional.ofNullable(adjustmentsOther);
    }
}

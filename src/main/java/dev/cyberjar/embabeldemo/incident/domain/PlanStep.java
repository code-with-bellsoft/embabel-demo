package dev.cyberjar.embabeldemo.incident.domain;


public record PlanStep(
        int order,
        String title,
        String rationale,
        StepType type,
        boolean reversible,
        String expectedOutcome
) {
}

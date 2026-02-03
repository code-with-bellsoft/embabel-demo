package dev.cyberjar.embabeldemo.incident.domain;

import java.time.Instant;
import java.util.List;

public record IncidentCase(
        String id,
        Instant createdAt,
        IncidentSignal signal,
        IncidentAssessment triage,
        List<AffectedImplant> affected,
        RootCauseHypothesis hypothesis,
        ContainmentPlan plan
) { }

package dev.cyberjar.embabeldemo.incident.domain;

import java.util.List;

public record RootCauseHypothesis(
        HypothesisType type,
        double confidence,
        List<String> evidence
) { }
package dev.cyberjar.embabeldemo.incident.domain;

public record IncidentAssessment(
        IncidentSignal signal,
        RiskLevel riskLevel
) { }

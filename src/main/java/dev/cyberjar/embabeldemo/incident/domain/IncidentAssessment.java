package dev.cyberjar.embabeldemo.incident.domain;

public record IncidentAssessment(
        IncidentSignal signal,
        int numberOfLogs,
        RiskLevel riskLevel
) { }

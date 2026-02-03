package dev.cyberjar.embabeldemo.incident.domain;

public record AffectedImplant(
        String serialNumber,
        String lotNumber,
        String model,
        String civilianId,
        double anomalyScore
) {}

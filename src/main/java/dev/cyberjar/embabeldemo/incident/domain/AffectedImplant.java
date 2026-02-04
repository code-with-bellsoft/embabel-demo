package dev.cyberjar.embabeldemo.incident.domain;

public record AffectedImplant(
        String serialNumber,
        String lotNumber,
        String model,
        String civilianNationalId,
        double anomalyScore
) {}

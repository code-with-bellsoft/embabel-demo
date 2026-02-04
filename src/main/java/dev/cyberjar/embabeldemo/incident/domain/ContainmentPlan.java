package dev.cyberjar.embabeldemo.incident.domain;

import java.util.List;

public record ContainmentPlan(
        List<String> steps,
        boolean requiresApproval,
        EstimatedBlastRadius estimatedBlastRadius
) { }

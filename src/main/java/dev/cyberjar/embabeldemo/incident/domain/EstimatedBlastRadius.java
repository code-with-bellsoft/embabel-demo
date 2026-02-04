package dev.cyberjar.embabeldemo.incident.domain;

import java.util.List;

public record EstimatedBlastRadius(
        int affectedImplantsEstimate,
        List<String> affectedLots,
        List<String> affectedModels,
        String geoSummary,
        String timeSummary
) {
}

package dev.cyberjar.embabeldemo.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import dev.cyberjar.embabeldemo.implantlog.domain.ImplantMonitoringLog;
import dev.cyberjar.embabeldemo.implantlog.service.ImplantMonitoringLogService;
import dev.cyberjar.embabeldemo.incident.domain.IncidentAssessment;
import dev.cyberjar.embabeldemo.incident.domain.IncidentSignal;
import dev.cyberjar.embabeldemo.incident.domain.RiskLevel;
import org.springframework.data.geo.Point;

import java.util.List;
import java.util.Map;

@Agent(description = "Investigates and assesses implant telemetry anomalies in a geo/time window using MongoDB logs")
public class IncidentTriageAgent {

    private final ImplantMonitoringLogService logService;

    public IncidentTriageAgent(ImplantMonitoringLogService logService) {
        this.logService = logService;
    }

    @AchievesGoal(description = "Parses an incident signal and assigns a risk level")
    @Action
    public IncidentAssessment triageIncidentSignal(UserInput input, OperationContext context) {

        IncidentSignal signal = context.ai().withDefaultLlm().createObject(
                """
                Extract an IncidentSignal from the user's message.

                Output rules:
                - lat is a number in [-90, 90]
                - lon is a number in [-180, 180]
                - radiusMeters is a number in meters
                - from/to are ISO-8601 LocalDateTime (e.g. 2026-02-02T02:00:00)
                - metric is one of: neuralLatencyMs, cpuUsagePct, powerUsageUw
                - threshold is a finite number

                User message:
                %s
                """.formatted(input.getContent()), IncidentSignal.class);

        Map<String, List<ImplantMonitoringLog>> logs = logService.findLogsByAreaAndTime(
                toSpringPoint(signal.latitude(), signal.longitude()),
                signal.radiusMeters(),
                signal.from(),
                signal.to());

        RiskLevel risk = classifyRisk(logs, signal);

        return new IncidentAssessment(signal, risk);
    }

    private static RiskLevel classifyRisk(
            Map<String, List<ImplantMonitoringLog>> logs,
            IncidentSignal signal) {

        if (logs.isEmpty()) return RiskLevel.LOW;

        long distinctImplants = logs.size();

        long exceedCount = logs.values()
                .stream()
                .flatMap(List::stream)
                .mapToDouble(log -> getMetricValue(log, signal.metric()))
                .filter(value -> value >= signal.threshold())
                .count();


        if (exceedCount >= 60 && distinctImplants >= 5) return RiskLevel.CRITICAL;
        if (exceedCount >= 30 && distinctImplants >= 3) return RiskLevel.HIGH;
        if (exceedCount >= 10) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private static double getMetricValue(ImplantMonitoringLog log, String metric) {
        return switch (metric) {
            case "neuralLatencyMs" -> log.getNeuralLatencyMs();
            case "cpuUsagePct" -> log.getCpuUsagePct();
            case "powerUsageUw" -> log.getPowerUsageUw();
            default -> throw new IllegalArgumentException("Unsupported metric: " + metric);
        };
    }

    private static Point toSpringPoint(double lat, double lon) {
        return new Point(lon, lat);
    }

}

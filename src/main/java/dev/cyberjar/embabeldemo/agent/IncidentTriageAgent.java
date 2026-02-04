package dev.cyberjar.embabeldemo.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import dev.cyberjar.embabeldemo.civilian.domain.Civilian;
import dev.cyberjar.embabeldemo.civilian.domain.Implant;
import dev.cyberjar.embabeldemo.civilian.service.CivilianService;
import dev.cyberjar.embabeldemo.implantlog.domain.ImplantMonitoringLog;
import dev.cyberjar.embabeldemo.implantlog.service.ImplantMonitoringLogService;
import dev.cyberjar.embabeldemo.incident.domain.*;
import org.springframework.data.geo.Point;

import java.time.Instant;
import java.util.*;

@Agent(description = "Investigates and assesses implant telemetry anomalies in a geo/time window using MongoDB logs")
public class IncidentTriageAgent {

    private final ImplantMonitoringLogService logService;
    private final CivilianService civilianService;

    public IncidentTriageAgent(ImplantMonitoringLogService logService, CivilianService civilianService) {
        this.logService = logService;
        this.civilianService = civilianService;
    }


    @Action(description = "Parse user's message into an IncidentSignal")
    public IncidentSignal parseIncidentSignal(UserInput input, OperationContext context) {
        return context.ai().withDefaultLlm().createObject(
                """
                        Extract an IncidentSignal from the user's message.
                        
                        Output rules:
                        - lon is a number in [-180, 180]
                        - lat is a number in [-90, 90]
                        - radiusMeters is a number in meters
                        - from/to are ISO-8601 LocalDateTime (e.g. 2026-02-02T02:00:00)
                        - metric is one of: neuralLatencyMs, cpuUsagePct, powerUsageUw
                        - threshold is a finite number
                        
                        User message:
                        %s
                        """.formatted(input.getContent()), IncidentSignal.class);

    }

    @Action(description = "Classify risk level for a signal using logs")
    public IncidentAssessment triageIncident(IncidentSignal signal) {
        Map<String, List<ImplantMonitoringLog>> logs = extractLogs(signal);

        RiskLevel risk = classifyRisk(logs, signal);

        return new IncidentAssessment(signal, logs.size(), risk);
    }

    @Action(description = "Find implants affected by the anomaly and assign anomaly scores")
    public List<AffectedImplant> findAffectedImplants(IncidentSignal signal) {

        Map<String, List<ImplantMonitoringLog>> logs = extractLogs(signal);

        return logs.entrySet().stream()
                .map(entry -> toAffectedImplant(entry.getKey(), entry.getValue(), signal))
                .sorted(Comparator.comparingDouble(AffectedImplant::anomalyScore).reversed())
                .toList();


    }

    @Action(description = "Infer a root cause hypothesis from the evidence")
    public RootCauseHypothesis makeRootCauseHypothesis(IncidentSignal signal,
                                                       IncidentAssessment assessment,
                                                       List<AffectedImplant> affectedImplants,
                                                       OperationContext context) {

        return context.ai().withDefaultLlm().createObject(
                """
                        Based on the incident details, choose a root cause hypothesis.
                        
                        Rules:
                        - type must be one of: FIRMWARE_REGRESSION, BAD_LOT, ATTACK_PATTERN, ENVIRONMENTAL
                        - confidence is 0..1
                        - evidence is a short bullet list of specific signals from the inputs
                        
                        IncidentSignal: %s
                        Triage: %s
                        Top affected implants: %s
                        """.formatted(signal, assessment, affectedImplants.stream().limit(10).toList()),
                RootCauseHypothesis.class
        );
    }

    @Action(description = "Create a containment plan based on the hypothesis and blast radius")
    public ContainmentPlan planContainment(
            IncidentAssessment assessment,
            RootCauseHypothesis hypothesis,
            List<AffectedImplant> affectedImplants,
            OperationContext context) {

        boolean requiresApproval =
                assessment.riskLevel() == RiskLevel.HIGH
                        || assessment.riskLevel() == RiskLevel.CRITICAL
                        || hypothesis.type() == HypothesisType.ATTACK_PATTERN;

        EstimatedBlastRadius radius = estimateRadius(assessment.signal(), affectedImplants);

        return context.ai().withDefaultLlm().createObject(
                """
                Produce a ContainmentPlan JSON object.
    
                Rules:
                - steps must be a list of objects like: { "text": "..." }
                - 4-8 steps max, short imperative text
    
                Inputs:
                - riskLevel: %s
                - hypothesis: %s
                - blastRadius: %s
                """.formatted(assessment.riskLevel(), hypothesis, radius),
                ContainmentPlan.class
        );
    }

    @AchievesGoal(description = "Investigate an incident signal and produce a complete incident case",
            examples = {
                    "Investigate telemetry anomalies near (lon,lat) in a time window and propose containment",
                    "Assess implant incident risk and recommend actions"
            })
    @Action(description = "Assemble a complete IncidentCase")
    public IncidentCase buildIncidentCase(
            IncidentSignal signal,
            IncidentAssessment assessment,
            List<AffectedImplant> affected,
            RootCauseHypothesis hypothesis,
            ContainmentPlan plan) {

        return new IncidentCase(
                UUID.randomUUID().toString(),
                Instant.now(),
                signal,
                assessment,
                affected,
                hypothesis,
                plan
        );
    }


    // helper methods


    private EstimatedBlastRadius estimateRadius(IncidentSignal signal,
                                                List<AffectedImplant> affectedImplants) {

        if (affectedImplants == null) affectedImplants = List.of();

        int affectedEstimate = affectedImplants.size();

        List<String> affectedLots = affectedImplants.stream()
                .map(AffectedImplant::lotNumber)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .limit(5)
                .toList();

        List<String> affectedModels = affectedImplants.stream()
                .map(AffectedImplant::model)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .limit(5)
                .toList();

        String geoSummary = "Within %.0fm of (%.5f, %.5f)"
                .formatted(signal.radiusMeters(), signal.latitude(), signal.longitude());

        String timeSummary = "From %s to %s".formatted(signal.from(), signal.to());

        return new EstimatedBlastRadius(
                affectedEstimate,
                affectedLots,
                affectedModels,
                geoSummary,
                timeSummary
        );
    }


    private AffectedImplant toAffectedImplant(
            String serialNumber,
            List<ImplantMonitoringLog> logsPerImplant,
            IncidentSignal signal) {


        if (logsPerImplant == null || logsPerImplant.isEmpty()) {
            // No logs means no evidence; score 0, rest unknown.
            return new AffectedImplant(
                    serialNumber,
                    null,
                    null,
                    null,
                    0.0);
        }

        double anomalyScore = calculateAnomalyScore(logsPerImplant, signal);

        Optional<Civilian> civilian = civilianService.findCivilianByImplantSerialNumber(serialNumber);
        if (civilian.isEmpty()) {
            throw new RuntimeException("No civilian found for implant serial number " + serialNumber);
        }

        Civilian c = civilian.get();
        String civilianNationalId = c.getNationalId();

        Implant implant = c.getImplants()
                .stream()
                .filter(i -> serialNumber.equals(i.getSerialNumber()))
                .findFirst()
                .orElseThrow();


        String lotNumber = String.valueOf(implant.getLotNumber());
        String model = implant.getModel();

        return new AffectedImplant(
                serialNumber,
                lotNumber,
                model,
                civilianNationalId,
                anomalyScore);

    }

    private double calculateAnomalyScore(List<ImplantMonitoringLog> logsPerImplant, IncidentSignal signal) {

        double threshold = signal.threshold();
        if (threshold <= 0.0) return 0.0;

        double max = logsPerImplant.stream()
                .mapToDouble(l -> getMetricValue(l, signal.metric()))
                .max()
                .orElse(0.0);

        if (max <= threshold) return 0.0;

        double score = (max - threshold) / threshold; // exceed ratio
        return Math.min(1.0, score);
    }

    private Map<String, List<ImplantMonitoringLog>> extractLogs(IncidentSignal signal) {
        return logService.findLogsByAreaAndTime(
                toSpringPoint(signal.longitude(), signal.latitude()),
                signal.radiusMeters(),
                signal.from(),
                signal.to());
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
            default -> 0.0;
        };
    }

    private static Point toSpringPoint(double lon, double lat) {
        return new Point(lon, lat);
    }

}

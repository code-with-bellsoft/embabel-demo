package dev.cyberjar.embabeldemo.implantlog.service;

import dev.cyberjar.embabeldemo.implantlog.domain.ImplantMonitoringLog;
import dev.cyberjar.embabeldemo.implantlog.dto.MonitoringStats;
import dev.cyberjar.embabeldemo.implantlog.repository.ImplantMonitoringLogRepository;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ImplantMonitoringLogService {

    private final ImplantMonitoringLogRepository implantMonitoringLogRepository;

    public ImplantMonitoringLogService(ImplantMonitoringLogRepository implantMonitoringLogRepository) {
        this.implantMonitoringLogRepository = implantMonitoringLogRepository;
    }

    public List<ImplantMonitoringLog> findAllByImplantSerialNumber(String serialNumber) {

        return implantMonitoringLogRepository.findByImplantSerialNumber(serialNumber);
    }

    public List<ImplantMonitoringLog> findByImplantSerialNumberAndAfter(
            String serialNumber,
            LocalDateTime timestamp) {

        return implantMonitoringLogRepository.findByImplantSerialNumberAndTimestampAfter(
                serialNumber,
                timestamp);
    }

    public MonitoringStats aggregateStatsForImplantForPeriod(
            String serialNumber,
            LocalDateTime from,
            LocalDateTime to) {

        return implantMonitoringLogRepository.aggregateStats(
                serialNumber,
                from,
                to);
    }

    public Map<String, List<ImplantMonitoringLog>> findLogsByAreaAndTime(
            Point center,
            double maxDistanceMeters,
            LocalDateTime from,
            LocalDateTime to) {

        return implantMonitoringLogRepository.findLogsByAreaAndTimeGrouped(
                center,
                maxDistanceMeters,
                from,
                to);
    }

}

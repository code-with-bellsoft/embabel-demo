package dev.cyberjar.embabeldemo.implantlog.repository;


import dev.cyberjar.embabeldemo.implantlog.domain.ImplantMonitoringLog;
import dev.cyberjar.embabeldemo.implantlog.dto.MonitoringStats;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ImplantMonitoringLogRepositoryCustom {

    MonitoringStats aggregateStats(String serialNumber, LocalDateTime from, LocalDateTime to);

    public Map<String, List<ImplantMonitoringLog>> findLogsByAreaAndTimeGrouped(
            Point center, double maxDistanceMeters, LocalDateTime from, LocalDateTime to);

}

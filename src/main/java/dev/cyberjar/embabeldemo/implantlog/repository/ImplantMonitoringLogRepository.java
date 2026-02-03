package dev.cyberjar.embabeldemo.implantlog.repository;

import dev.cyberjar.embabeldemo.implantlog.domain.ImplantMonitoringLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface ImplantMonitoringLogRepository extends MongoRepository<ImplantMonitoringLog, String>, ImplantMonitoringLogRepositoryCustom {

    List<ImplantMonitoringLog> findByImplantSerialNumber(String implantSerialNumber);

    List<ImplantMonitoringLog> findByImplantSerialNumberAndTimestampAfter(String implantSerialNumber,
                                                                          LocalDateTime timestamp);

    List<ImplantMonitoringLog> findByImplantSerialNumberAndTimestampBetween(String implantSerialNumber,
                                                                            LocalDateTime timestampFrom,
                                                                            LocalDateTime timestampTo);
}

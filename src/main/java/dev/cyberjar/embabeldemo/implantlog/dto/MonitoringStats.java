package dev.cyberjar.embabeldemo.implantlog.dto;

public record MonitoringStats(String implantSerialNumber,
                              double avgPowerUsageUw,
                              double avgCpuUsagePct,
                              double avgNeuralLatencyMs) {
}

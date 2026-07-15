package com.covoitdark.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Report {

    private int id;
    private int reporterId;
    private int reportedId;
    private String reason;
    private LocalDateTime createdAt;

    // Joined fields
    private String reporterName;
    private String reportedName;

    public Report() {}

    public Report(int id, int reporterId, int reportedId, String reason) {
        this.id = id;
        this.reporterId = reporterId;
        this.reportedId = reportedId;
        this.reason = reason;
    }

    public int getId() { return id; }
    public int getReporterId() { return reporterId; }
    public int getReportedId() { return reportedId; }
    public String getReason() { return reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getReporterName() { return reporterName; }
    public String getReportedName() { return reportedName; }

    public void setId(int id) { this.id = id; }
    public void setReporterId(int reporterId) { this.reporterId = reporterId; }
    public void setReportedId(int reportedId) { this.reportedId = reportedId; }
    public void setReason(String reason) { this.reason = reason; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    public void setReportedName(String reportedName) { this.reportedName = reportedName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Report r)) return false;
        return id == r.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Report{id=" + id + ", reporter=" + reporterId + ", reported=" + reportedId + "}";
    }
}

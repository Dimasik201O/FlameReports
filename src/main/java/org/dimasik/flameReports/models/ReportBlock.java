package org.dimasik.flameReports.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dimasik.flameReports.enums.EnumReportStatus;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportBlock {
    private int id;
    private int playerId;
    private String reportIds;
    private String moderator;
    private Timestamp createdAt;
    private EnumReportStatus status;
}
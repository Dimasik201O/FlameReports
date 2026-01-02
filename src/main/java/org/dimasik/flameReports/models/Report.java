package org.dimasik.flameReports.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Report {
    private int id;
    private String suspectName;
    private String reporterName;
    private String reason;
    private Timestamp createdAt;
}
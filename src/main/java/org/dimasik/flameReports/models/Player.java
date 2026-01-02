package org.dimasik.flameReports.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private int id;
    private String nickname;
    private int correctReports;
    private int incorrectReports;
    private String server;
    private Timestamp createdAt;
}
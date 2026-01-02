package org.dimasik.flameReports.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActiveMute {
    private long secondsLeft;
    private String reason;
}

package org.dimasik.flameReports.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum EnumSortingType {
    NEWEST_FIRST("Сначала новые"),
    OLDEST_FIRST("Сначала старые"),
    DECENCY_FIRST("Сначала больше порядочность"),
    AMOUNT_FIRST("Сначала больше количество");

    private String name;
}

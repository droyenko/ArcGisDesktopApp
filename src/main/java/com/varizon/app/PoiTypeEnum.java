package com.varizon.app;

import java.util.stream.Stream;

public enum PoiTypeEnum {

    PHARMACY(1),
    GROCERY(2),
    ATM(3),
    BANK(4),
    OFFICE(5),
    SCHOOL(6),
    HOSPITAL(7);

    private final int id;

    PoiTypeEnum(int id) {
        this.id = id;
    }

    public static PoiTypeEnum findById(final int id) {
        return Stream.of(values())
            .filter(poiTypeEnum -> poiTypeEnum.id == id)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("There is no PoiTypeEnum by specified id=" + id));
    }
}

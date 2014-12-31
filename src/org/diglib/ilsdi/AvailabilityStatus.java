package org.diglib.ilsdi;

import java.util.HashMap;

/**
 * List of ILS-DI services.
 * 
 * @version 1.1 (2008-12-08)
 * @see http://diglib.org/architectures/ilsdi/DLF_ILS_Discovery_1.1.pdf
 */
public enum AvailabilityStatus {
    UNKNOWN("unknown"),
    AVAILABLE("available"),
    NOT_AVAILABLE("not available"),
    POSSIBLY_AVAILABLE("possibly available");

    private String name = "";

    private static HashMap<String, AvailabilityStatus> statusMap = new HashMap<>(AvailabilityStatus.values().length);

    static {
        for (AvailabilityStatus status : AvailabilityStatus.values()) {
            statusMap.put(status.name, status);
        }
    }    
    
    AvailabilityStatus(String name) {
        this.name = name;
    }

    /**
     * Allow to check quickly if any string is a ILS-DI availability status.
     * 
     * @param value
     * @return
     */
    public static AvailabilityStatus getFromValue(String value) {
        return statusMap.get(value);
    }

    @Override
    public String toString() {
        return name;
    }
}

package net.berthereau.exlibris;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.diglib.ilsdi.AvailabilityStatus;

import com.exlibris.primo.api.plugins.rta.HoldingStatus;

/**
 * Handles the conversion from open standard ILS-DI AvailabilityStatus to
 * private Primo HoldingStatus.
 */
public class AvailabilityILSDI2Primo {

    // Allows to get quickly the holding status from availability status.
    private static final Map<AvailabilityStatus, HoldingStatus> emap = initMap();
    // Allows to get quickly the holding status from availability status value.
    private static final Map<String, HoldingStatus> esmap = initEsMap();

    private static Map<AvailabilityStatus, HoldingStatus> initMap() {
        Map<AvailabilityStatus, HoldingStatus> m =
                new EnumMap<>(AvailabilityStatus.class);
        m.put(AvailabilityStatus.UNKNOWN, HoldingStatus.CHECK_HOLDINGS);
        m.put(AvailabilityStatus.AVAILABLE, HoldingStatus.AVAILABLE);
        m.put(AvailabilityStatus.NOT_AVAILABLE, HoldingStatus.UNAVAILABLE);
        m.put(AvailabilityStatus.POSSIBLY_AVAILABLE, HoldingStatus.CHECK_HOLDINGS);
        return Collections.unmodifiableMap(m);
    }

    private static Map<String, HoldingStatus> initEsMap() {
        Map<String, HoldingStatus> m = new HashMap<>();
        m.put(AvailabilityStatus.UNKNOWN.toString(), HoldingStatus.CHECK_HOLDINGS);
        m.put(AvailabilityStatus.AVAILABLE.toString(), HoldingStatus.AVAILABLE);
        m.put(AvailabilityStatus.NOT_AVAILABLE.toString(), HoldingStatus.UNAVAILABLE);
        m.put(AvailabilityStatus.POSSIBLY_AVAILABLE.toString(), HoldingStatus.CHECK_HOLDINGS);
        return Collections.unmodifiableMap(m);
    }

    /**
     * Check if there is a mapping for any status.
     * 
     * @param status
     * @return
     */
    public static boolean isStatusMapped(AvailabilityStatus status) {
        return emap.containsKey(status);
    }

    public static boolean isStatusMapped(String status) {
        return esmap.containsKey(status);
    }

    /**
     * Get Primo holding status from ILS-DI availability status.
     * 
     * TODO Depending on closed Primo API, throws IllegalArgumentException.
     * 
     * @param status
     * @return
     */
    public static HoldingStatus convert(AvailabilityStatus status) {
        if (!emap.containsKey(status)) {
            return null;
        }
        return emap.get(status);
    }

    public static HoldingStatus convert(String status) {
        if (!esmap.containsKey(status)) {
            return null;
        }
        return esmap.get(status);
    }

    public static Collection<HoldingStatus> values() {
        return emap.values();
    }
}

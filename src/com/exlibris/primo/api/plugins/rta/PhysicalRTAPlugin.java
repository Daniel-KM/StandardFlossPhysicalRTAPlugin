package com.exlibris.primo.api.plugins.rta;

import java.util.List;
import java.util.Map;

import com.exlibris.primo.api.common.IMappingTablesFetcher;
import com.exlibris.primo.api.common.IPrimoLogger;

public interface PhysicalRTAPlugin {
    public void init(
            IPrimoLogger logger,
            IMappingTablesFetcher mtFetcher,
            Map<String, Object> params);

    public void updateAvailability(List<RTARequest> rtaRequests);
}

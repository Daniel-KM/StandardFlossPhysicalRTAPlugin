package com.exlibris.primo.api.plugins.rta;

/**
 * List of availability status supported by Primo API.
 */
public enum HoldingStatus {
    // According to public presentation, closed Primo API is not clear on the
    // content, it seems to be different from the availlibrary PNX field, so may
    // be available / unavailable / check_holdings.
    // TODO Check proprietary Primo API holdings status and simplify if needed.
    AVAILABLE("Available"),
    UNAVAILABLE("Unavailable"),
    CHECKHOLDING("Check holdings");

    private String name = "";

    HoldingStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

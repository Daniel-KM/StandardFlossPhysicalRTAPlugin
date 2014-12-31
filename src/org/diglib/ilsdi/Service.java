package org.diglib.ilsdi;

/**
 * List of ILS-DI services.
 * 
 * @version 1.1 (2008-12-08)
 * @see http://diglib.org/architectures/ilsdi/DLF_ILS_Discovery_1.1.pdf
 */
public enum Service {
    // Level 1: Basic discovery interfaces
    
    HARVEST_BIBLIOGRAPHIC_RECORDS("HarvestBibliographicRecords"),
    HARVEST_EXPANDED_RECORDS("HarvestExpandedRecords"),
    GET_AVAILABILITY("GetAvailability"),
    GO_TO_BIBLIOGRAPHIC_REQUEST_PAGE("GoToBibliographicRequestPage"),

    // Level 2: Elementary OPAC supplement
    
    HARVEST_AUTHORITY_RECORDS("HarvestAuthorityRecords"),
    HARVEST_HOLDINGS_RECORDS("HarvestHoldingsRecords"),
    GET_RECORDS("GetRecords"),
    SEARCH("Search"),
    SCAN("Scan"),
    GET_AUTHORITY_RECORDS("GetAuthorityRecords"),
    OUTPUT_REWRITABLE_PAGE("OutputRewritablePage"),
    OUTPUT_INTERMEDIATE_FORMAT("OutputIntermediateFormat"),

    // Level 3: Elementary OPAC alternative

    LOOKUP_PATRON("LookupPatron"),
    AUTHENTICATE_PATRON("AuthenticatePatron"),
    GET_PATRON_INFO("GetPatronInfo"),
    GET_PATRON_STATUS("GetPatronStatus"),
    GET_SERVICES("GetServices"),
    RENEW_LOAN("RenewLoan"),
    HOLD_TITLE("HoldTitle"),
    HOLD_ITEM("HoldItem"),
    CANCEL_HOLD("CancelHold"),
    RECALL_ITEM("RecallItem"),
    CANCEL_RECALL("CancelRecall"),

    // Level 4: Robust/domain specific discovery platforms

    SEARCH_COURSE_RESERVES("SearchCourseReserves"),
    EXPLAIN("Explain");
    
    private String name = "";

    Service(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

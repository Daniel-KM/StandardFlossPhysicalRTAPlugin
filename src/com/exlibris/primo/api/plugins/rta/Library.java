package com.exlibris.primo.api.plugins.rta;

public class Library implements Cloneable {

    private String institution;

    private String libraryCode;

    private String collection;

    private String callNumber;

    private String rtaBaseURL;

    private HoldingStatus holdingStatus;

    public String getInstitution() {
        return institution;
    }

    public String getLibraryCode() {
        return libraryCode;
    }

    public String getCollection() {
        return collection;
    }

    public String getCallNumber() {
        return callNumber;
    }

    public String getRtaBaseURL() {
        return rtaBaseURL;
    }

    public HoldingStatus getHoldingStatus() {
        return holdingStatus;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public void setLibraryCode(String libraryCode) {
        this.libraryCode = libraryCode;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    public void setRtaBaseURL(String rtaBaseURL) {
        this.rtaBaseURL = rtaBaseURL;
    }

    public void setHoldingStatus(HoldingStatus holdingStatus) {
        this.holdingStatus = holdingStatus;
    }

    @Override
    public String toString() {
        return "{"
                + "Institution: \"" + institution + "\",\n"
                + "libraryCode: \"" + libraryCode + "\", \n"
                + "collection: \"" + collection + "\", \n"
                + "callNumber: \"" + callNumber + "\", \n"
                + "holdingStatus: \"" + holdingStatus + "\""
                + "}";
   }
}

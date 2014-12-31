package com.exlibris.primo.api.plugins.rta;

import java.util.List;

public class RTARequest {

    private String primoRecordId;

    private String recordIdentifier;

    private List<Library> libraries;

    public String getPrimoRecordId() {
        return primoRecordId;
    }

    public String getRecordIdentifier() {
        return recordIdentifier;
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    public void setPrimoRecordId(String primoRecordId) {
        this.primoRecordId = primoRecordId;
    }

    public void setRecordIdentifier(String recordIdentifier) {
        this.recordIdentifier = recordIdentifier;
    }

    public void setLibraries(List<Library> libraries) {
        this.libraries = libraries;
    }

    @Override
    public String toString() {
        String output = "{"
                + "primoRecordId: \"" + primoRecordId + "\",\n"
                + "recordIdentifier: \"" + recordIdentifier + "\", \n"
                + "libraries: [";
        int i = 0;
        for (Library library : libraries) {
            if (i++ != 0)
                output += ", \n";
            output += library;
        }

        return output + "]}";
   }
}

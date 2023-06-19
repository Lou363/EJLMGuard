package com.efrei.ejlmguard;

public class Version implements Comparable<Version> {
    private final String version;

    public Version(String version) {
        if (version == null) {
            throw new IllegalArgumentException("Version cannot be null");
        }
        if (!version.matches("\\d+(\\.\\d+)*")) {
            throw new IllegalArgumentException("Invalid version format");
        }
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int compareTo(Version other) {
        String[] thisParts = this.getVersion().split("\\.");
        String[] otherParts = other.getVersion().split("\\.");
        int length = Math.max(thisParts.length, otherParts.length);
        
        for (int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0;
            int otherPart = i < otherParts.length ? Integer.parseInt(otherParts[i]) : 0;
            
            if (thisPart < otherPart) {
                return -1;
            }
            if (thisPart > otherPart) {
                return 1;
            }
        }
        
        return 0;
    }
}

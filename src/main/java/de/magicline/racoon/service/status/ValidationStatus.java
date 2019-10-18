package de.magicline.racoon.service.status;

public interface ValidationStatus {

    int getCode();

    boolean isRetry();

    String getDescription();

    String getType();

}

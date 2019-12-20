package de.magicline.racoon.domain.status.dto;

public interface ValidationStatus {

    int getCode();

    boolean isRetry();

    String getDescription();

    String getType();

}

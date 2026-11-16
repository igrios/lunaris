package com.lunaris.ansenuza.infrastructure.web.dto;

public record UpdatePassengerRequest(String firstName, String lastName, String cuil,
                                     String phone, String address, String locality, String notes) {
}

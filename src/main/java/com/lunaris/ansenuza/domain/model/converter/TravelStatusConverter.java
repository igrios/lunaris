package com.lunaris.ansenuza.domain.model.converter;

import com.lunaris.ansenuza.domain.model.Reservation;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter
public class TravelStatusConverter
        implements AttributeConverter<Reservation.TravelStatus, String> {

    @Override
    public String convertToDatabaseColumn(Reservation.TravelStatus attribute) {
        return (attribute == null ? Reservation.TravelStatus.PENDING : attribute).name();
    }

    @Override
    public Reservation.TravelStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Reservation.TravelStatus.PENDING;
        }
        try {
            return Reservation.TravelStatus.valueOf(
                    dbData.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return Reservation.TravelStatus.PENDING;
        }
    }
}

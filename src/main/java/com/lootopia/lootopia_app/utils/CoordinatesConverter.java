package com.lootopia.lootopia_app.utils;

import com.lootopia.lootopia_app.domain.model.Coordinates;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;

@Converter(autoApply = true)
public class CoordinatesConverter implements AttributeConverter<Coordinates, String> {

    @Override
    public String convertToDatabaseColumn(Coordinates attribute) {
        if (attribute == null) return null;
        return "(" + attribute.getX() + "," + attribute.getY() + ")";
    }

    @Override
    public Coordinates convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) return null;
        if (dbData.startsWith("(") && dbData.endsWith(")")) {
            String content = dbData.substring(1, dbData.length() - 1);
            String[] parts = content.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Expected 2 values for point, got: " + content);
            }
            try {
                double x = Double.parseDouble(parts[0].trim());
                double y = Double.parseDouble(parts[1].trim());
                return new Coordinates(x, y);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error parsing coordinates: " + content, e);
            }
        }
        throw new IllegalArgumentException("Unexpected format for coordinates: " + dbData);
    }
}


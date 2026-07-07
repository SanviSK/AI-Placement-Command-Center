package com.placement.commandcenter.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.commandcenter.dto.ParsedResumeData;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ParsedResumeDataConverter implements AttributeConverter<ParsedResumeData, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ParsedResumeData attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting ParsedResumeData to JSON string", e);
        }
    }

    @Override
    public ParsedResumeData convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new ParsedResumeData();
        }
        try {
            return objectMapper.readValue(dbData, ParsedResumeData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON string to ParsedResumeData", e);
        }
    }
}

package com.placement.commandcenter.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.placement.commandcenter.dto.ReadinessScoreResponse;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ReadinessScoreBreakdownConverter implements AttributeConverter<ReadinessScoreResponse.Breakdown, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public String convertToDatabaseColumn(ReadinessScoreResponse.Breakdown attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting ReadinessScoreResponse.Breakdown to JSON string", e);
        }
    }

    @Override
    public ReadinessScoreResponse.Breakdown convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new ReadinessScoreResponse.Breakdown();
        }
        try {
            return objectMapper.readValue(dbData, ReadinessScoreResponse.Breakdown.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON string to ReadinessScoreResponse.Breakdown", e);
        }
    }
}

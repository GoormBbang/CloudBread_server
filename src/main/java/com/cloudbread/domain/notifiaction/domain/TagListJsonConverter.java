package com.cloudbread.domain.notifiaction.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;

/** tags <-> JSON 배열
    JPA(Java Persistence API)에서 List<String>타입의 자바 객체를 받아, 데이터베이스에 저장할 수 있는 TEXT 타입의 JSON 문자열로 반환하거나,
    그 반대로 변환하는 역할을 한다
 */
@Converter
public class TagListJsonConverter implements AttributeConverter<List<String>, String> {
    private static final ObjectMapper om = new ObjectMapper();
    @Override public String convertToDatabaseColumn(List<String> attribute) {
        try { return om.writeValueAsString(attribute == null ? Collections.emptyList() : attribute); }
        catch (Exception e) { return "[]"; }
    }
    @Override public List<String> convertToEntityAttribute(String dbData) {
        try { return dbData == null ? Collections.emptyList() : om.readValue(dbData, new TypeReference<>(){}); }
        catch (Exception e) { return Collections.emptyList(); }
    }
}

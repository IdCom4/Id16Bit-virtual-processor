package com.idcom4.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idcom4.exceptions.JsonDeserializeException;
import com.idcom4.exceptions.JsonSerializeException;

public class Json {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> String Serialize(T obj) throws JsonSerializeException {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new JsonSerializeException(e);
        }
    }

    public static <T> T Deserialize(String json, Class<T> clazz) throws JsonDeserializeException {
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new JsonDeserializeException(e);
        }
    }

}

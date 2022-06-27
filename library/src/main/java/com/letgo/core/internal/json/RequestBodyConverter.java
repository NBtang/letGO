package com.letgo.core.internal.json;

import java.io.IOException;
import java.lang.reflect.Type;

import javax.annotation.Nullable;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

public class RequestBodyConverter<T> implements Converter<T, RequestBody> {
    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");


    private final JsonConverter jsonConverter;
    private final Type type;

    RequestBodyConverter(JsonConverter jsonConverter, Type type) {
        this.jsonConverter = jsonConverter;
        this.type = type;
    }

    @Nullable
    @Override
    public RequestBody convert(T value) throws IOException {
        return RequestBody.create(MEDIA_TYPE, jsonConverter.toJson(value, type));
    }

}

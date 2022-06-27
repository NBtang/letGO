package com.letgo.core.internal.json;

import java.io.IOException;
import java.lang.reflect.Type;

import javax.annotation.Nullable;

import okhttp3.ResponseBody;
import retrofit2.Converter;

final class ResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private final JsonConverter jsonConverter;
    private final Type type;

    ResponseBodyConverter(JsonConverter jsonConverter, Type type) {
        this.jsonConverter = jsonConverter;
        this.type = type;
    }

    @Nullable
    @Override
    public T convert(ResponseBody value) throws IOException {
        return (T) jsonConverter.fromJson(value.string(), type);
    }
}

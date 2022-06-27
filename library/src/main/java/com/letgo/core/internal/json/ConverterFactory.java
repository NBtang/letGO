package com.letgo.core.internal.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public final class ConverterFactory extends Converter.Factory {

    public static ConverterFactory create(JsonConverter jsonConverter) {
        if (jsonConverter == null) throw new NullPointerException("jsonConverter == null");
        return new ConverterFactory(jsonConverter);
    }

    private final JsonConverter jsonConverter;

    private ConverterFactory(JsonConverter jsonConverter) {
        this.jsonConverter = jsonConverter;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(
            Type type, Annotation[] annotations, Retrofit retrofit) {
        return new ResponseBodyConverter<>(jsonConverter, type);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(
            Type type,
            Annotation[] parameterAnnotations,
            Annotation[] methodAnnotations,
            Retrofit retrofit) {
        return new RequestBodyConverter<>(jsonConverter, type);
    }
}

package com.letgo.core.internal.http.response;


import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BaseResponseBeanDeserializer implements JsonDeserializer<BaseResponseBean> {

    private Gson gson;
    private final String KEY_CODE;
    private final String KEY_MSG;
    private final String KEY_DATA;
    private final int RESULT_OK_CODE;

    public BaseResponseBeanDeserializer(@NonNull String keyCode, @NonNull String keyMsg, @NonNull String keyData, @NonNull int resultOkCode) {
        this.gson = new Gson();
        KEY_CODE = keyCode;
        KEY_MSG = keyMsg;
        KEY_DATA = keyData;
        RESULT_OK_CODE = resultOkCode;
    }


    public BaseResponseBeanDeserializer() {
        this.gson = new Gson();
        KEY_CODE = "code";
        KEY_MSG = "msg";
        KEY_DATA = "data";
        RESULT_OK_CODE = 200;
    }

    @Override
    public BaseResponseBean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Type[] typeArguments = ((ParameterizedType) typeOfT).getActualTypeArguments();
        Type type = typeArguments[0];
        BaseResponseBean baseResponse = null;

        if (json.isJsonPrimitive()) {
            //返回的数据是基本类型
            baseResponse = new BaseResponseBean();
            baseResponse.setSuccess(true);
            baseResponse.setMessage("");
            baseResponse.setCode(RESULT_OK_CODE);
            setBaseResponseData((JsonPrimitive) json, type, baseResponse);
            return baseResponse;
        }
        if (json.isJsonObject()) {
            //返回的数据是json类型
            JsonObject value = json.getAsJsonObject();
            JsonElement code = value.get(KEY_CODE);
            JsonElement message = value.get(KEY_MSG);
            JsonElement data = value.get(KEY_DATA);
            boolean success = (code == null || (code.getAsInt() == RESULT_OK_CODE));
            baseResponse = new BaseResponseBean();
            baseResponse.setSuccess(success);
            baseResponse.setMessage(message == null ? "" : message.getAsString());
            if (!success) {
                baseResponse.setCode(code.getAsInt());
            } else {
                baseResponse.setCode(RESULT_OK_CODE);
                if (data == null || data instanceof JsonNull) {
                    try {
                        Class<?> rawType = getRawType(type);
                        if (rawType == Object.class) {
                            baseResponse.setData(new Object());
                            return baseResponse;
                        } else if (rawType == Void.class) {
                            baseResponse.setData(new Object());
                            return baseResponse;
                        }
                        if (Iterable.class.isAssignableFrom(rawType)) {
                            baseResponse.setData(gson.fromJson("[]", type));
                        } else if (rawType.isArray()) {
                            baseResponse.setData(gson.fromJson("[]", type));
                        } else {
                            if (rawType == Boolean.class) {
                                baseResponse.setData(false);
                            } else if (rawType == Character.class) {
                                baseResponse.setData("");
                            } else if (rawType == String.class) {
                                baseResponse.setData("");
                            } else if (rawType == Byte.class) {
                                baseResponse.setData(0);
                            } else if (rawType == Short.class) {
                                baseResponse.setData(0);
                            } else if (rawType == Integer.class) {
                                baseResponse.setData(0);
                            } else if (rawType == Long.class) {
                                baseResponse.setData(0);
                            } else if (rawType == Float.class) {
                                baseResponse.setData(0);
                            } else if (rawType == Double.class) {
                                baseResponse.setData(0);
                            } else {
                                baseResponse.setData(gson.fromJson("{}", type));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        baseResponse = new BaseResponseBean();
                        baseResponse.setSuccess(false);
                        baseResponse.setMessage("BaseResponseBeanDeserializer result gson parse error exception: " + e.getMessage());
                        baseResponse.setCode(Integer.MAX_VALUE);
                    }
                    return baseResponse;
                }
            }
            if (data != null) {
                if (data.isJsonPrimitive()) {
                    setBaseResponseData((JsonPrimitive) data, type, baseResponse);
                } else {
                    if (type.equals(TypeToken.get(Void.class).getType())) {
                        return baseResponse;
                    }
                    if (type.equals(TypeToken.get(String.class).getType())) {
                        baseResponse.setData(data.toString());
                    } else if (type.equals(TypeToken.get(Object.class).getType())) {
                        baseResponse.setData(data);
                    } else {
                        try {
                            baseResponse.setData(gson.fromJson(data.toString(), type));
                        } catch (Exception e) {
                            e.printStackTrace();
                            baseResponse = new BaseResponseBean();
                            baseResponse.setSuccess(false);
                            baseResponse.setMessage("BaseResponseBeanDeserializer result gson parse error exception: " + e.getMessage());
                            baseResponse.setCode(Integer.MAX_VALUE);
                            return baseResponse;
                        }
                    }
                }
            }
            return baseResponse;
        }
        if (json.isJsonArray()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                if (parameterizedType.getRawType().equals(List.class)) {
                    Type[] arguments = parameterizedType.getActualTypeArguments();
                    if (arguments.length == 1) {
                        Type argType = arguments[0];
                        JsonArray value = json.getAsJsonArray();
                        if (value.size() > 0) {
                            baseResponse = new BaseResponseBean();
                            baseResponse.setSuccess(true);
                            baseResponse.setMessage("");
                            baseResponse.setCode(RESULT_OK_CODE);
                            JsonElement element = value.get(0);
                            if (element.isJsonPrimitive()) {
                                JsonPrimitive result = (JsonPrimitive) element;
                                if (result.isString() && argType.equals(TypeToken.get(String.class).getType())) {
                                    baseResponse.setData(getStringArray(value));
                                } else if (result.isBoolean() && argType.equals(TypeToken.get(Boolean.class).getType())) {
                                    baseResponse.setData(getBooleanArray(value));
                                } else if (result.isNumber()) {
                                    if (argType.equals(TypeToken.get(Float.class).getType())) {
                                        baseResponse.setData(getFloatArray(value));
                                    } else if (argType.equals(TypeToken.get(Integer.class).getType())) {
                                        baseResponse.setData(getIntegerArray(value));
                                    } else if (argType.equals(TypeToken.get(Long.class).getType())) {
                                        baseResponse.setData(getLongArray(value));
                                    } else if (argType.equals(TypeToken.get(Double.class).getType())) {
                                        baseResponse.setData(getDoubleArray(value));
                                    } else if (argType.equals(TypeToken.get(Short.class).getType())) {
                                        baseResponse.setData(getShortArray(value));
                                    } else if (argType.equals(TypeToken.get(Byte.class).getType())) {
                                        baseResponse.setData(getByteArray(value));
                                    } else if (argType.equals(TypeToken.get(Character.class).getType())) {
                                        baseResponse.setData(getCharacterArray(value));
                                    }
                                }
                            } else if (element.isJsonObject()) {
                                baseResponse.setData(getObjectArray(value, argType));
                            }
                            return baseResponse;
                        }
                    }
                }
            }
        }
        baseResponse = new BaseResponseBean();
        baseResponse.setSuccess(false);
        baseResponse.setMessage("BaseResponseBeanDeserializer parse error json: " + json.toString() + " typeOfT:" + ((ParameterizedType) typeOfT).getRawType().getClass().getName());
        baseResponse.setCode(Integer.MAX_VALUE);
        return baseResponse;
    }

    private void setBaseResponseData(JsonPrimitive result, Type type, BaseResponseBean baseResponseBean) {
        if (result.isBoolean() && type.equals(TypeToken.get(Boolean.class).getType())) {
            baseResponseBean.setData(result.getAsBoolean());
        } else if (result.isString() && type.equals(TypeToken.get(String.class).getType())) {
            baseResponseBean.setData(result.getAsString());
        } else if (result.isString() && type.equals(TypeToken.get(Object.class).getType())) {
            baseResponseBean.setData(result.getAsString());
        } else if (result.isNumber()) {
            if (type.equals(TypeToken.get(Float.class).getType())) {
                baseResponseBean.setData(result.getAsFloat());
            } else if (type.equals(TypeToken.get(Integer.class).getType())) {
                baseResponseBean.setData(result.getAsInt());
            } else if (type.equals(TypeToken.get(Long.class).getType())) {
                baseResponseBean.setData(result.getAsLong());
            } else if (type.equals(TypeToken.get(Double.class).getType())) {
                baseResponseBean.setData(result.getAsDouble());
            } else if (type.equals(TypeToken.get(Short.class).getType())) {
                baseResponseBean.setData(result.getAsShort());
            } else if (type.equals(TypeToken.get(Byte.class).getType())) {
                baseResponseBean.setData(result.getAsByte());
            } else if (type.equals(TypeToken.get(Character.class).getType())) {
                baseResponseBean.setData(result.getAsCharacter());
            } else {
                baseResponseBean.setData(result.getAsInt());
            }
        }
    }

    private List<Object> getObjectArray(JsonArray value, Type type) {
        List<Object> datas = new ArrayList<>();
        Iterator<JsonElement> iterator = value.iterator();
        while (iterator.hasNext()) {
            JsonObject jsonObject = (JsonObject) iterator.next();
            datas.add(gson.fromJson(jsonObject.toString(), type));
        }
        return datas;
    }

    private List<String> getStringArray(JsonArray value) {
        List<String> datas = new ArrayList<>();
        Iterator<JsonElement> iterator = value.iterator();
        while (iterator.hasNext()) {
            JsonElement jsonElement = iterator.next();
            datas.add(jsonElement.getAsString());
        }
        return datas;
    }

    private List<Boolean> getBooleanArray(JsonArray value) {
        List<Boolean> datas = new ArrayList<>();
        Iterator<JsonElement> iterator = value.iterator();
        while (iterator.hasNext()) {
            JsonElement jsonElement = iterator.next();
            datas.add(jsonElement.getAsBoolean());
        }
        return datas;
    }

    private List<Float> getFloatArray(JsonArray value) {
        List<Float> datas = new ArrayList<>();
        Iterator<JsonElement> iterator = value.iterator();
        while (iterator.hasNext()) {
            JsonElement jsonElement = iterator.next();
            datas.add(jsonElement.getAsFloat());
        }
        return datas;
    }

    private List<Integer> getIntegerArray(JsonArray value) {
        List<Integer> datas = new ArrayList<>();
        Iterator<JsonElement> iterator = value.iterator();
        while (iterator.hasNext()) {
            JsonElement jsonElement = iterator.next();
            datas.add(jsonElement.getAsInt());
        }
        return datas;
    }

    private List<Long> getLongArray(JsonArray value) {
        List<Long> datas = new ArrayList<>();
        Iterator<JsonElement> iterator = value.iterator();
        while (iterator.hasNext()) {
            JsonElement jsonElement = iterator.next();
            datas.add(jsonElement.getAsLong());
        }
        return datas;
    }

    private List<Double> getDoubleArray(JsonArray value) {
        List<Double> datas = new ArrayList<>();
        Iterator<JsonElement> iterator = value.iterator();
        while (iterator.hasNext()) {
            JsonElement jsonElement = iterator.next();
            datas.add(jsonElement.getAsDouble());
        }
        return datas;
    }

    private List<Short> getShortArray(JsonArray value) {
        List<Short> datas = new ArrayList<>();
        Iterator<JsonElement> iterator = value.iterator();
        while (iterator.hasNext()) {
            JsonElement jsonElement = iterator.next();
            datas.add(jsonElement.getAsShort());
        }
        return datas;
    }

    private List<Character> getCharacterArray(JsonArray value) {
        List<Character> datas = new ArrayList<>();
        Iterator<JsonElement> iterator = value.iterator();
        while (iterator.hasNext()) {
            JsonElement jsonElement = iterator.next();
            datas.add(jsonElement.getAsCharacter());
        }
        return datas;
    }

    private List<Byte> getByteArray(JsonArray value) {
        List<Byte> datas = new ArrayList<>();
        Iterator<JsonElement> iterator = value.iterator();
        while (iterator.hasNext()) {
            JsonElement jsonElement = iterator.next();
            datas.add(jsonElement.getAsByte());
        }
        return datas;
    }

    private Class<?> getRawType(Type type) {
        checkNotNull(type, "type == null");

        if (type instanceof Class<?>) {
            // Type is a normal class.
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            // I'm not exactly sure why getRawType() returns Type instead of Class. Neal isn't either but
            // suspects some pathological case related to nested classes exists.
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) throw new IllegalArgumentException();
            return (Class<?>) rawType;
        }
        if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();
        }
        if (type instanceof TypeVariable) {
            // We could use the variable's bounds, but that won't work if there are multiple. Having a raw
            // type that's more general than necessary is okay.
            return Object.class;
        }
        if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);
        }

        throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                + "GenericArrayType, but <" + type + "> is of type " + type.getClass().getName());
    }

    private <T> T checkNotNull(T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }
}

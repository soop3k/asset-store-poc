package com.db.assetstore.domain.exception;

public class JsonTransformException extends JsonException {
    public JsonTransformException(String message) {
        super(message);
    }

    public JsonTransformException(String message, Throwable cause) {
        super(message, cause);
    }
}

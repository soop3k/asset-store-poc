package com.db.assetstore.domain.exception;

public class TransformTemplateNotFoundException extends JsonTransformException {
    public TransformTemplateNotFoundException(String templatePath) {
        super("Transform template not found: " + templatePath);
    }
}

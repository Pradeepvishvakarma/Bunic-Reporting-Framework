package com.bunic.reportingframework.exception;

import java.util.Collections;
import java.util.Map;

public class BunicInvalidRequestException extends BunicException{

    private static final long serialVersionUID = 1L;

    private final Map<String, String> errors;
    private final transient Map<String, Object> errorsObject;

    public BunicInvalidRequestException(String message) {
        super(message);
        this.errors = null;
        this.errorsObject = null;
    }

    public BunicInvalidRequestException(String message, Map<String, String> errors) {
        super(message);
        this.errors = Collections.unmodifiableMap(errors);
        this.errorsObject = null;
    }

    public BunicInvalidRequestException(Map<String, Object> errorsObject, String message) {
        super(message);
        this.errors = null;
        this.errorsObject = Collections.unmodifiableMap(errorsObject);
    }

    public Map<String, String> getErrors(){
        return errors;
    }

    public Map<String, Object> getErrorsObject(){
        return errorsObject;
    }
}

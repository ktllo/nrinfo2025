package org.leolo.nrinfo.validator;

public class SampleInputValidator implements InputValidator {

    @Override
    public boolean validate(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

package io.github.amrjlg.processor.exception;

public class DefaultExceptionHandler extends AbstractExceptionHandler<Exception> {

    public DefaultExceptionHandler() {
        super(Exception.class::isAssignableFrom);
    }

    @Override
    public String resolve(Exception e) {
        return e.getMessage();
    }
}

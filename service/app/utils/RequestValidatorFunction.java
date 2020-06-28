package utils;

import org.sunbird.exception.BaseException;

@FunctionalInterface
public interface RequestValidatorFunction<T, R> {
  R apply(T t) throws BaseException;
}

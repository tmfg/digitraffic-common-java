package fi.livi.digitraffic.common.config.resolvers;

import java.lang.reflect.Array;
import java.util.stream.Stream;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import fi.livi.digitraffic.common.annotation.CustomRequestParam;

public class NullFilteringVarargsResolver implements HandlerMethodArgumentResolver {

    // the resolver is run if parameter is annotated with @CustomRequestParam and is an array containing enums
    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CustomRequestParam.class) &&
            parameter.getParameterType().isArray() && parameter.getParameterType().getComponentType().isEnum();
    }

    @Override
    public Object[] resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer,
                                    final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) {
        final String[] parameterValues = webRequest.getParameterValues(parameter.getParameterName());
        if (parameterValues == null) {
            return null;
        }
        final Class<?> componentType = parameter.getParameterType().getComponentType();

        // filter nulls and empty strings from array of parameter values
        // before passing them on to controller method
        return Stream.of(parameterValues)
            .filter(value -> value != null && !value.isEmpty())
            .map(value -> convertValue(value, componentType))
            .toArray(size -> (Object[]) Array.newInstance(componentType, size));
    }

    private Object convertValue(final String value, final Class<?> targetType) {
        if (Enum.class.isAssignableFrom(targetType)) {
            return Enum.valueOf((Class<Enum>) targetType, value);
        }
        throw new IllegalArgumentException("Unsupported target type: " + targetType);
    }
}

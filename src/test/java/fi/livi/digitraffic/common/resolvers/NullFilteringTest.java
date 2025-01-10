package fi.livi.digitraffic.common.resolvers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import fi.livi.digitraffic.common.annotation.CustomRequestParam;
import fi.livi.digitraffic.common.config.resolvers.NullFilteringVarargsResolver;

public class NullFilteringTest {

    enum TestEnum {
        A,
        B,
        C,
        D;
    }

    private NullFilteringVarargsResolver resolver;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private ModelAndViewContainer mavContainer;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private WebDataBinderFactory binderFactory;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        resolver = new NullFilteringVarargsResolver();
    }

    @Test
    public void testSupportsParameter() {
        final String[] parameterValues = new String[] { "A", "B" };
        when(methodParameter.getParameterType()).thenReturn((Class) parameterValues.getClass());
        when(methodParameter.hasParameterAnnotation(CustomRequestParam.class)).thenReturn(true);
        when(methodParameter.getParameterType().getComponentType()).thenReturn((Class) TestEnum[].class);

        assertTrue(resolver.supportsParameter(methodParameter));
    }

    @Test
    public void testResolveArgumentWithValidEnumValues() throws Exception {
        final String[] parameterValues = new String[] { "A", "B" };

        when(webRequest.getParameterValues("arrayParameter")).thenReturn(parameterValues);
        when(methodParameter.getParameterName()).thenReturn("arrayParameter");
        when(methodParameter.getParameterType()).thenReturn((Class) parameterValues.getClass());
        when(methodParameter.getParameterType().getComponentType()).thenReturn((Class) TestEnum[].class);

        final Object[] result = resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
        assertArrayEquals(new TestEnum[] { TestEnum.A, TestEnum.B }, result);
    }

    @Test
    public void testResolveArgumentWithNullAndEmptyValues() throws Exception {
        final String[] parameterValues = new String[] { "", "C", null, "D" };

        when(webRequest.getParameterValues("arrayParameter")).thenReturn(parameterValues);
        when(methodParameter.getParameterName()).thenReturn("arrayParameter");
        when(methodParameter.getParameterType()).thenReturn((Class) parameterValues.getClass());
        when(methodParameter.getParameterType().getComponentType()).thenReturn((Class) TestEnum[].class);

        final Object[] result = resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

        assertArrayEquals(new TestEnum[] { TestEnum.C, TestEnum.D }, result);
    }

    @Test
    public void testResolveArgumentWithInvalidEnumValues() {
        final String[] parameterValues = new String[] { "INVALID" };

        when(webRequest.getParameterValues("arrayParameter")).thenReturn(parameterValues);
        when(methodParameter.getParameterName()).thenReturn("arrayParameter");
        when(methodParameter.getParameterType()).thenReturn((Class) parameterValues.getClass());
        when(methodParameter.getParameterType().getComponentType()).thenReturn((Class) TestEnum[].class);

        assertThrows(IllegalArgumentException.class, () -> {
            resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
        });
    }
}
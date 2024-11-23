package ca.bc.gov.nrs.wfprev;

import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@TestConfiguration
public class MockMvcRestExceptionConfiguration implements WebMvcConfigurer {
  private final BasicErrorController errorController;

  public MockMvcRestExceptionConfiguration (final BasicErrorController basicErrorController) {
    this.errorController = basicErrorController;
  }

  @Override
  public void addInterceptors (final InterceptorRegistry registry) {
    registry.addInterceptor(
      new HandlerInterceptor() {
        @Override
        public void afterCompletion (final HttpServletRequest request, final HttpServletResponse response, final Object handler, final Exception ex) throws Exception {
          final int status = response.getStatus();

          if (status >= 400) {
            request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, status);
            new ObjectMapper().writeValue(response.getOutputStream(), MockMvcRestExceptionConfiguration.this.errorController.error(request).getBody());
          }
        }
      });
  }
}
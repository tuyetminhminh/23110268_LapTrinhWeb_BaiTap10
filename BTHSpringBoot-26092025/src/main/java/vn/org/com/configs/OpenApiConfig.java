package vn.org.com.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(info = @Info(title = "Categories & Products API", version = "v1", description = "REST API cho Category và Product"))
@Configuration
public class OpenApiConfig {
}

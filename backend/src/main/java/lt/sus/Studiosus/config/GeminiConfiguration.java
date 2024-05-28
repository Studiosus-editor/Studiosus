package lt.sus.Studiosus.config;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class GeminiConfiguration {
  @Bean
  public VertexAI vertexAI() throws IOException {
    return new VertexAI("studiosus", "europe-central2");
  }

  @Bean
  public GenerativeModel geminiProGenerativeModel(VertexAI vertexAI) {
    return new GenerativeModel("gemini-pro", vertexAI);
  }
}

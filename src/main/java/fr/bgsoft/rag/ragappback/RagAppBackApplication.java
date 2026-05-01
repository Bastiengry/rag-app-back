package fr.bgsoft.rag.ragappback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import fr.bgsoft.rag.ragappback.config.ChatProperties;

@SpringBootApplication
@EnableConfigurationProperties(ChatProperties.class)
public class RagAppBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagAppBackApplication.class, args);
	}

}

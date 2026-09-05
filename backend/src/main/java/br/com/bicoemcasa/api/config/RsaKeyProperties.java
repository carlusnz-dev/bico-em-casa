package br.com.bicoemcasa.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.rsa")
@Getter
@Setter
public class RsaKeyProperties {
    private String privateKeyPath;
    private String publicKeyPath;
}

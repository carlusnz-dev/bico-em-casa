package br.com.bicoemcasa.api.lib.armazenamento;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "armazenamento")
@Getter
@Setter
public class ArmazenamentoProperties {
    private String endpoint;
    private String region;
    private String accessKey;
    private String secretKey;
    private long urlExpiracaoMinutos;
}

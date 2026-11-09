package br.com.bicoemcasa.api.lib.armazenamento;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.net.URL;
import java.time.Duration;

// Path-style habilitado: o MinIO nao resolve virtual-hosted-style (bucket.endpoint) sem DNS coringa.
@Component
public class ClienteArmazenamentoS3 {

    private final ArmazenamentoProperties properties;
    private final S3Presigner presigner;
    private final S3Client cliente;

    public ClienteArmazenamentoS3(ArmazenamentoProperties properties) {
        this.properties = properties;
        var endpoint = URI.create(properties.getEndpoint());
        var regiao = Region.of(properties.getRegion());
        var credenciais = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey()));
        var configuracao = S3Configuration.builder().pathStyleAccessEnabled(true).build();
        this.presigner = S3Presigner.builder()
                .endpointOverride(endpoint)
                .region(regiao)
                .credentialsProvider(credenciais)
                .serviceConfiguration(configuracao)
                .build();
        this.cliente = S3Client.builder()
                .endpointOverride(endpoint)
                .region(regiao)
                .credentialsProvider(credenciais)
                .serviceConfiguration(configuracao)
                .build();
    }

    public URL gerarUrlUpload(String bucket, String chave, String contentType) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(chave)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(properties.getUrlExpiracaoMinutos()))
                .putObjectRequest(objectRequest)
                .build();

        return presigner.presignPutObject(presignRequest).url();
    }

    public String construirUrlPublica(String bucket, String chave) {
        return "%s/%s/%s".formatted(properties.getEndpoint(), bucket, chave);
    }

    public boolean verificarObjeto(String bucket, String chave, String contentType, long limiteBytes) {
        var request = HeadObjectRequest.builder().bucket(bucket).key(chave).build();
        try {
            var objeto = cliente.headObject(request);
            return contentType.equals(objeto.contentType())
                    && objeto.contentLength() > 0 && objeto.contentLength() <= limiteBytes;
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return false;
            }
            throw ex;
        }
    }

    public void removerObjeto(String bucket, String chave) {
        cliente.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(chave).build());
    }

    public URL gerarUrlDownload(String bucket, String chave) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(chave)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(properties.getUrlExpiracaoMinutos()))
                .getObjectRequest(objectRequest)
                .build();

        return presigner.presignGetObject(presignRequest).url();
    }

    @PreDestroy
    public void fechar() {
        presigner.close();
        cliente.close();
    }
}

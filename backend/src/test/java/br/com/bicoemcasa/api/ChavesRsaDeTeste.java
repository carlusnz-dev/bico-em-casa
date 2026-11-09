package br.com.bicoemcasa.api;

import org.springframework.test.context.DynamicPropertyRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.util.Base64;

public final class ChavesRsaDeTeste {
    private ChavesRsaDeTeste() {}

    public static void configurar(DynamicPropertyRegistry props) throws GeneralSecurityException, IOException {
        var gerador = KeyPairGenerator.getInstance("RSA");
        gerador.initialize(2048);
        var par = gerador.generateKeyPair();
        Path diretorio = Files.createTempDirectory("bico-chaves-teste-");
        diretorio.toFile().deleteOnExit();
        Path publica = gravarPem(diretorio.resolve("public.pem"), "PUBLIC KEY", par.getPublic().getEncoded());
        Path privada = gravarPem(diretorio.resolve("private.pem"), "PRIVATE KEY", par.getPrivate().getEncoded());
        props.add("app.rsa.public-key-path", publica::toString);
        props.add("app.rsa.private-key-path", privada::toString);
    }

    private static Path gravarPem(Path arquivo, String tipo, byte[] chave) throws IOException {
        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(chave);
        arquivo.toFile().deleteOnExit();
        return Files.writeString(arquivo, "-----BEGIN " + tipo + "-----\n" + base64 + "\n-----END " + tipo + "-----\n");
    }
}

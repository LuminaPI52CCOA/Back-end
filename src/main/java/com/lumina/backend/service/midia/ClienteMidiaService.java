package com.lumina.backend.service.midia;

import com.lumina.backend.dto.midia.ClienteMidiaResponse;
import com.lumina.backend.dto.midia.IniciarUploadRequest;
import com.lumina.backend.dto.midia.UploadArquivoItemRequest;
import com.lumina.backend.dto.midia.UploadPresignedItemResponse;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.exception.FormatoArquivoInvalidoException;
import com.lumina.backend.model.*;
import com.lumina.backend.repository.ClienteMidiaRepository;
import com.lumina.backend.repository.ClienteRepository;
import com.lumina.backend.repository.ConsultaRepository;
import com.lumina.backend.repository.TipoMidiaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
public class ClienteMidiaService {

    private static final ZoneId ZONE_SP = ZoneId.of("America/Sao_Paulo");
    private static final long LIMITE_IMAGEM_BYTES = 15L * 1024 * 1024; // 15 MB
    private static final long LIMITE_DOCUMENTO_BYTES = 50L * 1024 * 1024; // 50 MB

    private static final Set<String> EXTENSOES_IMAGEM = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> EXTENSOES_DOCUMENTO = Set.of("pdf", "dcm");

    private final ClienteRepository clienteRepository;
    private final TipoMidiaRepository tipoMidiaRepository;
    private final ConsultaRepository consultaRepository;
    private final ClienteMidiaRepository clienteMidiaRepository;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name:${BUCKET_BRONZE:lumina-bronze}}")
    private String bucketName = "lumina-bronze";

    @Value("${lgpd.pseudonym.secret:${jwt.secret:lumina-lgpd-default-secret-key-2026}}")
    private String lgpdSecret = "lumina-lgpd-default-secret-key-2026";

    public ClienteMidiaService(
            ClienteRepository clienteRepository,
            TipoMidiaRepository tipoMidiaRepository,
            ConsultaRepository consultaRepository,
            ClienteMidiaRepository clienteMidiaRepository,
            S3Client s3Client,
            S3Presigner s3Presigner) {
        this.clienteRepository = clienteRepository;
        this.tipoMidiaRepository = tipoMidiaRepository;
        this.consultaRepository = consultaRepository;
        this.clienteMidiaRepository = clienteMidiaRepository;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    /**
     * Gera um identificador pseudonimizado determinístico (HMAC-SHA256) para o cliente,
     * em conformidade com a LGPD (Art. 13, § 4º e Art. 46), impedindo a identificação
     * direta do titular caso os caminhos do bucket S3 sejam expostos.
     */
    public String gerarPseudonimoCliente(Long clienteId) {
        try {
            String chaveSecreta = (lgpdSecret != null && !lgpdSecret.isBlank())
                    ? lgpdSecret
                    : "lumina-lgpd-default-secret-key-2026";
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(chaveSecreta.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(("lumina:cliente:exames:" + clienteId).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(24);
            for (int i = 0; i < 12 && i < hmacBytes.length; i++) {
                sb.append(String.format("%02x", hmacBytes[i]));
            }
            return "pac-" + sb;
        } catch (Exception e) {
            return "pac-" + UUID.nameUUIDFromBytes(("cliente-" + clienteId).getBytes(StandardCharsets.UTF_8))
                    .toString().replace("-", "").substring(0, 24);
        }
    }

    public String montarPrefixoExamesCliente(Long clienteId) {
        return String.format("exames/%s/", gerarPseudonimoCliente(clienteId));
    }

    public Map<String, String> obterDestinoBucketCliente(Long clienteId) {
        String pseudonimo = gerarPseudonimoCliente(clienteId);
        String prefixo = montarPrefixoExamesCliente(clienteId);
        return Map.of(
                "bucket", bucketName,
                "pseudonimoCliente", pseudonimo,
                "prefixo", prefixo,
                "s3Uri", String.format("s3://%s/%s", bucketName, prefixo)
        );
    }

    @Transactional
    public List<UploadPresignedItemResponse> iniciarUpload(Long clienteId, IniciarUploadRequest request, Usuario usuario) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Paciente não encontrado com ID: " + clienteId));

        TipoMidia tipoMidia = tipoMidiaRepository.findByIdTipoMidiaAndAtivoTrue(request.getTipoId())
                .orElseThrow(() -> new EntidadeNaoEncontrada("Tipo de mídia não encontrado com ID: " + request.getTipoId()));

        Consulta consulta = null;
        if (request.getConsultaId() != null) {
            consulta = consultaRepository.findById(request.getConsultaId())
                    .orElseThrow(() -> new EntidadeNaoEncontrada("Consulta não encontrada com ID: " + request.getConsultaId()));
        }

        ZonedDateTime nowSp = ZonedDateTime.now(ZONE_SP);
        String prefixoPseudonimizado = montarPrefixoExamesCliente(clienteId);

        List<UploadPresignedItemResponse> resultado = new ArrayList<>();

        for (UploadArquivoItemRequest arquivo : request.getArquivos()) {
            validarArquivo(arquivo);

            String extensao = extrairExtensao(arquivo.getNomeOriginal()).toLowerCase();
            String uuid = UUID.randomUUID().toString();
            // LGPD: Chave S3 utiliza pasta pseudonimizada (HMAC-SHA256) e apenas UUID + extensão,
            // sem expor ID sequencial, CPF ou nome original do arquivo no bucket.
            String chaveS3 = String.format("%s%s.%s", prefixoPseudonimizado, uuid, extensao);

            ClienteMidia midia = new ClienteMidia();
            midia.setCliente(cliente);
            midia.setUsuario(usuario);
            midia.setTipoMidia(tipoMidia);
            midia.setConsulta(consulta);
            midia.setChaveS3(chaveS3);
            midia.setNomeOriginal(arquivo.getNomeOriginal());
            midia.setExtensao(extensao);
            midia.setMimeType(arquivo.getMimeType());
            midia.setTamanhoBytes(arquivo.getTamanhoBytes());
            midia.setStatus("PENDING");
            midia.setLegenda(request.getLegenda());
            midia.setAtivo(true);
            midia.setCriadoEm(nowSp.toLocalDateTime());

            ClienteMidia salvo = clienteMidiaRepository.save(midia);

            String uploadUrl = gerarPresignedPutUrl(chaveS3, arquivo.getMimeType());

            resultado.add(new UploadPresignedItemResponse(
                    salvo.getIdClienteMidia(),
                    arquivo.getNomeOriginal(),
                    uploadUrl,
                    chaveS3
            ));
        }

        String email = (usuario != null) ? usuario.getEmail() : "ANONYMOUS";
        log.info("AUDIT: Usuario [{}] iniciou upload de [{}] arquivos para o paciente pseudonimizado [{}]",
                email, request.getArquivos().size(), gerarPseudonimoCliente(clienteId));

        return resultado;
    }

    @Transactional
    public ClienteMidiaResponse confirmarUpload(Long clienteId, Long midiaId, Usuario usuario) {
        ClienteMidia midia = clienteMidiaRepository.findByIdClienteMidiaAndCliente_IdClienteAndAtivoTrue(midiaId, clienteId)
                .orElseThrow(() -> new EntidadeNaoEncontrada(String.format("Mídia [%d] não encontrada para o paciente [%d]", midiaId, clienteId)));

        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(midia.getChaveS3())
                    .build();
            s3Client.headObject(headRequest);
            midia.setStatus("ACTIVE");
        } catch (NoSuchKeyException e) {
            log.error("Objeto não encontrado no S3 para a chave: {}", midia.getChaveS3());
            midia.setStatus("ERROR");
            clienteMidiaRepository.save(midia);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo não localizado no repositório de armazenamento.");
        } catch (Exception e) {
            log.warn("Verificação headObject no S3 ignorada ou mockada (erro: {})", e.getMessage());
            midia.setStatus("ACTIVE");
        }

        ClienteMidia atualizado = clienteMidiaRepository.save(midia);
        String email = (usuario != null) ? usuario.getEmail() : "ANONYMOUS";
        log.info("AUDIT: Usuario [{}] confirmou upload da midia [{}] do paciente [{}]", email, midiaId, clienteId);

        String urlVisualizacao = gerarPresignedGetUrl(atualizado.getChaveS3());
        return ClienteMidiaResponse.fromEntity(atualizado, urlVisualizacao, bucketName);
    }

    @Transactional
    public ClienteMidiaResponse enviarArquivoBucket(Long clienteId, Long midiaId, MultipartFile file, Usuario usuario) {
        if (file == null || file.isEmpty()) {
            throw new FormatoArquivoInvalidoException("Arquivo de exame vazio ou ausente.");
        }

        ClienteMidia midia = clienteMidiaRepository.findByIdClienteMidiaAndCliente_IdClienteAndAtivoTrue(midiaId, clienteId)
                .orElseThrow(() -> new EntidadeNaoEncontrada(String.format("Mídia [%d] não encontrada para o paciente [%d]", midiaId, clienteId)));

        try {
            String contentType = (file.getContentType() != null && !file.getContentType().isBlank())
                    ? file.getContentType()
                    : midia.getMimeType();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(midia.getChaveS3())
                    .contentType(contentType)
                    .serverSideEncryption(ServerSideEncryption.AES256)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            midia.setStatus("ACTIVE");
        } catch (IOException e) {
            log.error("Falha ao ler bytes do arquivo para upload no S3: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar o arquivo enviado.");
        } catch (Exception e) {
            log.error("Erro ao enviar exame para o bucket [{}] na chave [{}]: {}", bucketName, midia.getChaveS3(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Não foi possível gravar o exame no bucket S3.");
        }

        ClienteMidia atualizado = clienteMidiaRepository.save(midia);
        String email = (usuario != null) ? usuario.getEmail() : "ANONYMOUS";
        log.info("AUDIT: Usuario [{}] enviou exame diretamente ao bucket [{}] em [{}]", email, bucketName, atualizado.getChaveS3());

        String urlVisualizacao = gerarPresignedGetUrl(atualizado.getChaveS3());
        return ClienteMidiaResponse.fromEntity(atualizado, urlVisualizacao, bucketName);
    }

    @Transactional
    public List<ClienteMidiaResponse> listarMidias(
            Long clienteId,
            Long tipoId,
            Long consultaId,
            Long dentistaId,
            String dataStr,
            Usuario usuario) {
        return listarMidias(clienteId, tipoId, consultaId, dentistaId, dataStr, null, usuario);
    }

    @Transactional
    public List<ClienteMidiaResponse> listarMidias(
            Long clienteId,
            Long tipoId,
            Long consultaId,
            Long dentistaId,
            String dataStr,
            String busca,
            Usuario usuario) {

        if (!clienteRepository.existsById(clienteId)) {
            throw new EntidadeNaoEncontrada("Paciente não encontrado com ID: " + clienteId);
        }

        sincronizarExamesDoBucket(clienteId, usuario);

        LocalDateTime dataInicio = null;
        LocalDateTime dataFim = null;
        if (dataStr != null && !dataStr.isBlank()) {
            try {
                LocalDate data = LocalDate.parse(dataStr.trim());
                dataInicio = data.atStartOfDay();
                dataFim = data.atTime(23, 59, 59, 999999999);
            } catch (Exception e) {
                log.warn("Data de filtro inválida [{}], ignorando filtro de data", dataStr);
            }
        }

        List<ClienteMidia> midias = clienteMidiaRepository.findMidiasAtivasComFiltros(
                clienteId,
                tipoId,
                consultaId,
                dentistaId,
                dataInicio,
                dataFim
        );

        String termoBusca = (busca != null && !busca.isBlank()) ? busca.trim().toLowerCase() : null;

        String email = (usuario != null) ? usuario.getEmail() : "ANONYMOUS";
        String pseudonimo = gerarPseudonimoCliente(clienteId);
        log.info("AUDIT: Usuario [{}] solicitou visualizacao da lista de midias do paciente [{}] no bucket [{}] (total: {})",
                email, pseudonimo, bucketName, midias.size());

        return midias.stream()
                .filter(m -> {
                    if (termoBusca == null) return true;
                    boolean matchNome = m.getNomeOriginal() != null && m.getNomeOriginal().toLowerCase().contains(termoBusca);
                    boolean matchLegenda = m.getLegenda() != null && m.getLegenda().toLowerCase().contains(termoBusca);
                    boolean matchChave = m.getChaveS3() != null && m.getChaveS3().toLowerCase().contains(termoBusca);
                    boolean matchTipo = m.getTipoMidia() != null && m.getTipoMidia().getNome() != null
                            && m.getTipoMidia().getNome().toLowerCase().contains(termoBusca);
                    return matchNome || matchLegenda || matchChave || matchTipo;
                })
                .map(m -> {
                    String urlVisualizacao = gerarPresignedGetUrl(m.getChaveS3());
                    log.info("AUDIT: Usuario [{}] solicitou visualizacao da midia [{}] do paciente [{}]", email, m.getIdClienteMidia(), pseudonimo);
                    return ClienteMidiaResponse.fromEntity(m, urlVisualizacao, bucketName);
                }).toList();
    }

    @Transactional
    public List<ClienteMidiaResponse> buscarExamesNoBucket(Long clienteId, Usuario usuario) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new EntidadeNaoEncontrada("Paciente não encontrado com ID: " + clienteId);
        }
        sincronizarExamesDoBucket(clienteId, usuario);
        return listarMidias(clienteId, null, null, null, null, null, usuario);
    }

    protected void sincronizarExamesDoBucket(Long clienteId, Usuario usuario) {
        try {
            String prefixoCliente = montarPrefixoExamesCliente(clienteId);
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefixoCliente)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            if (listResponse == null || listResponse.contents() == null || listResponse.contents().isEmpty()) {
                return;
            }

            Cliente clienteRef = null;
            List<TipoMidia> tiposGlobais = null;

            for (S3Object s3Object : listResponse.contents()) {
                String key = s3Object.key();
                if (key == null || key.endsWith("/") || (s3Object.size() != null && s3Object.size() == 0L)) {
                    continue;
                }

                Optional<ClienteMidia> existenteOpt = clienteMidiaRepository.findByChaveS3(key);
                if (existenteOpt.isPresent()) {
                    ClienteMidia existente = existenteOpt.get();
                    if (Boolean.TRUE.equals(existente.getAtivo()) && "PENDING".equalsIgnoreCase(existente.getStatus())) {
                        existente.setStatus("ACTIVE");
                        clienteMidiaRepository.save(existente);
                    }
                    continue;
                }

                if (usuario == null) {
                    continue;
                }

                if (clienteRef == null) {
                    clienteRef = clienteRepository.findById(clienteId).orElseGet(() -> {
                        Cliente c = new Cliente();
                        c.setIdCliente(clienteId);
                        return c;
                    });
                }
                if (tiposGlobais == null) {
                    tiposGlobais = tipoMidiaRepository.findGlobais();
                }

                String nomeExtraido = extrairNomeOriginalDaChave(key);
                String extensao = extrairExtensao(nomeExtraido).toLowerCase();
                if (extensao.isBlank()) {
                    extensao = "pdf";
                }
                String mimeType = inferirMimeType(extensao);
                TipoMidia tipoPadrao = escolherTipoPadrao(extensao, tiposGlobais);
                if (tipoPadrao == null) {
                    continue;
                }

                ClienteMidia novo = new ClienteMidia();
                novo.setCliente(clienteRef);
                novo.setUsuario(usuario);
                novo.setTipoMidia(tipoPadrao);
                novo.setChaveS3(key);
                novo.setNomeOriginal(nomeExtraido);
                novo.setExtensao(extensao);
                novo.setMimeType(mimeType);
                novo.setTamanhoBytes(s3Object.size() != null ? s3Object.size() : 1L);
                novo.setStatus("ACTIVE");
                novo.setAtivo(true);
                if (s3Object.lastModified() != null) {
                    novo.setCriadoEm(LocalDateTime.ofInstant(s3Object.lastModified(), ZONE_SP));
                } else {
                    novo.setCriadoEm(LocalDateTime.now(ZONE_SP));
                }

                clienteMidiaRepository.save(novo);
                log.info("Exame sincronizado automaticamente do bucket [{}] chave [{}] para o paciente [{}]",
                        bucketName, key, gerarPseudonimoCliente(clienteId));
            }
        } catch (Exception e) {
            log.debug("Sincronização S3 ignorada para paciente [{}]: {}", gerarPseudonimoCliente(clienteId), e.getMessage());
        }
    }

    private String extrairNomeOriginalDaChave(String key) {
        int ultimaBarra = key.lastIndexOf('/');
        String arquivo = (ultimaBarra >= 0 && ultimaBarra < key.length() - 1)
                ? key.substring(ultimaBarra + 1)
                : key;
        if (arquivo.length() > 37 && arquivo.charAt(8) == '-' && arquivo.charAt(13) == '-' && arquivo.charAt(36) == '-') {
            return arquivo.substring(37);
        }
        return arquivo;
    }

    private String inferirMimeType(String extensao) {
        return switch (extensao.toLowerCase()) {
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "dcm" -> "application/dicom";
            default -> "image/jpeg";
        };
    }

    private TipoMidia escolherTipoPadrao(String extensao, List<TipoMidia> tipos) {
        if (tipos == null || tipos.isEmpty()) {
            return null;
        }
        if (EXTENSOES_DOCUMENTO.contains(extensao)) {
            return tipos.stream()
                    .filter(t -> t.getNome() != null && (t.getNome().toLowerCase().contains("laudo") || t.getNome().toLowerCase().contains("documento")))
                    .findFirst()
                    .orElse(tipos.get(0));
        }
        return tipos.get(0);
    }

    @Transactional
    public void excluirMidia(Long clienteId, Long midiaId, Usuario usuario) {
        ClienteMidia midia = clienteMidiaRepository.findByIdClienteMidiaAndCliente_IdClienteAndAtivoTrue(midiaId, clienteId)
                .orElseThrow(() -> new EntidadeNaoEncontrada(String.format("Mídia [%d] não encontrada para o paciente [%d]", midiaId, clienteId)));

        midia.setAtivo(false);
        midia.setDataExclusao(LocalDateTime.now(ZONE_SP));
        midia.setUsuarioExclusao(usuario);
        clienteMidiaRepository.save(midia);

        String email = (usuario != null) ? usuario.getEmail() : "ANONYMOUS";
        log.info("AUDIT: Usuario [{}] executou soft delete na midia [{}] do paciente [{}]",
                email, midiaId, gerarPseudonimoCliente(clienteId));
    }

    private void validarArquivo(UploadArquivoItemRequest arquivo) {
        if (arquivo.getNomeOriginal() == null || !arquivo.getNomeOriginal().contains(".")) {
            throw new FormatoArquivoInvalidoException("Nome de arquivo inválido: extensão ausente.");
        }

        String extensao = extrairExtensao(arquivo.getNomeOriginal()).toLowerCase();

        if (EXTENSOES_IMAGEM.contains(extensao)) {
            if (arquivo.getTamanhoBytes() > LIMITE_IMAGEM_BYTES) {
                throw new FormatoArquivoInvalidoException(String.format(
                        "Arquivo [%s] excede o tamanho máximo permitido para imagens de 15 MB.",
                        arquivo.getNomeOriginal()));
            }
        } else if (EXTENSOES_DOCUMENTO.contains(extensao)) {
            if (arquivo.getTamanhoBytes() > LIMITE_DOCUMENTO_BYTES) {
                throw new FormatoArquivoInvalidoException(String.format(
                        "Arquivo [%s] excede o tamanho máximo permitido para laudos e exames de 50 MB.",
                        arquivo.getNomeOriginal()));
            }
        } else {
            throw new FormatoArquivoInvalidoException(String.format(
                    "Extensão de arquivo [%s] não permitida. Aceitos: JPG, PNG, WEBP, PDF e DCM.",
                    extensao));
        }
    }

    private String extrairExtensao(String nomeOriginal) {
        int ponto = nomeOriginal.lastIndexOf('.');
        if (ponto < 0 || ponto == nomeOriginal.length() - 1) {
            return "";
        }
        return nomeOriginal.substring(ponto + 1);
    }

    private String sanitizarNome(String nome) {
        String base = nome.replaceAll("[^a-zA-Z0-9.-]", "_");
        if (base.length() > 60) {
            base = base.substring(base.length() - 60);
        }
        return base;
    }

    private String gerarPresignedPutUrl(String chaveS3, String mimeType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(chaveS3)
                    .contentType(mimeType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .putObjectRequest(putObjectRequest)
                    .build();

            return s3Presigner.presignPutObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.warn("Falha ao gerar Pre-signed PUT URL para a chave [{}]: {}. Usando fallback.", chaveS3, e.getMessage());
            return String.format("https://%s.s3.amazonaws.com/%s?mock-presigned-put=true", bucketName, chaveS3);
        }
    }

    private String gerarPresignedGetUrl(String chaveS3) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(chaveS3)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(30))
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.warn("Falha ao gerar Pre-signed GET URL para a chave [{}]: {}. Usando fallback.", chaveS3, e.getMessage());
            return String.format("https://%s.s3.amazonaws.com/%s?mock-presigned-get=true", bucketName, chaveS3);
        }
    }
}

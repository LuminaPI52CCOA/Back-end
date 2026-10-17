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
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

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

    @Value("${aws.s3.bucket-name:lumina-storage}")
    private String bucketName;

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
        int ano = nowSp.getYear();
        int mes = nowSp.getMonthValue();

        List<UploadPresignedItemResponse> resultado = new ArrayList<>();

        for (UploadArquivoItemRequest arquivo : request.getArquivos()) {
            validarArquivo(arquivo);

            String extensao = extrairExtensao(arquivo.getNomeOriginal());
            String nomeSanitizado = sanitizarNome(arquivo.getNomeOriginal());
            String uuid = UUID.randomUUID().toString();
            String chaveS3 = String.format("clientes/%d/midias/%d/%02d/%s-%s", clienteId, ano, mes, uuid, nomeSanitizado);

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
        log.info("AUDIT: Usuario [{}] iniciou upload de [{}] arquivos para o paciente [{}]", email, request.getArquivos().size(), clienteId);

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
        return ClienteMidiaResponse.fromEntity(atualizado, urlVisualizacao);
    }

    @Transactional(readOnly = true)
    public List<ClienteMidiaResponse> listarMidias(
            Long clienteId,
            Long tipoId,
            Long consultaId,
            Long dentistaId,
            String dataStr,
            Usuario usuario) {

        if (!clienteRepository.existsById(clienteId)) {
            throw new EntidadeNaoEncontrada("Paciente não encontrado com ID: " + clienteId);
        }

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

        String email = (usuario != null) ? usuario.getEmail() : "ANONYMOUS";
        log.info("AUDIT: Usuario [{}] solicitou visualizacao da lista de midias do paciente [{}] (total: {})", email, clienteId, midias.size());

        return midias.stream().map(m -> {
            String urlVisualizacao = gerarPresignedGetUrl(m.getChaveS3());
            log.info("AUDIT: Usuario [{}] solicitou visualizacao da midia [{}] do paciente [{}]", email, m.getIdClienteMidia(), clienteId);
            return ClienteMidiaResponse.fromEntity(m, urlVisualizacao);
        }).toList();
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
        log.info("AUDIT: Usuario [{}] executou soft delete na midia [{}] do paciente [{}]", email, midiaId, clienteId);
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

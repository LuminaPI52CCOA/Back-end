package com.lumina.backend.service.anamnese;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.backend.dto.anamnese.AnamneseMapper;
import com.lumina.backend.dto.anamnese.OcrRespostaDTO;
import com.lumina.backend.exception.AnamneseVazio;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.exception.FormatoArquivoInvalidoException;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.repository.AnamneseRepository;
import com.lumina.backend.service.openIA.GeminiAIService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextResponse;
import software.amazon.awssdk.services.textract.model.Document;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnamneseService {

    private static final List<String> TIPOS_PERMITIDOS = List.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "application/pdf"
    );

    private static final long TAMANHO_MAXIMO_BYTES = 10 * 1024 * 1024; // 10MB

    private final AnamneseRepository repository;
    private final TextractClient textractClient;
    private final GeminiAIService geminiAIService;
    private AnamneseMapper anamneseMapper;

    public AnamneseService(AnamneseRepository repository, TextractClient textractClient, GeminiAIService geminiAIService,  AnamneseMapper anamneseMapper) {
        this.repository = repository;
        this.textractClient = textractClient;
        this.geminiAIService = geminiAIService;
        this.anamneseMapper = anamneseMapper;
    }

    public Anamnese processImage(
            MultipartFile file,
            Long clienteId
    ) throws IOException {

        log.trace("Processando imagem da anamnese no OCR");

        if(file == null || file.isEmpty()){
            throw new AnamneseVazio("Arquivo não encontrado.");
        }

        if (file.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new FormatoArquivoInvalidoException("Tamanho do arquivo excede o limite máximo permitido de 10MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new FormatoArquivoInvalidoException("Tipo de arquivo inválido. Apenas PNG, JPEG ou PDF são aceitos.");
        }

        byte[] bytes = file.getBytes();

        DetectDocumentTextRequest request =
                DetectDocumentTextRequest.builder()
                        .document(
                                Document.builder()
                                        .bytes(
                                                SdkBytes.fromByteArray(bytes)
                                        )
                                        .build()
                        )
                        .build();

        DetectDocumentTextResponse response =
                textractClient.detectDocumentText(
                        request
                );

        String ocr =
                response.blocks()
                        .stream()
                        .filter(
                                block ->
                                        block.blockType() == BlockType.LINE
                        )
                        .map(
                                block -> block.text()
                        )
                        .collect(
                                Collectors.joining("\n")
                        );

        ObjectMapper mapper = new ObjectMapper();

        log.trace("Transformando as informações da OCR em Json");
        String retornoApi = geminiAIService.transformarEmJson(ocr);
        log.trace("Limpando os dados divergentes");
        JsonNode json = geminiAIService.limparJsonGemini(retornoApi);
        log.trace("Realizando insert no banco de dados");

        if (json == null || json.isNull() || json.isEmpty()) {
            throw new FormatoArquivoInvalidoException("Resposta do processamento de IA inválida ou vazia.");
        }

        OcrRespostaDTO.FichaAnamneseWrapperDTO ficha;
        try {
            ficha = mapper.treeToValue(json, OcrRespostaDTO.FichaAnamneseWrapperDTO.class);
        } catch (Exception e) {
            throw new FormatoArquivoInvalidoException("Estrutura do JSON retornado pela IA é inválida.");
        }

        if (ficha == null || ficha.fichaAnamnese() == null || ficha.fichaAnamnese().perguntas() == null) {
            throw new FormatoArquivoInvalidoException("Campos obrigatórios da ficha de anamnese não encontrados na resposta da IA.");
        }

        OcrRespostaDTO.PerguntasAnamneseDTO perguntas = ficha.fichaAnamnese().perguntas();

        Anamnese anamnese = new Anamnese();

        anamnese.setDataAnamnese(LocalDate.now());
        anamnese.setFazendoTratamento(getRespostaSim(perguntas.q1()));
        anamnese.setDescricaoTratamento(getDetalhes(perguntas.q1()));
        anamnese.setDoresCabecaFaceAtm(getRespostaSim(perguntas.q2()));
        anamnese.setAlergiaMedicamentos(getRespostaSim(perguntas.q3()));
        anamnese.setDescricaoAlergiaMedicamentos(getDetalhes(perguntas.q3()));
        anamnese.setReacaoAnestesiaLocal(getRespostaSim(perguntas.q4()));
        anamnese.setSensibilidadeDentaria(getRespostaSim(perguntas.q5()));
        anamnese.setBruxismoApertamento(getRespostaSim(perguntas.q6()));
        anamnese.setSangramentoGengival(getRespostaSim(perguntas.q7()));
        anamnese.setPossuiHabito(getRespostaSim(perguntas.q8()));
        anamnese.setDescricaoHabito(getDetalhes(perguntas.q8()));
        anamnese.setHistoricoDiabetes(getRespostaSim(perguntas.q9()));
        anamnese.setSangramentoExcessivo(getRespostaSim(perguntas.q10()));
        anamnese.setProblemaCardiaco(getRespostaSim(perguntas.q11()));
        anamnese.setDescricaoProblemaCardiaco(getDetalhes(perguntas.q11()));
        anamnese.setPressaoArterialNormal(getRespostaSim(perguntas.q12()));
        anamnese.setDescricaoPressaoArterial(getDetalhes(perguntas.q12()));
        anamnese.setHistoricoDesmaioConvulsao(getRespostaSim(perguntas.q13()));
        anamnese.setGestante(getRespostaSim(perguntas.q14()));

        if (clienteId != null) {
            Cliente cliente = new Cliente();
            cliente.setIdCliente(clienteId);
            anamnese.setFkCliente(cliente);
        }

        return repository.save(anamnese);
    }

    private Boolean getRespostaSim(OcrRespostaDTO.DetalheRespostaDTO detalhe) {
        return detalhe != null ? detalhe.respostaSim() : false;
    }

    private String getDetalhes(OcrRespostaDTO.DetalheRespostaDTO detalhe) {
        return detalhe != null ? detalhe.detalhes() : null;
    }

    public Anamnese processImage(MultipartFile file) throws IOException {
        return processImage(file, null);
    }


    public Anamnese buscarPorId(Integer id){
        return repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Anamnese não encontrada!"));
    }
}

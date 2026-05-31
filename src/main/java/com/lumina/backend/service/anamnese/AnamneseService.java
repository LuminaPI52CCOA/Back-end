package com.lumina.backend.service.anamnese;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.backend.dto.anamnese.AnamneseMapper;
import com.lumina.backend.dto.anamnese.OcrRespostaDTO;
import com.lumina.backend.exception.AnamneseVazio;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
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
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnamneseService {

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
            MultipartFile file
    ) throws IOException {

        log.trace("Processando imagem da anamnese no OCR");

        if(file.isEmpty()){
            throw new AnamneseVazio("Arquivo não encontrado.");
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

            OcrRespostaDTO.FichaAnamneseWrapperDTO ficha = mapper.treeToValue(json, OcrRespostaDTO.FichaAnamneseWrapperDTO.class);
            OcrRespostaDTO.PerguntasAnamneseDTO perguntas = ficha.fichaAnamnese().perguntas();


        Anamnese anamnese = new Anamnese();

        anamnese.setDataAnamnese(LocalDate.now());
        anamnese.setFazendoTratamento(perguntas.q1().respostaSim());
        anamnese.setDescricaoTratamento(perguntas.q1().detalhes());
        anamnese.setDoresCabecaFaceAtm(perguntas.q2().respostaSim());
        anamnese.setAlergiaMedicamentos(perguntas.q3().respostaSim());
        anamnese.setDescricaoAlergiaMedicamentos(perguntas.q3().detalhes());
        anamnese.setReacaoAnestesiaLocal(perguntas.q4().respostaSim());
        anamnese.setSensibilidadeDentaria(perguntas.q5().respostaSim());
        anamnese.setBruxismoApertamento(perguntas.q6().respostaSim());
        anamnese.setSangramentoGengival(perguntas.q7().respostaSim());
        anamnese.setPossuiHabito(perguntas.q8().respostaSim());
        anamnese.setDescricaoHabito(perguntas.q8().detalhes());
        anamnese.setHistoricoDiabetes(perguntas.q9().respostaSim());
        anamnese.setSangramentoExcessivo(perguntas.q10().respostaSim());
        anamnese.setProblemaCardiaco(perguntas.q11().respostaSim());
        anamnese.setDescricaoProblemaCardiaco(perguntas.q11().detalhes());
        anamnese.setPressaoArterialNormal(perguntas.q12().respostaSim());
        anamnese.setDescricaoPressaoArterial(perguntas.q12().detalhes());
        anamnese.setHistoricoDiabetes(perguntas.q13().respostaSim());
        anamnese.setGestante(perguntas.q14().respostaSim());

        Cliente cliente = new Cliente();
        cliente.setIdCliente(1L);

            anamnese.setFkCliente(cliente);

        return repository.save(anamnese);
    }


    public Anamnese buscarPorId(Integer id){
        return repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Anamnese não encontrada!"));
    }
}

package com.lumina.backend.service.anamnese;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.backend.dto.anamnese.AnamneseMapper;
import com.lumina.backend.exception.AnamneseVazio;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.repository.AnamneseRepository;
import com.lumina.backend.service.openIA.GeminiAIService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.Block;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class AnamneseServiceTest {

    @Mock
    private AnamneseRepository repository;

    @Mock
    private TextractClient textractClient;

    @Mock
    private GeminiAIService geminiAIService;

    @Mock
    private AnamneseMapper anamneseMapper;

    @InjectMocks
    private AnamneseService anamneseService;

    @Nested
    @DisplayName("Testes do metodo processImage")
    class ProcessarImagemTest {

	@Test
	@DisplayName("Deve processar imagem e salvar anamnese corretamente")
	void deveProcessarImagemESalvarAnamneseCorretamente() throws IOException {
	    MultipartFile arquivo = new MockMultipartFile(
		    "arquivo",
		    "anamnese.png",
		    "image/png",
		    "conteudo-imagem".getBytes()
	    );

	    DetectDocumentTextResponse respostaTextract = DetectDocumentTextResponse.builder()
		    .blocks(
			    Block.builder().blockType(BlockType.LINE).text("Pergunta 1").build(),
			    Block.builder().blockType(BlockType.LINE).text("Pergunta 2").build()
		    )
		    .build();

	    String retornoGeminiBruto = "retorno-gemini";

	    String jsonValido = """
		    {
		      "ficha_anamnese": {
			"perguntas": {
			  "q1_tratamento_medico": {"resposta_sim": true, "detalhes": "Uso de antibiotico"},
			  "q2_dores_cabeca_face_ouvido_articulacao": {"resposta_sim": false, "detalhes": null},
			  "q3_alergia_medicamento": {"resposta_sim": true, "detalhes": "Dipirona"},
			  "q4_reacao_anestesia_local": {"resposta_sim": false, "detalhes": null},
			  "q5_sensibilidade_dentes": {"resposta_sim": true, "detalhes": null},
			  "q6_range_dentes_apertamento": {"resposta_sim": false, "detalhes": null},
			  "q7_gengiva_sangra_frequencia": {"resposta_sim": true, "detalhes": null},
			  "q8_tem_algum_habito": {"resposta_sim": true, "detalhes": "Roer unhas"},
			  "q9_diabetico_ou_historico_familiar": {"resposta_sim": true, "detalhes": null},
			  "q10_sangramento_excessivo_cortes": {"resposta_sim": false, "detalhes": null},
			  "q11_problema_cardiaco": {"resposta_sim": true, "detalhes": "Arritmia"},
			  "q12_pressao_arterial_normal": {"resposta_sim": true, "detalhes": "12 por 8"},
			  "q13_desmaio_ataques_epilepsia_convulsao": {"resposta_sim": true, "detalhes": null},
			  "q14_esta_gravida": {"resposta_sim": false, "detalhes": null}
			}
		      }
		    }
		    """;

	    ObjectMapper objectMapper = new ObjectMapper();
	    JsonNode jsonNodeValido = objectMapper.readTree(jsonValido);

	    Mockito.when(textractClient.detectDocumentText(any(DetectDocumentTextRequest.class)))
       .thenReturn(respostaTextract);;
	    Mockito.when(geminiAIService.transformarEmJson(anyString()))
		    .thenReturn(retornoGeminiBruto);
	    Mockito.when(geminiAIService.limparJsonGemini(retornoGeminiBruto))
		    .thenReturn(jsonNodeValido);
	    Mockito.when(repository.save(any(Anamnese.class)))
		    .thenAnswer(invocation -> invocation.getArgument(0));

	    Anamnese resultado = anamneseService.processImage(arquivo);

	    ArgumentCaptor<Anamnese> captor = ArgumentCaptor.forClass(Anamnese.class);
	    Mockito.verify(repository, Mockito.times(1)).save(captor.capture());

	    Anamnese anamneseSalva = captor.getValue();
	    assertNotNull(resultado);
	    assertNotNull(anamneseSalva.getDataAnamnese());
	    assertEquals(LocalDate.now(), anamneseSalva.getDataAnamnese());
	    assertEquals(true, anamneseSalva.getFazendoTratamento());
	    assertEquals("Uso de antibiotico", anamneseSalva.getDescricaoTratamento());
	    assertEquals(true, anamneseSalva.getAlergiaMedicamentos());
	    assertEquals("Dipirona", anamneseSalva.getDescricaoAlergiaMedicamentos());
	    assertEquals(true, anamneseSalva.getHistoricoDiabetes());
	    assertEquals(false, anamneseSalva.getGestante());
	    assertNotNull(anamneseSalva.getFkCliente());
	    assertEquals(1L, anamneseSalva.getFkCliente().getIdCliente());

	    Mockito.verify(textractClient, Mockito.times(1)).detectDocumentText(any(DetectDocumentTextRequest.class));;
	    Mockito.verify(geminiAIService, Mockito.times(1)).transformarEmJson(anyString());
	    Mockito.verify(geminiAIService, Mockito.times(1)).limparJsonGemini(retornoGeminiBruto);
	}

	@Test
	@DisplayName("Deve lancar excecao quando arquivo estiver vazio")
	void deveLancarExcecaoQuandoArquivoEstiverVazio() {
	    MultipartFile arquivoVazio = new MockMultipartFile(
		    "arquivo",
		    "anamnese.png",
		    "image/png",
		    new byte[0]
	    );

	    assertThrows(AnamneseVazio.class, () -> anamneseService.processImage(arquivoVazio));

	    Mockito.verifyNoInteractions(textractClient);
	    Mockito.verifyNoInteractions(geminiAIService);
	    Mockito.verifyNoInteractions(repository);
	}
    }

    @Nested
    @DisplayName("Testes do metodo buscarPorId")
    class BuscarPorIdTest {

	@Test
	@DisplayName("Deve retornar anamnese quando id existir")
	void deveRetornarAnamneseQuandoIdExistir() {
	    Anamnese anamnese = new Anamnese();
	    anamnese.setIdAnamnese(10L);

	    Mockito.when(repository.findById(anyInt()))
		    .thenReturn(Optional.of(anamnese));

	    Anamnese resultado = anamneseService.buscarPorId(10);

	    assertNotNull(resultado);
	    assertEquals(10L, resultado.getIdAnamnese());
	    Mockito.verify(repository, Mockito.times(1)).findById(10);
	}

	@Test
	@DisplayName("Deve lancar excecao quando id nao existir")
	void deveLancarExcecaoQuandoIdNaoExistir() {
	    Mockito.when(repository.findById(anyInt()))
		    .thenReturn(Optional.empty());

	    assertThrows(EntidadeNaoEncontrada.class, () -> anamneseService.buscarPorId(99));

	    Mockito.verify(repository, Mockito.times(1)).findById(99);
	}
    }
}

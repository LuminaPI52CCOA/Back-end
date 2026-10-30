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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteMidiaServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private TipoMidiaRepository tipoMidiaRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private ClienteMidiaRepository clienteMidiaRepository;

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private ClienteMidiaService clienteMidiaService;

    private Usuario usuario;
    private Cliente cliente;
    private TipoMidia tipoMidia;
    private ClienteMidia midiaExistente;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(clienteMidiaService, "bucketName", "lumina-bronze");

        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setNome("Dr. John Doe");
        usuario.setEmail("john@doe.com");

        cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNome("Carlos Silva");

        tipoMidia = new TipoMidia(1L, "Foto Intraoral", "Fotos da cavidade bucal", null, true);

        midiaExistente = new ClienteMidia();
        midiaExistente.setIdClienteMidia(101L);
        midiaExistente.setCliente(cliente);
        midiaExistente.setUsuario(usuario);
        midiaExistente.setTipoMidia(tipoMidia);
        midiaExistente.setChaveS3("exames/pac-9f86d081884c7d659a2feaa0/uuid.jpg");
        midiaExistente.setNomeOriginal("foto-01.jpg");
        midiaExistente.setExtensao("jpg");
        midiaExistente.setMimeType("image/jpeg");
        midiaExistente.setTamanhoBytes(2048L);
        midiaExistente.setStatus("PENDING");
        midiaExistente.setAtivo(true);
        midiaExistente.setCriadoEm(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve iniciar upload em lote gerando Pre-signed PUT URLs na pasta pseudonimizada exames/pac-{hash} (LGPD)")
    void iniciarUpload_ComSucesso() throws Exception {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(tipoMidiaRepository.findByIdTipoMidiaAndAtivoTrue(1L)).thenReturn(Optional.of(tipoMidia));

        PresignedPutObjectRequest mockPresigned = mock(PresignedPutObjectRequest.class);
        when(mockPresigned.url()).thenReturn(new URI("https://s3.amazonaws.com/lumina-bronze/upload-url").toURL());
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresigned);

        when(clienteMidiaRepository.save(any(ClienteMidia.class))).thenAnswer(invocation -> {
            ClienteMidia m = invocation.getArgument(0);
            m.setIdClienteMidia(101L);
            return m;
        });

        UploadArquivoItemRequest arquivo = new UploadArquivoItemRequest("exame-carlos-silva.jpg", "image/jpeg", 1024L * 1024);
        IniciarUploadRequest request = new IniciarUploadRequest(1L, null, "Legenda teste", List.of(arquivo));

        List<UploadPresignedItemResponse> resultado = clienteMidiaService.iniciarUpload(1L, request, usuario);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(101L, resultado.get(0).getMidiaId());
        assertEquals("exame-carlos-silva.jpg", resultado.get(0).getNomeOriginal());
        assertTrue(resultado.get(0).getUploadUrl().contains("upload-url"));
        assertTrue(resultado.get(0).getChaveS3().startsWith("exames/pac-"));
        assertTrue(resultado.get(0).getChaveS3().endsWith(".jpg"));
        assertFalse(resultado.get(0).getChaveS3().contains("carlos-silva"));
        assertFalse(resultado.get(0).getChaveS3().contains("cliente-1"));
        verify(clienteMidiaRepository, times(1)).save(any(ClienteMidia.class));
    }

    @Test
    @DisplayName("Deve rejeitar upload de imagem maior que 15 MB")
    void iniciarUpload_ImagemExcedeLimite() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(tipoMidiaRepository.findByIdTipoMidiaAndAtivoTrue(1L)).thenReturn(Optional.of(tipoMidia));

        UploadArquivoItemRequest arquivo = new UploadArquivoItemRequest("foto-grande.png", "image/png", 16L * 1024 * 1024);
        IniciarUploadRequest request = new IniciarUploadRequest(1L, null, null, List.of(arquivo));

        assertThrows(FormatoArquivoInvalidoException.class, () ->
                clienteMidiaService.iniciarUpload(1L, request, usuario)
        );
        verify(clienteMidiaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar upload de extensão não permitida (.exe)")
    void iniciarUpload_ExtensaoNaoPermitida() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(tipoMidiaRepository.findByIdTipoMidiaAndAtivoTrue(1L)).thenReturn(Optional.of(tipoMidia));

        UploadArquivoItemRequest arquivo = new UploadArquivoItemRequest("malware.exe", "application/octet-stream", 5000L);
        IniciarUploadRequest request = new IniciarUploadRequest(1L, null, null, List.of(arquivo));

        assertThrows(FormatoArquivoInvalidoException.class, () ->
                clienteMidiaService.iniciarUpload(1L, request, usuario)
        );
        verify(clienteMidiaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve confirmar upload alterando status para ACTIVE via headObject")
    void confirmarUpload_ComSucesso() throws Exception {
        when(clienteMidiaRepository.findByIdClienteMidiaAndCliente_IdClienteAndAtivoTrue(101L, 1L))
                .thenReturn(Optional.of(midiaExistente));
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().build());
        when(clienteMidiaRepository.save(any(ClienteMidia.class))).thenReturn(midiaExistente);

        PresignedGetObjectRequest mockPresignedGet = mock(PresignedGetObjectRequest.class);
        when(mockPresignedGet.url()).thenReturn(new URI("https://s3.amazonaws.com/test-bucket/view-url").toURL());
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(mockPresignedGet);

        ClienteMidiaResponse response = clienteMidiaService.confirmarUpload(1L, 101L, usuario);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("ACTIVE", midiaExistente.getStatus());
        verify(s3Client, times(1)).headObject(any(HeadObjectRequest.class));
        verify(clienteMidiaRepository, times(1)).save(midiaExistente);
    }

    @Test
    @DisplayName("Deve lançar erro 400 se o arquivo não estiver presente no S3 ao confirmar")
    void confirmarUpload_ArquivoNaoEncontradoNoS3() {
        when(clienteMidiaRepository.findByIdClienteMidiaAndCliente_IdClienteAndAtivoTrue(101L, 1L))
                .thenReturn(Optional.of(midiaExistente));
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().message("Object not found").build());

        assertThrows(ResponseStatusException.class, () ->
                clienteMidiaService.confirmarUpload(1L, 101L, usuario)
        );
        assertEquals("ERROR", midiaExistente.getStatus());
        verify(clienteMidiaRepository, times(1)).save(midiaExistente);
    }

    @Test
    @DisplayName("Deve executar soft delete na mídia preservando registro no banco")
    void excluirMidia_SoftDelete() {
        when(clienteMidiaRepository.findByIdClienteMidiaAndCliente_IdClienteAndAtivoTrue(101L, 1L))
                .thenReturn(Optional.of(midiaExistente));

        clienteMidiaService.excluirMidia(1L, 101L, usuario);

        assertFalse(midiaExistente.getAtivo());
        assertNotNull(midiaExistente.getDataExclusao());
        assertEquals(usuario, midiaExistente.getUsuarioExclusao());
        verify(clienteMidiaRepository, times(1)).save(midiaExistente);
    }

    @Test
    @DisplayName("Deve listar mídias ativas e gerar URLs pré-assinadas GET")
    void listarMidias_GeraPresignedUrls() throws Exception {
        when(clienteRepository.existsById(1L)).thenReturn(true);
        midiaExistente.setStatus("ACTIVE");
        when(clienteMidiaRepository.findMidiasAtivasComFiltros(eq(1L), any(), any(), any(), any(), any()))
                .thenReturn(List.of(midiaExistente));

        PresignedGetObjectRequest mockPresignedGet = mock(PresignedGetObjectRequest.class);
        when(mockPresignedGet.url()).thenReturn(new URI("https://s3.amazonaws.com/test-bucket/view-url").toURL());
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(mockPresignedGet);

        List<ClienteMidiaResponse> resultado = clienteMidiaService.listarMidias(1L, null, null, null, null, usuario);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(101L, resultado.get(0).getId());
        assertEquals("https://s3.amazonaws.com/test-bucket/view-url", resultado.get(0).getUrlVisualizacao());
        assertEquals("lumina-bronze", resultado.get(0).getBucket());
        assertEquals("exames/pac-9f86d081884c7d659a2feaa0/uuid.jpg", resultado.get(0).getChaveS3());
    }

    @Test
    @DisplayName("Deve lançar EntidadeNaoEncontrada quando paciente não existir ao listar")
    void listarMidias_PacienteNaoEncontrado() {
        when(clienteRepository.existsById(999L)).thenReturn(false);

        assertThrows(EntidadeNaoEncontrada.class, () ->
                clienteMidiaService.listarMidias(999L, null, null, null, null, usuario)
        );
    }
}

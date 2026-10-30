package com.lumina.backend.controller;

import com.lumina.backend.dto.midia.ClienteMidiaResponse;
import com.lumina.backend.dto.midia.IniciarUploadRequest;
import com.lumina.backend.dto.midia.UploadPresignedItemResponse;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.service.midia.ClienteMidiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/clientes/{clienteId}/midias")
@PreAuthorize("hasAnyRole('ADMIN', 'DENTISTA', 'RECEPCIONISTA')")
@Tag(name = "Mídias Clínicas", description = "Endpoints para upload, visualização e soft delete de fotos, exames e laudos de pacientes no Data Lake Bronze")
public class ClienteMidiaController {

    private final ClienteMidiaService clienteMidiaService;
    private final UsuarioRepository usuarioRepository;

    public ClienteMidiaController(ClienteMidiaService clienteMidiaService, UsuarioRepository usuarioRepository) {
        this.clienteMidiaService = clienteMidiaService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/iniciar-upload")
    @Operation(summary = "Inicia upload em lote de mídias", description = "Registra os metadados dos arquivos com status PENDING e retorna Pre-signed PUT URLs para streaming direto ao S3.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Upload iniciado e URLs pré-assinadas geradas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Tamanho ou formato de arquivo inválido"),
            @ApiResponse(responseCode = "404", description = "Paciente ou tipo de mídia não encontrado")
    })
    public ResponseEntity<List<UploadPresignedItemResponse>> iniciarUpload(
            @Parameter(description = "ID do paciente", example = "1") @PathVariable Long clienteId,
            @org.springframework.web.bind.annotation.RequestBody @Valid IniciarUploadRequest request) {

        Usuario usuario = getUsuarioAutenticado();
        List<UploadPresignedItemResponse> urls = clienteMidiaService.iniciarUpload(clienteId, request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(urls);
    }

    @PutMapping(value = "/{midiaId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Envia o arquivo de exame diretamente para o bucket bronze", description = "Realiza o upload do arquivo binário para a pasta exames/cliente-{id} no bucket bronze e ativa o registro.")
    public ResponseEntity<ClienteMidiaResponse> enviarArquivoBucket(
            @Parameter(description = "ID do paciente", example = "1") @PathVariable Long clienteId,
            @Parameter(description = "ID da mídia", example = "101") @PathVariable Long midiaId,
            @RequestParam("file") MultipartFile file) {

        Usuario usuario = getUsuarioAutenticado();
        ClienteMidiaResponse response = clienteMidiaService.enviarArquivoBucket(clienteId, midiaId, file, usuario);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{midiaId}/confirmar")
    @Operation(summary = "Confirma a conclusão do upload no S3", description = "Verifica a integridade do objeto no S3 via headObject e altera o status para ACTIVE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mídia ativada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Objeto não localizado no S3"),
            @ApiResponse(responseCode = "404", description = "Mídia não encontrada")
    })
    public ResponseEntity<ClienteMidiaResponse> confirmarUpload(
            @Parameter(description = "ID do paciente", example = "1") @PathVariable Long clienteId,
            @Parameter(description = "ID da mídia", example = "101") @PathVariable Long midiaId) {

        Usuario usuario = getUsuarioAutenticado();
        ClienteMidiaResponse response = clienteMidiaService.confirmarUpload(clienteId, midiaId, usuario);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<List<ClienteMidiaResponse>> listarMidias(
            Long clienteId,
            Long tipoId,
            Long consultaId,
            Long dentistaId,
            String data) {
        return listarMidias(clienteId, tipoId, consultaId, dentistaId, data, null);
    }

    @GetMapping
    @Operation(summary = "Lista exames e mídias clínicas ativas do paciente no bucket bronze", description = "Retorna a galeria de exames e mídias ativas do cliente (exames/cliente-{id}/) com URLs pré-assinadas de visualização temporária (TTL 30 min).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mídias retornadas com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhuma mídia localizada para os filtros informados"),
            @ApiResponse(responseCode = "404", description = "Paciente não encontrado")
    })
    public ResponseEntity<List<ClienteMidiaResponse>> listarMidias(
            @Parameter(description = "ID do paciente", example = "1") @PathVariable Long clienteId,
            @Parameter(description = "Filtro por ID do tipo de mídia") @RequestParam(required = false) Long tipoId,
            @Parameter(description = "Filtro por ID da consulta associada") @RequestParam(required = false) Long consultaId,
            @Parameter(description = "Filtro por ID do dentista responsável") @RequestParam(required = false) Long dentistaId,
            @Parameter(description = "Filtro por data no formato YYYY-MM-DD") @RequestParam(required = false) String data,
            @Parameter(description = "Busca textual por nome do exame ou legenda") @RequestParam(required = false) String busca) {

        Usuario usuario = getUsuarioAutenticado();
        List<ClienteMidiaResponse> midias;
        if (busca == null || busca.isBlank()) {
            midias = clienteMidiaService.listarMidias(clienteId, tipoId, consultaId, dentistaId, data, usuario);
        } else {
            midias = clienteMidiaService.listarMidias(clienteId, tipoId, consultaId, dentistaId, data, busca, usuario);
        }
        if (midias.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(midias);
    }

    @GetMapping("/bucket")
    @Operation(summary = "Busca e sincroniza os exames do cliente diretamente da pasta pseudonimizada no bucket bronze", description = "Consulta o prefixo s3://lumina-bronze/exames/pac-{hash}/ e retorna os exames disponíveis.")
    public ResponseEntity<List<ClienteMidiaResponse>> buscarExamesNoBucket(
            @Parameter(description = "ID do paciente", example = "1") @PathVariable Long clienteId) {

        Usuario usuario = getUsuarioAutenticado();
        List<ClienteMidiaResponse> midias = clienteMidiaService.buscarExamesNoBucket(clienteId, usuario);
        if (midias.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(midias);
    }

    @GetMapping("/destino-bucket")
    @Operation(summary = "Retorna o caminho pseudonimizado (LGPD) da pasta de exames do cliente no bucket bronze")
    public ResponseEntity< java.util.Map<String, String>> obterDestinoBucket(
            @Parameter(description = "ID do paciente", example = "1") @PathVariable Long clienteId) {
        return ResponseEntity.ok(clienteMidiaService.obterDestinoBucketCliente(clienteId));
    }

    @DeleteMapping("/{midiaId}")
    @Operation(summary = "Executa soft delete na mídia", description = "Desativa a mídia no prontuário sem remover o arquivo físico do S3 para fins de conformidade legal e auditoria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Mídia desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Mídia não encontrada")
    })
    public ResponseEntity<Void> excluirMidia(
            @Parameter(description = "ID do paciente", example = "1") @PathVariable Long clienteId,
            @Parameter(description = "ID da mídia", example = "101") @PathVariable Long midiaId) {

        Usuario usuario = getUsuarioAutenticado();
        clienteMidiaService.excluirMidia(clienteId, midiaId, usuario);
        return ResponseEntity.noContent().build();
    }

    private Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return usuarioRepository.findByEmail(auth.getName()).orElse(null);
        }
        return null;
    }
}

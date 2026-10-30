package com.lumina.backend.controller;

import com.lumina.backend.domain.cliente.Cliente;
import com.lumina.backend.dto.anamnese.AnamneseMapper;
import com.lumina.backend.dto.anamnese.AnamneseRequest;
import com.lumina.backend.dto.anamnese.AnamneseResponse;
import com.lumina.backend.dto.cliente.ClienteMapper;
import com.lumina.backend.dto.cliente.ClienteRequest;
import com.lumina.backend.dto.cliente.ClienteResponse;
import com.lumina.backend.dto.convenio.ConvenioMapper;
import com.lumina.backend.dto.convenio.ConvenioResponse;
import com.lumina.backend.dto.estado_civil.EstadoCivilMapper;
import com.lumina.backend.dto.estado_civil.EstadoCivilResponse;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.Convenio;
import com.lumina.backend.model.EstadoCivil;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.service.cliente.ClienteService;
import com.lumina.backend.service.convenio.ConvenioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/clientes")
@PreAuthorize("hasAnyRole('ADMIN', 'DENTISTA', 'RECEPCIONISTA')")
@Tag(name = "Clientes", description = "Endpoints para cadastro e gestao de clientes")
public class ClienteController {

    private final ClienteService service;
    private final ConvenioService convenioService;
    private final UsuarioRepository usuarioRepository;
    private final com.lumina.backend.repository.ConsultaRepository consultaRepository;

    public ClienteController(ClienteService service, ConvenioService convenioService, UsuarioRepository usuarioRepository, com.lumina.backend.repository.ConsultaRepository consultaRepository){
        this.service = service;
        this.convenioService = convenioService;
        this.usuarioRepository = usuarioRepository;
        this.consultaRepository = consultaRepository;
    }

    @GetMapping("/estado-civil")
    @Operation(summary = "Lista todos os estados civis", description = "Retorna os estados civis cadastrados no sistema para selecao em formularios.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estados civis retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = EstadoCivilResponse.class))),
            @ApiResponse(responseCode = "204", description = "Nenhum estado civil encontrado", content = @Content)
    })
    public ResponseEntity<List<EstadoCivilResponse>> listarEstadosCivis() {
        List<EstadoCivil> estadosCivis = service.listarEstadosCivis();
        if (estadosCivis.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<EstadoCivilResponse> response = EstadoCivilMapper.toDto(estadosCivis);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Lista ou busca clientes com paginacao em offset", description = "Retorna os clientes cadastrados de forma paginada (offset), permitindo filtrar por nome ou CPF e ordenar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagina de clientes retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "204", description = "Nenhum cliente encontrado", content = @Content)
    })
    public ResponseEntity<Page<ClienteResponse>> listar(
            @Parameter(description = "Nome para busca parcial de clientes", example = "Maria")
            @RequestParam(required = false) String nome,
            @Parameter(description = "CPF exato do cliente", example = "12345678901")
            @RequestParam(required = false) String cpf,
            @PageableDefault(page = 0, size = 6, sort = "nome", direction = Sort.Direction.ASC)
            Pageable pageable){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Boolean isRecepcionista = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RECEPCIONISTA") || a.getAuthority().equals("ROLE_ADMIN"));

        if(!isRecepcionista){
            return ResponseEntity.status(403).build();
        }

        Page<Cliente> clientesPage = service.listarComFiltrosPaginado(nome, cpf, pageable);
        if(clientesPage.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        Page<ClienteResponse> response = clientesPage.map(ClienteMapper::toDto);
        enriquecerClientesComConveniosEConsultas(response.getContent());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Cadastra cliente", description = "Cadastra um novo cliente a partir dos dados informados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos para cadastro", content = @Content)
    })
    public ResponseEntity<ClienteResponse> cadastrar(
            @RequestBody(description = "Dados de cadastro do cliente", required = true,
                    content = @Content(schema = @Schema(implementation = ClienteRequest.class)))
            @org.springframework.web.bind.annotation.RequestBody @Valid ClienteRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Boolean isRecepcionista = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RECEPCIONISTA") || a.getAuthority().equals("ROLE_ADMIN"));

        if(!isRecepcionista){
            return ResponseEntity.status(403).build();
        }
        String usuario = (auth != null) ? auth.getName() : "ANONYMOUS";
        Cliente clienteCadastrado = service.cadastrar(request);
        ClienteResponse response = ClienteMapper.toDto(clienteCadastrado);
        log.info("AUDIT: Usuario [{}] cadastrou novo cliente ID [{}] CPF [{}]",
                usuario, response.getIdCliente(), mascararCpfLgpd(clienteCadastrado.getCpf()));
        return ResponseEntity.status(201).body(response);
    }

    private String mascararCpfLgpd(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return "***";
        }
        String digitos = cpf.replaceAll("\\D", "");
        if (digitos.length() != 11) {
            return "***.***.***-**";
        }
        return "***." + digitos.substring(3, 6) + "." + digitos.substring(6, 9) + "-**";
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Busca cliente por ID", description = "Retorna os dados de um cliente pelo identificador informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente nao encontrado", content = @Content)
    })
    public ResponseEntity<ClienteResponse> buscarPorId(
            @Parameter(description = "ID do cliente", example = "1") @PathVariable Long id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Boolean isApenasDentista = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DENTISTA"))
                && auth.getAuthorities()
                .stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_RECEPCIONISTA"));

        Long usuarioLogadoId = obterUsuarioIdAutenticado();

        if (isApenasDentista) {
            boolean temConsulta = consultaRepository.existsByClienteIdClienteAndUsuarioIdUsuario(id, usuarioLogadoId);
            if (!temConsulta) {
                return ResponseEntity.status(403).build();
            }
        }
        String usuario = (auth != null) ? auth.getName() : "ANONYMOUS";
        log.info("AUDIT: Usuario [{}] consultou dados do cliente ID [{}]", usuario, id);
        Cliente cliente = service.buscarPorId(id);
        ClienteResponse response = ClienteMapper.toDto(cliente);
        enriquecerClienteComConveniosEConsulta(response);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id:\\d+}")
    @Operation(summary = "Atualiza cliente", description = "Atualiza os dados de um cliente pelo identificador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos para atualizacao", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente nao encontrado", content = @Content)
    })
    public ResponseEntity<ClienteResponse> atualizar(
            @RequestBody(description = "Dados para atualizacao do cliente", required = true,
                    content = @Content(schema = @Schema(implementation = ClienteRequest.class)))
            @org.springframework.web.bind.annotation.RequestBody @Valid ClienteRequest request,
            @Parameter(description = "ID do cliente", example = "1") @PathVariable Long id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Boolean isRecepcionista = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RECEPCIONISTA") || a.getAuthority().equals("ROLE_ADMIN"));

        if(!isRecepcionista){
            return ResponseEntity.status(403).build();
        }
        String usuario = (auth != null) ? auth.getName() : "ANONYMOUS";
        Cliente cliente = service.atualizar(request, id);
        log.info("AUDIT: Usuario [{}] atualizou dados do cliente ID [{}]", usuario, id);
        ClienteResponse response = ClienteMapper.toDto(cliente);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id:\\d+}/convenios")
    @Operation(summary = "Lista convenios do cliente", description = "Retorna os convenios vinculados ao cliente informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Convenios do cliente retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = ConvenioResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente nao encontrado", content = @Content)
    })
    public ResponseEntity<List<ConvenioResponse>> listarConvenios(
            @Parameter(description = "ID do cliente", example = "1") @PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Boolean isRecepcionista = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RECEPCIONISTA") || a.getAuthority().equals("ROLE_ADMIN"));

        if(!isRecepcionista){
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.status(200).body(ConvenioMapper.toResponse(convenioService.listarConveniosCliente(id)));
    }


    @GetMapping("/{id:\\d+}/anamneses")
    public ResponseEntity<List<AnamneseResponse>> listarAnamnese(@PathVariable Integer id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isDentistaSemVinculoComPaciente(auth, id != null ? id.longValue() : null)) {
            return ResponseEntity.status(403).build();
        }
        String usuario = (auth != null) ? auth.getName() : "ANONYMOUS";
        log.info("AUDIT: Usuario [{}] listou anamneses do cliente ID [{}]", usuario, id);
        List<Anamnese> anamneseList = service.listarAnamnese(id);
        List<AnamneseResponse> response = AnamneseMapper.toDto(anamneseList);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id:\\d+}/anamneses")
    public ResponseEntity<AnamneseResponse> cadastroAnamnese(@PathVariable Long id,
                                                             @org.springframework.web.bind.annotation.RequestBody AnamneseRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isDentistaSemVinculoComPaciente(auth, id)) {
            return ResponseEntity.status(403).build();
        }
        String usuario = (auth != null) ? auth.getName() : "ANONYMOUS";
        Anamnese anamnese = service.cadastrarAnamnese(id, request);
        log.info("AUDIT: Usuario [{}] cadastrou anamnese manual para o cliente ID [{}]", usuario, id);
        AnamneseResponse response = AnamneseMapper.toDto(anamnese);
        return ResponseEntity.ok(response);
    }

    private boolean isDentistaSemVinculoComPaciente(Authentication auth, Long clienteId) {
        if (auth == null || auth.getAuthorities() == null || clienteId == null) {
            return false;
        }
        boolean isApenasDentista = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DENTISTA"))
                && auth.getAuthorities()
                .stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_RECEPCIONISTA"));
        if (!isApenasDentista) {
            return false;
        }
        Long usuarioLogadoId = obterUsuarioIdAutenticado();
        return !consultaRepository.existsByClienteIdClienteAndUsuarioIdUsuario(clienteId, usuarioLogadoId);
    }

    private Long obterUsuarioIdAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isBlank() && !"anonymousUser".equalsIgnoreCase(auth.getName())) {
            return usuarioRepository.findByEmail(auth.getName())
                    .map(Usuario::getIdUsuario)
                    .orElse(null);
        }
        return null;
    }

    private void enriquecerClientesComConveniosEConsultas(List<ClienteResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            return;
        }

        List<Long> ids = responses.stream()
                .map(ClienteResponse::getIdCliente)
                .filter(Objects::nonNull)
                .toList();

        if (ids.isEmpty()) {
            return;
        }

        Map<Long, List<Convenio>> conveniosPorCliente = Collections.emptyMap();
        if (convenioService != null) {
            try {
                conveniosPorCliente = convenioService.listarConveniosPorClientes(ids);
                if (conveniosPorCliente == null) {
                    conveniosPorCliente = Collections.emptyMap();
                }
            } catch (Exception e) {
                log.warn("Erro ao carregar convenios dos clientes em lote: {}", e.getMessage());
            }
        }

        Map<Long, LocalDate> proximaConsultaPorCliente = Collections.emptyMap();
        if (consultaRepository != null) {
            try {
                List<Consulta> consultasFuturas = consultaRepository.findConsultasFuturasPorClientes(ids, LocalDate.now());
                if (consultasFuturas != null) {
                    proximaConsultaPorCliente = consultasFuturas.stream()
                            .filter(c -> c.getCliente() != null && c.getCliente().getIdCliente() != null && c.getData() != null)
                            .collect(Collectors.toMap(
                                    c -> c.getCliente().getIdCliente(),
                                    Consulta::getData,
                                    (primeira, segunda) -> primeira
                            ));
                }
            } catch (Exception e) {
                log.warn("Erro ao carregar proximas consultas dos clientes em lote: {}", e.getMessage());
            }
        }

        for (ClienteResponse resp : responses) {
            List<Convenio> convs = conveniosPorCliente.getOrDefault(resp.getIdCliente(), Collections.emptyList());
            List<String> nomesConvs = convs.stream().map(Convenio::getNome).filter(Objects::nonNull).toList();
            resp.setConvenios(nomesConvs);
            resp.setPlano(nomesConvs.isEmpty() ? "Particular" : nomesConvs.get(0));
            resp.setProximaConsulta(proximaConsultaPorCliente.get(resp.getIdCliente()));
        }
    }

    private void enriquecerClienteComConveniosEConsulta(ClienteResponse response) {
        if (response != null) {
            enriquecerClientesComConveniosEConsultas(List.of(response));
        }
    }
}

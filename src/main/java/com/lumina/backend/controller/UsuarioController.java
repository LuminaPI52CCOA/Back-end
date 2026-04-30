package com.lumina.backend.controller;

import com.lumina.backend.dto.usuario.*;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.service.Usuario.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Endpoints para cadastro e gestao de usuarios do sistema Lumina")
public class UsuarioController {

    // Nome do cookie — definido em um só lugar para evitar typos
    public static final String COOKIE_NOME = "authToken";
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.validity}")
    private long jwtValidity;

    private final UsuarioService service;

    public UsuarioController(UsuarioService service, PasswordEncoder passwordEncoder) {
        this.service = service;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    @Operation(
            summary = "Cadastra um novo usuario",
            description = "Recebe os dados do usuario, valida as informacoes e registra o usuario no sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos para cadastro", content = @Content)
    })
    public ResponseEntity<UsuarioResponse> cadastrarUsuarios(
            @RequestBody(description = "Dados de cadastro do usuario", required = true,
                    content = @Content(schema = @Schema(implementation = UsuarioRequest.class)))
            @org.springframework.web.bind.annotation.RequestBody @Valid UsuarioRequest usuario) {

        String senhaNova = passwordEncoder.encode(usuario.getSenha());
        Usuario entidade = UsuarioMapper.toEntity(usuario);
        entidade.setSenha(senhaNova);

        Usuario usuariosCadastrados = service.salvar(entidade);

        return ResponseEntity
                .status(201)
                .body(UsuarioMapper.toDto(usuariosCadastrados));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Autentica usuario",
            description = "Valida credenciais, cria sessao com cookie HttpOnly e retorna dados basicos da sessao."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioSessaoDto.class))),
            @ApiResponse(responseCode = "400", description = "Credenciais invalidas", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuario nao autenticado", content = @Content)
    })
    public ResponseEntity<UsuarioSessaoDto> login(
            @RequestBody(description = "Credenciais de autenticacao", required = true,
                    content = @Content(schema = @Schema(implementation = UsuarioLoginDto.class)))
            @org.springframework.web.bind.annotation.RequestBody UsuarioLoginDto usuarioLoginDto,
            HttpServletResponse response) {

        final Usuario usuario = UsuarioMapper.of(usuarioLoginDto);

        // autenticar() gera o token internamente — precisamos dele apenas para o cookie
        UsuarioTokenDto autenticado = this.service.autenticar(usuario);

        // Token vai para o cookie HttpOnly — inacessível ao JavaScript (proteção XSS)
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NOME, autenticado.getToken())
                .httpOnly(true)                          // inacessível ao JavaScript
                .secure(false)                           // true em produção (exige HTTPS)
                .sameSite("Strict")                      // bloqueia envio cross-site (mitiga CSRF)
                .path("/")                               // valido para toda a aplicacao
                .maxAge(Duration.ofSeconds(jwtValidity)) // expira junto com o token JWT
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Body retorna apenas dados de sessão — sem o token
        UsuarioSessaoDto sessao = UsuarioMapper.ofSessao(autenticado);
        System.out.println(autenticado.getToken());
        return ResponseEntity.ok(sessao);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Encerra sessao do usuario",
            description = "Remove o cookie de autenticacao atual e finaliza a sessao no cliente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso", content = @Content)
    })
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NOME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)  // maxAge=0 instrui o browser a deletar o cookie imediatamente
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
            summary = "Lista todos os usuarios",
            description = "Retorna a lista de usuarios cadastrados. Caso nao existam registros, retorna 204."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "204", description = "Nao ha usuarios cadastrados", content = @Content)
    })
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios(){
        List<Usuario> usuarios =service.listar();
        if(usuarios.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(200).body(UsuarioMapper.toDto(usuarios));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busca usuario por ID",
            description = "Retorna os dados do usuario correspondente ao identificador informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content)
    })
    public ResponseEntity<Optional<Usuario>> buscarPorId(
            @Parameter(description = "ID do usuario", example = "1") @PathVariable Long id){
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Inativa usuario por ID",
            description = "Realiza a inativacao logica do usuario com base no ID informado e retorna 204."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario inativado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content)
    })
    public ResponseEntity<Void> deletarUsuario(
            @Parameter(description = "ID do usuario", example = "1") @PathVariable Long id){
        Boolean ativo = false;
        service.deletar(ativo, id);
    return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualiza usuario por ID",
            description = "Atualiza os dados do usuario conforme o corpo da requisicao e retorna o registro atualizado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos para atualizacao", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content)
    })
    public ResponseEntity<Optional<Usuario>> atualizar(
            @RequestBody(description = "Dados para atualizacao do usuario", required = true,
                    content = @Content(schema = @Schema(implementation = UsuarioRequest.class)))
            @Valid @org.springframework.web.bind.annotation.RequestBody UsuarioRequest usuario,
            @Parameter(description = "ID do usuario", example = "1") @PathVariable Long id){
        service.atualizar(usuario, id);
        return ResponseEntity.ok(service.buscarPorId(id));
    }
}

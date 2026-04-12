package com.lumina.backend.controller;

import com.lumina.backend.dto.usuario.*;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.service.Usuario.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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

    @Value("${jwt.validity}")
    private long jwtValidity;

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
            summary = "Cadastra um novo usuario",
            description = "Recebe os dados do usuario, valida as informacoes e registra o usuario no sistema."
    )
    public ResponseEntity<UsuarioResponse> cadastrarUsuarios(
            @RequestBody @Valid UsuarioRequest usuario) {

        Usuario entidade = UsuarioMapper.toEntity(usuario);

        Usuario usuariosCadastrados = service.salvar(entidade);

        return ResponseEntity
                .status(201)
                .body(UsuarioMapper.toDto(usuariosCadastrados));
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioSessaoDto> login(
            @RequestBody UsuarioLoginDto usuarioLoginDto,
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
        return ResponseEntity.ok(sessao);
    }
    
    @PostMapping("/logout")
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
    public ResponseEntity<Optional<Usuario>> buscarPorId(@PathVariable Integer id){
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Inativa usuario por ID",
            description = "Realiza a inativacao logica do usuario com base no ID informado e retorna 204."
    )
    public ResponseEntity<Void> deletarUsuario(@PathVariable Integer id){
        Boolean ativo = false;
        service.deletar(ativo, id);
    return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualiza usuario por ID",
            description = "Atualiza os dados do usuario conforme o corpo da requisicao e retorna o registro atualizado."
    )
    public ResponseEntity<Optional<Usuario>> atualizar(
            @Valid @RequestBody UsuarioRequest usuario, @PathVariable Integer id){
        service.atualizar(usuario, id);
        return ResponseEntity.ok(service.buscarPorId(id));
    }
}

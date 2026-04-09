package com.lumina.backend.controller;

import com.lumina.backend.dto.usuario.UsuarioMapper;
import com.lumina.backend.dto.usuario.UsuarioRequest;
import com.lumina.backend.dto.usuario.UsuarioResponse;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.service.Usuario.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Endpoints para cadastro e gestao de usuarios do sistema Lumina")
public class UsuarioController {

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

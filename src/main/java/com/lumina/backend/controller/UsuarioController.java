package com.lumina.backend.controller;

import com.lumina.backend.dto.usuario.UsuarioMapper;
import com.lumina.backend.dto.usuario.UsuarioRequest;
import com.lumina.backend.dto.usuario.UsuarioResponse;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrarUsuarios(
            @RequestBody @Valid UsuarioRequest usuario) {

        Usuario entidade = UsuarioMapper.toEntity(usuario);

        Usuario usuariosCadastrados = service.salvar(entidade);

        return ResponseEntity
                .status(201)
                .body(UsuarioMapper.toDto(usuariosCadastrados));
    }
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios(){
        List<Usuario> usuarios =service.listar();
        if(usuarios.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(200).body(UsuarioMapper.toDto(usuarios));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Usuario>> buscarPorId(@PathVariable Integer id){
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Integer id){
        Boolean ativo = false;
        service.deletar(ativo, id);
    return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Optional<Usuario>> atualizar(
            @Valid @RequestBody UsuarioRequest usuario, @PathVariable Integer id){
        service.atualizar(usuario, id);
        return ResponseEntity.ok(service.buscarPorId(id));
    }
}

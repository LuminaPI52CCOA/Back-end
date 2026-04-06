package com.lumina.backend.service;

import com.lumina.backend.dto.usuario.UsuarioRequest;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Usuario salvar(Usuario usuario){
        return repository.save(usuario);
    }

    public List<Usuario> listar(){
        return repository.findAll();
    }
    public Optional<Usuario> buscarPorId(Integer id){
        return Optional.of(repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontrada(
                        "Usuario de id: %d não encontrado".formatted(id))));
    }

    public int deletar(Boolean ativo, Integer id){
        Optional<Usuario> usuario = repository.findById(id);
        if(usuario.isEmpty()){
            throw new EntidadeNaoEncontrada("Usuario de id: %d não encontrado".formatted(id));
        }
        return repository.logicalDelete(ativo, id);
    }
    public int atualizar(UsuarioRequest usuarios, Integer id){

        Optional<Usuario> usuario = repository.findById(id);
        if(usuario.isEmpty()){
            throw new EntidadeNaoEncontrada("Usuario de id: %d não encontrado".formatted(id));
        }

        return repository.atualizarPeloId(usuarios, id);
    }
}

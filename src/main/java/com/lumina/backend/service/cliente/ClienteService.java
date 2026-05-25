package com.lumina.backend.service.cliente;

import com.lumina.backend.dto.cliente.ClienteRequest;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public List<Cliente> listar(){
        return repository.findAll();
    }

    public Cliente cadastrar(ClienteRequest request){
        Cliente cliente = new Cliente();
        cliente.setIdCliente(request.getIdCliente());
        cliente.setNome(request.getNome());
        cliente.setCpf(request.getCpf());
        cliente.setRg(request.getRg());
        cliente.setDataNascimento(request.getDataNascimento());
        cliente.setNumeroCelular(request.getNumeroCelular());
        cliente.setEmail(request.getEmail());
        cliente.setSexo(request.getSexo());
        cliente.setNaturalidade(request.getNaturalidade());
        cliente.setNacionalidade(request.getNacionalidade());
        cliente.setFkEstadoCivil(request.getFkEstadoCivil());
        cliente.setEnderecoResidencial(request.getEnderecoResidencial());
        cliente.setCep(request.getCep());
        cliente.setFkClienteIndicacao(request.getFkClienteIndicacao());
        cliente.setFkResponsavel(request.getFkResponsavel());
        cliente.setGrauParentescoResponsavel(request.getGrauParentescoResponsavel());
        return repository.save(cliente);
    }

    public Cliente buscarPorId(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Cliente não encontrado!"));
    }

    public Cliente atualizar(ClienteRequest request, Long id){
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Cliente não encontrado!"));
        cliente.setIdCliente(request.getIdCliente());
        cliente.setNome(request.getNome());
        cliente.setCpf(request.getCpf());
        cliente.setRg(request.getRg());
        cliente.setDataNascimento(request.getDataNascimento());
        cliente.setNumeroCelular(request.getNumeroCelular());
        cliente.setEmail(request.getEmail());
        cliente.setSexo(request.getSexo());
        cliente.setNaturalidade(request.getNaturalidade());
        cliente.setNacionalidade(request.getNacionalidade());
        cliente.setFkEstadoCivil(request.getFkEstadoCivil());
        cliente.setEnderecoResidencial(request.getEnderecoResidencial());
        cliente.setCep(request.getCep());
        cliente.setFkClienteIndicacao(request.getFkClienteIndicacao());
        cliente.setFkResponsavel(request.getFkResponsavel());
        cliente.setGrauParentescoResponsavel(request.getGrauParentescoResponsavel());
        return repository.save(cliente);
    }


}

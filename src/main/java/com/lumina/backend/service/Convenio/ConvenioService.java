package com.lumina.backend.service.Convenio;

import com.lumina.backend.dto.convenio.ConvenioMapper;
import com.lumina.backend.dto.convenio.ConvenioRequest;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Convenio;
import com.lumina.backend.repository.ClienteConvenioRepository;
import com.lumina.backend.repository.ClienteRepository;
import com.lumina.backend.repository.ConvenioRepository;

import java.util.List;

public class ConvenioService {

    private final ConvenioRepository convenioRepository;
    private final ClienteConvenioRepository clienteConvenioRepository;
    private final ClienteRepository clienteRepository;

    public ConvenioService(ConvenioRepository convenioRepository,
                           ClienteConvenioRepository clienteConvenioRepository,
                           ClienteRepository clienteRepository) {
        this.convenioRepository = convenioRepository;
        this.clienteConvenioRepository = clienteConvenioRepository;
        this.clienteRepository = clienteRepository;
    }

    public List<Convenio> listar() {
        return convenioRepository.findAll();
    }

    public Convenio cadastrar(ConvenioRequest convenioRequest) {

        Convenio convenioEntidade = ConvenioMapper.toEntity(convenioRequest);

        return convenioRepository.save(convenioEntidade);
    }

    public Convenio atualizar(Long id, ConvenioRequest convenioRequest) {
        Convenio convenioEncontrado = convenioRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Convênio não encontrado!"));

        convenioEncontrado.setNome(convenioRequest.getNome());

        return convenioRepository.save(convenioEncontrado);
    }


    public void deletar(Long id) {
        Convenio convenioEncontrado = convenioRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Convênio não encontrado!"));

        convenioRepository.delete(convenioEncontrado);
    }

    public void listarConvenios(Integer id) {
        if(!clienteRepository.existsById(id)) {
            throw new EntidadeNaoEncontrada("Cliente não encontrado!");
        }

        return clienteConvenioRepository.findByClienteIdCliente(id);
    }
}

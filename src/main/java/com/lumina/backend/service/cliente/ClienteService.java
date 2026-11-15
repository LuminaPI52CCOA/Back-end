package com.lumina.backend.service.cliente;

import com.lumina.backend.dto.anamnese.AnamneseMapper;
import com.lumina.backend.dto.anamnese.AnamneseRequest;
import com.lumina.backend.dto.cliente.ClienteMapper;
import com.lumina.backend.dto.cliente.ClienteRequest;
import com.lumina.backend.dto.cliente.ResponsavelRequest;
import com.lumina.backend.exception.CpfDuplicadoException;
import com.lumina.backend.exception.EmailDuplicadoException;
import com.lumina.backend.exception.EntidadeNaoEncontradaException;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.EstadoCivil;
import com.lumina.backend.repository.AnamneseRepository;
import com.lumina.backend.repository.ClienteRepository;
import com.lumina.backend.repository.EstadoCivilRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final AnamneseRepository anamneseRepository;
    private final EstadoCivilRepository estadoCivilRepository;

    public ClienteService(ClienteRepository clienteRepository,
                          AnamneseRepository anamneseRepository,
                          EstadoCivilRepository estadoCivilRepository) {
        this.clienteRepository = clienteRepository;
        this.anamneseRepository = anamneseRepository;
        this.estadoCivilRepository = estadoCivilRepository;
    }

    public List<EstadoCivil> listarEstadosCivis() {
        return estadoCivilRepository.findAll();
    }

    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    public Page<Cliente> listarPaginado(Pageable pageable) {
        return clienteRepository.findAll(paginacaoOrdenadaPorNome(pageable));
    }

    public List<Cliente> listarComFiltros(String nome, String cpf) {
        if (cpf != null && !cpf.isBlank()) {
            return clienteRepository.findByCpf(cpf)
                    .map(List::of)
                    .orElse(Collections.emptyList());
        }
        if (nome != null && !nome.isBlank()) {
            return clienteRepository.findByNomeContainingIgnoreCase(nome);
        }
        return clienteRepository.findAll();
    }

    public Page<Cliente> listarComFiltrosPaginado(String nome, String cpf, Pageable pageable) {
        PageRequest pageRequest = paginacaoOrdenadaPorNome(pageable);

        if (cpf != null && !cpf.isBlank()) {
            List<Cliente> encontrados = clienteRepository.findByCpf(cpf)
                    .map(List::of)
                    .orElse(Collections.emptyList());
            return new PageImpl<>(encontrados, pageRequest, encontrados.size());
        }
        if (nome != null && !nome.isBlank()) {
            return clienteRepository.findByNomeContainingIgnoreCase(nome, pageRequest);
        }
        return clienteRepository.findAll(pageRequest);
    }

    public Page<Cliente> listarPorDentistaPaginado(Long idUsuario, String nome, String cpf, Pageable pageable) {
        return clienteRepository.buscarDoDentista(
                idUsuario,
                (nome == null || nome.isBlank()) ? null : nome,
                (cpf == null || cpf.isBlank()) ? null : cpf,
                paginacaoOrdenadaPorNome(pageable)
        );
    }

    public Cliente cadastrar(ClienteRequest request) {
        if (clienteRepository.existsByEmail(request.email())) {
            throw new EmailDuplicadoException("Email " + request.email() + " já existe");
        }
        if (clienteRepository.existsByCpf(request.cpf())) {
            throw new CpfDuplicadoException("Cpf " + request.cpf() + " já existe");
        }

        Cliente cliente = new Cliente();
        ClienteMapper.aplicarDados(cliente, request);
        preencherRelacoes(cliente, request);
        return clienteRepository.save(cliente);
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente não encontrado!"));
    }

    public Cliente atualizar(ClienteRequest request, Long id) {
        Cliente clienteExistente = buscarPorId(id);
        ClienteMapper.aplicarDados(clienteExistente, request);
        preencherRelacoes(clienteExistente, request);
        return clienteRepository.save(clienteExistente);
    }

    public List<Anamnese> listarAnamnese(Integer id) {
        List<Anamnese> anamnese = anamneseRepository.findAnamneseByFkCliente_IdCliente(id);
        if (anamnese.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Anamnese não encontrada!");
        }
        return anamnese;
    }

    public Anamnese cadastrarAnamnese(Long id, AnamneseRequest request) {
        Cliente cliente = buscarPorId(id);

        Anamnese anamnese = new Anamnese();
        AnamneseMapper.aplicarDados(anamnese, request);
        anamnese.setFkCliente(cliente);
        if (anamnese.getDataAnamnese() == null) {
            anamnese.setDataAnamnese(LocalDate.now());
        }

        anamneseRepository.save(anamnese);
        return anamnese;
    }

    private void preencherRelacoes(Cliente cliente, ClienteRequest request) {
        cliente.setClienteIndicacao(resolverIndicacao(request.fkClienteIndicacao()));
        cliente.setResponsavel(resolverResponsavel(request.responsavel()));
    }

    private Cliente resolverIndicacao(Integer fkClienteIndicacao) {
        if (fkClienteIndicacao == null) {
            return null;
        }
        return clienteRepository.findById(fkClienteIndicacao.longValue())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente indicador não encontrado!"));
    }

    private Cliente resolverResponsavel(ResponsavelRequest respDto) {
        if (respDto == null) {
            return null;
        }
        if (respDto.idCliente() != null) {
            return clienteRepository.findById(respDto.idCliente())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Responsável não encontrado!"));
        }
        if (respDto.cpf() != null && !respDto.cpf().isBlank()) {
            return clienteRepository.findByCpf(respDto.cpf())
                    .orElseGet(() -> clienteRepository.save(criarResponsavelEntidade(respDto)));
        }
        return null;
    }

    private Cliente criarResponsavelEntidade(ResponsavelRequest respDto) {
        Cliente novoResp = new Cliente();
        novoResp.setNome(respDto.nome());
        novoResp.setCpf(respDto.cpf());
        novoResp.setRg(respDto.rg());
        novoResp.setDataNascimento(respDto.dataNascimento());
        novoResp.setNaturalidade(respDto.naturalidade());
        novoResp.setNacionalidade(respDto.nacionalidade());
        novoResp.setSexo(respDto.sexo());
        novoResp.setCep(respDto.cep());
        novoResp.setEnderecoResidencial(respDto.enderecoResidencial());
        novoResp.setEmail(respDto.email());
        novoResp.setNumeroCelular(respDto.numeroCelular());
        novoResp.setFkEstadoCivil(respDto.fkEstadoCivil());
        return novoResp;
    }

    private static PageRequest paginacaoOrdenadaPorNome(Pageable pageable) {
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.ASC, "nome");
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}

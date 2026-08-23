package com.lumina.backend.service.cliente;

import com.lumina.backend.dto.anamnese.AnamneseMapper;
import com.lumina.backend.dto.anamnese.AnamneseRequest;
import com.lumina.backend.dto.cliente.ClienteMapper;
import com.lumina.backend.dto.cliente.ClienteRequest;
import com.lumina.backend.dto.cliente.ResponsavelRequest;
import com.lumina.backend.exception.CpfDuplicadoException;
import com.lumina.backend.exception.EmailDuplicadoException;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.EstadoCivil;
import com.lumina.backend.repository.AnamneseRepository;
import com.lumina.backend.repository.ClienteRepository;
import com.lumina.backend.repository.EstadoCivilRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final AnamneseRepository anamneseRepository;
    private final EstadoCivilRepository estadoCivilRepository;

    public ClienteService(ClienteRepository repository,
                          AnamneseRepository anamneseRepository,
                          EstadoCivilRepository estadoCivilRepository) {
        this.repository = repository;
        this.anamneseRepository = anamneseRepository;
        this.estadoCivilRepository = estadoCivilRepository;
    }

    public List<EstadoCivil> listarEstadosCivis() {
        return estadoCivilRepository.findAll();
    }

    public List<Cliente> listar(){
        return repository.findAll();
    }

    public List<Cliente> listarComFiltros(String nome, String cpf) {
        if (cpf != null && !cpf.isBlank()) {
            return repository.findByCpf(cpf)
                    .map(List::of)
                    .orElse(Collections.emptyList());
        }
        if (nome != null && !nome.isBlank()) {
            return repository.findByNomeContainingIgnoreCase(nome);
        }
        return repository.findAll();
    }

    public Cliente cadastrar(ClienteRequest request){
        if((repository.findByEmail(request.getEmail()).isPresent())){
            throw new EmailDuplicadoException("Email " + request.getEmail() + " já existe");
        }
        if((repository.findByCpf(request.getCpf()).isPresent())){
            throw new CpfDuplicadoException("Cpf " + request.getCpf() + " já existe");
        }

        Cliente cliente = ClienteMapper.toEntity(request);

        if (request.getFkClienteIndicacao() != null) {
            Cliente indicacao = repository.findById(request.getFkClienteIndicacao().longValue())
                    .orElseThrow(() -> new EntidadeNaoEncontrada("Cliente indicador não encontrado!"));
            cliente.setClienteIndicacao(indicacao);
        }

        if (request.getResponsavel() != null) {
            ResponsavelRequest respDto = request.getResponsavel();
            Cliente responsavel = null;

            if (respDto.getIdCliente() != null) {
                responsavel = repository.findById(respDto.getIdCliente())
                        .orElseThrow(() -> new EntidadeNaoEncontrada("Responsável não encontrado!"));
            } else if (respDto.getCpf() != null && !respDto.getCpf().isBlank()) {
                responsavel = repository.findByCpf(respDto.getCpf())
                        .orElseGet(() -> {
                            Cliente novoResp = new Cliente();
                            novoResp.setNome(respDto.getNome());
                            novoResp.setCpf(respDto.getCpf());
                            novoResp.setRg(respDto.getRg());
                            novoResp.setNumeroCelular(respDto.getNumeroCelular());
                            novoResp.setEmail(respDto.getEmail());
                            novoResp.setSexo(respDto.getSexo());
                            novoResp.setDataNascimento(respDto.getDataNascimento());
                            novoResp.setEnderecoResidencial(respDto.getEnderecoResidencial());
                            novoResp.setCep(respDto.getCep());
                            novoResp.setNaturalidade(respDto.getNaturalidade());
                            novoResp.setNacionalidade(respDto.getNacionalidade());
                            novoResp.setFkEstadoCivil(respDto.getFkEstadoCivil());
                            return repository.save(novoResp);
                        });
            }
            cliente.setResponsavel(responsavel);
        }

        return repository.save(cliente);
    }

    public Cliente buscarPorId(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Cliente não encontrado!"));
    }

    public Cliente atualizar(ClienteRequest request, Long id){
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Cliente não encontrado!"));

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
        cliente.setGrauParentescoResponsavel(request.getGrauParentescoResponsavel());

        if (request.getFkClienteIndicacao() != null) {
            Cliente indicacao = repository.findById(request.getFkClienteIndicacao().longValue())
                    .orElseThrow(() -> new EntidadeNaoEncontrada("Cliente indicador não encontrado!"));
            cliente.setClienteIndicacao(indicacao);
        } else {
            cliente.setClienteIndicacao(null);
        }

        if (request.getResponsavel() != null) {
            ResponsavelRequest respDto = request.getResponsavel();
            Cliente responsavel = null;

            if (respDto.getIdCliente() != null) {
                responsavel = repository.findById(respDto.getIdCliente())
                        .orElseThrow(() -> new EntidadeNaoEncontrada("Responsável não encontrado!"));
            } else if (respDto.getCpf() != null && !respDto.getCpf().isBlank()) {
                responsavel = repository.findByCpf(respDto.getCpf())
                        .orElseGet(() -> {
                            Cliente novoResp = new Cliente();
                            novoResp.setNome(respDto.getNome());
                            novoResp.setCpf(respDto.getCpf());
                            novoResp.setRg(respDto.getRg());
                            novoResp.setNumeroCelular(respDto.getNumeroCelular());
                            novoResp.setEmail(respDto.getEmail());
                            novoResp.setSexo(respDto.getSexo());
                            novoResp.setDataNascimento(respDto.getDataNascimento());
                            novoResp.setEnderecoResidencial(respDto.getEnderecoResidencial());
                            novoResp.setCep(respDto.getCep());
                            novoResp.setNaturalidade(respDto.getNaturalidade());
                            novoResp.setNacionalidade(respDto.getNacionalidade());
                            novoResp.setFkEstadoCivil(respDto.getFkEstadoCivil());
                            return repository.save(novoResp);
                        });
            }
            cliente.setResponsavel(responsavel);
        } else {
            cliente.setResponsavel(null);
        }

        return repository.save(cliente);
    }

    public List<Anamnese> listarAnamnese(Integer id){
        List<Anamnese> anamnese = anamneseRepository.findAnamneseByFkCliente_IdCliente(id);
        if(anamnese.isEmpty()) throw new EntidadeNaoEncontrada("Anamnese não encontrada!") ;
        return anamnese;
    }

    public Anamnese cadastrarAnamnese(Long id, AnamneseRequest request){
        repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Cliente não encontrado!"));
        Anamnese anamnese = new Anamnese();
        anamnese.setIdAnamnese(request.getIdAnamnese());
        anamnese.setDataAnamnese(request.getDataAnamnese());
        anamnese.setFazendoTratamento(request.getFazendoTratamento());
        anamnese.setDescricaoTratamento(request.getDescricaoTratamento());
        anamnese.setDoresCabecaFaceAtm(request.getDoresCabecaFaceAtm());
        anamnese.setAlergiaMedicamentos(request.getAlergiaMedicamentos());
        anamnese.setDescricaoAlergiaMedicamentos(request.getDescricaoAlergiaMedicamentos());
        anamnese.setReacaoAnestesiaLocal(request.getReacaoAnestesiaLocal());
        anamnese.setSensibilidadeDentaria(request.getSensibilidadeDentaria());
        anamnese.setBruxismoApertamento(request.getBruxismoApertamento());
        anamnese.setSangramentoGengival(request.getSangramentoGengival());
        anamnese.setPossuiHabito(request.getPossuiHabito());
        anamnese.setDescricaoHabito(request.getDescricaoHabito());
        anamnese.setHistoricoDiabetes(request.getHistoricoDiabetes());
        anamnese.setSangramentoExcessivo(request.getSangramentoExcessivo());
        anamnese.setProblemaCardiaco(request.getProblemaCardiaco());
        anamnese.setDescricaoProblemaCardiaco(request.getDescricaoProblemaCardiaco());
        anamnese.setPressaoArterialNormal(request.getPressaoArterialNormal());
        anamnese.setDescricaoPressaoArterial(request.getDescricaoPressaoArterial());
        anamnese.setHistoricoDesmaioConvulsao(request.getHistoricoDesmaioConvulsao());
        anamnese.setGestante(request.getGestante());

        anamneseRepository.save(anamnese);

        return anamnese;

    }

}

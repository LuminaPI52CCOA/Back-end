package com.lumina.backend.service.cliente;

import com.lumina.backend.dto.anamnese.AnamneseMapper;
import com.lumina.backend.dto.anamnese.AnamneseRequest;
import com.lumina.backend.dto.cliente.ClienteMapper;
import com.lumina.backend.dto.cliente.ClienteRequest;
import com.lumina.backend.dto.cliente.ResponsavelRequest;
import com.lumina.backend.domain.cliente.Cliente;
import com.lumina.backend.domain.cliente.ClienteCommand;
import com.lumina.backend.domain.cliente.ClienteId;
import com.lumina.backend.domain.cliente.ClienteRepositoryPort;
import com.lumina.backend.exception.CpfDuplicadoException;
import com.lumina.backend.exception.EmailDuplicadoException;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.model.EstadoCivil;
import com.lumina.backend.repository.AnamneseRepository;
import com.lumina.backend.repository.EstadoCivilRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepositoryPort repositoryPort;
    private final AnamneseRepository anamneseRepository;
    private final EstadoCivilRepository estadoCivilRepository;

    public ClienteService(ClienteRepositoryPort repositoryPort,
                          AnamneseRepository anamneseRepository,
                          EstadoCivilRepository estadoCivilRepository) {
        this.repositoryPort = repositoryPort;
        this.anamneseRepository = anamneseRepository;
        this.estadoCivilRepository = estadoCivilRepository;
    }

    public List<EstadoCivil> listarEstadosCivis() {
        return estadoCivilRepository.findAll();
    }

    public List<Cliente> listar(){
        return repositoryPort.buscarTodos();
    }

    public List<Cliente> listarComFiltros(String nome, String cpf) {
        if (cpf != null && !cpf.isBlank()) {
            return repositoryPort.buscarPorCpf(cpf)
                    .map(List::of)
                    .orElse(Collections.emptyList());
        }
        if (nome != null && !nome.isBlank()) {
            return repositoryPort.buscarPorNome(nome);
        }
        return repositoryPort.buscarTodos();
    }

    public Cliente cadastrar(ClienteRequest request){
        if(repositoryPort.existePorEmail(request.getEmail())){
            throw new EmailDuplicadoException("Email " + request.getEmail() + " já existe");
        }
        if(repositoryPort.existePorCpf(request.getCpf())){
            throw new CpfDuplicadoException("Cpf " + request.getCpf() + " já existe");
        }

        ClienteCommand command = ClienteMapper.toCommand(request);

        // Tratar cliente indicacao
        if (request.getFkClienteIndicacao() != null) {
            Cliente indicacao = repositoryPort.buscarPorId(ClienteId.of(request.getFkClienteIndicacao().longValue()))
                    .orElseThrow(() -> new EntidadeNaoEncontrada("Cliente indicador não encontrado!"));
            command = new ClienteCommand(
                command.nome(),
                command.cpf(),
                command.rg(),
                command.dataNascimento(),
                command.naturalidade(),
                command.nacionalidade(),
                command.sexo(),
                command.cep(),
                command.enderecoResidencial(),
                command.email(),
                command.numeroCelular(),
                command.fkEstadoCivil(),
                indicacao,
                command.responsavel(),
                command.grauParentescoResponsavel()
            );
        }

        // Tratar responsavel
        if (request.getResponsavel() != null) {
            ResponsavelRequest respDto = request.getResponsavel();
            Cliente responsavel = null;

            if (respDto.getIdCliente() != null) {
                responsavel = repositoryPort.buscarPorId(ClienteId.of(respDto.getIdCliente()))
                        .orElseThrow(() -> new EntidadeNaoEncontrada("Responsável não encontrado!"));
            } else if (respDto.getCpf() != null && !respDto.getCpf().isBlank()) {
                responsavel = repositoryPort.buscarPorCpf(respDto.getCpf())
                        .orElseGet(() -> {
                            ClienteCommand respCommand = new ClienteCommand(
                                respDto.getNome(),
                                respDto.getCpf(),
                                respDto.getRg(),
                                respDto.getDataNascimento(),
                                respDto.getNaturalidade(),
                                respDto.getNacionalidade(),
                                respDto.getSexo(),
                                respDto.getCep(),
                                respDto.getEnderecoResidencial(),
                                respDto.getEmail(),
                                respDto.getNumeroCelular(),
                                respDto.getFkEstadoCivil(),
                                null,
                                null,
                                null
                            );
                            Cliente novoResp = Cliente.criar(respCommand);
                            return repositoryPort.salvar(novoResp);
                        });
            }
            
            command = new ClienteCommand(
                command.nome(),
                command.cpf(),
                command.rg(),
                command.dataNascimento(),
                command.naturalidade(),
                command.nacionalidade(),
                command.sexo(),
                command.cep(),
                command.enderecoResidencial(),
                command.email(),
                command.numeroCelular(),
                command.fkEstadoCivil(),
                command.clienteIndicacao(),
                responsavel,
                command.grauParentescoResponsavel()
            );
        }

        Cliente cliente = Cliente.criar(command);
        return repositoryPort.salvar(cliente);
    }

    public Cliente buscarPorId(Long id){
        return repositoryPort.buscarPorId(ClienteId.of(id))
                .orElseThrow(() -> new EntidadeNaoEncontrada("Cliente não encontrado!"));
    }

    public Cliente atualizar(ClienteRequest request, Long id){
        Cliente clienteExistente = repositoryPort.buscarPorId(ClienteId.of(id))
                .orElseThrow(() -> new EntidadeNaoEncontrada("Cliente não encontrado!"));

        ClienteCommand command = ClienteMapper.toCommand(request);

        // Tratar cliente indicacao
        if (request.getFkClienteIndicacao() != null) {
            Cliente indicacao = repositoryPort.buscarPorId(ClienteId.of(request.getFkClienteIndicacao().longValue()))
                    .orElseThrow(() -> new EntidadeNaoEncontrada("Cliente indicador não encontrado!"));
            command = new ClienteCommand(
                command.nome(),
                command.cpf(),
                command.rg(),
                command.dataNascimento(),
                command.naturalidade(),
                command.nacionalidade(),
                command.sexo(),
                command.cep(),
                command.enderecoResidencial(),
                command.email(),
                command.numeroCelular(),
                command.fkEstadoCivil(),
                indicacao,
                command.responsavel(),
                command.grauParentescoResponsavel()
            );
        }

        // Tratar responsavel
        if (request.getResponsavel() != null) {
            ResponsavelRequest respDto = request.getResponsavel();
            Cliente responsavel = null;

            if (respDto.getIdCliente() != null) {
                responsavel = repositoryPort.buscarPorId(ClienteId.of(respDto.getIdCliente()))
                        .orElseThrow(() -> new EntidadeNaoEncontrada("Responsável não encontrado!"));
            } else if (respDto.getCpf() != null && !respDto.getCpf().isBlank()) {
                responsavel = repositoryPort.buscarPorCpf(respDto.getCpf())
                        .orElseGet(() -> {
                            ClienteCommand respCommand = new ClienteCommand(
                                respDto.getNome(),
                                respDto.getCpf(),
                                respDto.getRg(),
                                respDto.getDataNascimento(),
                                respDto.getNaturalidade(),
                                respDto.getNacionalidade(),
                                respDto.getSexo(),
                                respDto.getCep(),
                                respDto.getEnderecoResidencial(),
                                respDto.getEmail(),
                                respDto.getNumeroCelular(),
                                respDto.getFkEstadoCivil(),
                                null,
                                null,
                                null
                            );
                            Cliente novoResp = Cliente.criar(respCommand);
                            return repositoryPort.salvar(novoResp);
                        });
            }
            
            command = new ClienteCommand(
                command.nome(),
                command.cpf(),
                command.rg(),
                command.dataNascimento(),
                command.naturalidade(),
                command.nacionalidade(),
                command.sexo(),
                command.cep(),
                command.enderecoResidencial(),
                command.email(),
                command.numeroCelular(),
                command.fkEstadoCivil(),
                command.clienteIndicacao(),
                responsavel,
                command.grauParentescoResponsavel()
            );
        }

        Cliente clienteAtualizado = clienteExistente.atualizar(command);
        return repositoryPort.salvar(clienteAtualizado);
    }

    public List<Anamnese> listarAnamnese(Integer id){
        List<Anamnese> anamnese = anamneseRepository.findAnamneseByFkCliente_IdCliente(id);
        if(anamnese.isEmpty()) throw new EntidadeNaoEncontrada("Anamnese não encontrada!") ;
        return anamnese;
    }

    public Anamnese cadastrarAnamnese(Long id, AnamneseRequest request){
        repositoryPort.buscarPorId(ClienteId.of(id))
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

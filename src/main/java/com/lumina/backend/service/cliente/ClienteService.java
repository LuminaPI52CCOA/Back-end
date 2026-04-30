package com.lumina.backend.service.cliente;

import com.lumina.backend.dto.anamnese.AnamneseMapper;
import com.lumina.backend.dto.anamnese.AnamneseRequest;
import com.lumina.backend.dto.cliente.ClienteRequest;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.repository.AnamneseRepository;
import com.lumina.backend.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final AnamneseRepository anamneseRepository;

    public ClienteService(ClienteRepository repository, AnamneseRepository anamneseRepository) {
        this.repository = repository;
        this.anamneseRepository = anamneseRepository;
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

    public List<Anamnese> listarAnamnese(Integer id){
        List<Anamnese> anamnese = anamneseRepository.findAnamneseByFkCliente_IdCliente(id);
        if(anamnese.isEmpty()) throw new EntidadeNaoEncontrada("Anamnese não encontrada!") ;
        return anamnese;
    }

    public Anamnese cadastrarAnamnese(Integer id, AnamneseRequest request){
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

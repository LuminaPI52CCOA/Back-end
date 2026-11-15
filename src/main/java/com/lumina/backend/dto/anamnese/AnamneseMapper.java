package com.lumina.backend.dto.anamnese;

import com.lumina.backend.model.Anamnese;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.repository.ClienteRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AnamneseMapper {

    private final ClienteRepository repositorioCliente;

    public AnamneseMapper(ClienteRepository repositorioCliente) {
        this.repositorioCliente = repositorioCliente;
    }

    public Anamnese toEntity(AnamneseRequest dto) {
        if (dto == null) {
            return null;
        }

        Anamnese entidade = new Anamnese();

        Cliente cliente = repositorioCliente.getReferenceById(dto.fkCliente());
        entidade.setFkCliente(cliente);
        aplicarDados(entidade, dto);

        return entidade;
    }

    public static void aplicarDados(Anamnese entidade, AnamneseRequest dto) {
        if (entidade == null || dto == null) {
            return;
        }
        entidade.setIdAnamnese(dto.idAnamnese());
        entidade.setDataAnamnese(dto.dataAnamnese());
        entidade.setFazendoTratamento(dto.fazendoTratamento());
        entidade.setDescricaoTratamento(dto.descricaoTratamento());
        entidade.setDoresCabecaFaceAtm(dto.doresCabecaFaceAtm());
        entidade.setAlergiaMedicamentos(dto.alergiaMedicamentos());
        entidade.setDescricaoAlergiaMedicamentos(dto.descricaoAlergiaMedicamentos());
        entidade.setReacaoAnestesiaLocal(dto.reacaoAnestesiaLocal());
        entidade.setSensibilidadeDentaria(dto.sensibilidadeDentaria());
        entidade.setBruxismoApertamento(dto.bruxismoApertamento());
        entidade.setSangramentoGengival(dto.sangramentoGengival());
        entidade.setPossuiHabito(dto.possuiHabito());
        entidade.setDescricaoHabito(dto.descricaoHabito());
        entidade.setHistoricoDiabetes(dto.historicoDiabetes());
        entidade.setSangramentoExcessivo(dto.sangramentoExcessivo());
        entidade.setProblemaCardiaco(dto.problemaCardiaco());
        entidade.setDescricaoProblemaCardiaco(dto.descricaoProblemaCardiaco());
        entidade.setPressaoArterialNormal(dto.pressaoArterialNormal());
        entidade.setDescricaoPressaoArterial(dto.descricaoPressaoArterial());
        entidade.setHistoricoDesmaioConvulsao(dto.historicoDesmaioConvulsao());
        entidade.setGestante(dto.gestante());
    }

    public static AnamneseResponse toDto(Anamnese model) {
        if (model == null) {
            return null;
        }

        Cliente cliente = model.getFkCliente();
        AnamneseResponse.AnamneseCliente anamneseCliente = null;
        if (cliente != null) {
            Integer fkClienteIndicacao = cliente.getClienteIndicacao() != null && cliente.getClienteIndicacao().getIdCliente() != null
                    ? cliente.getClienteIndicacao().getIdCliente().intValue()
                    : null;
            Integer fkResponsavel = cliente.getResponsavel() != null && cliente.getResponsavel().getIdCliente() != null
                    ? cliente.getResponsavel().getIdCliente().intValue()
                    : null;
            anamneseCliente = new AnamneseResponse.AnamneseCliente(
                    cliente.getIdCliente(),
                    cliente.getNome(),
                    cliente.getCpf(),
                    cliente.getRg(),
                    cliente.getDataNascimento(),
                    cliente.getNumeroCelular(),
                    cliente.getEmail(),
                    cliente.getSexo(),
                    cliente.getNaturalidade(),
                    cliente.getNacionalidade(),
                    cliente.getFkEstadoCivil(),
                    cliente.getEnderecoResidencial(),
                    cliente.getCep(),
                    fkClienteIndicacao,
                    fkResponsavel,
                    cliente.getGrauParentescoResponsavel()
            );
        }

        return new AnamneseResponse(
                model.getIdAnamnese(),
                anamneseCliente,
                model.getDataAnamnese(),
                model.getFazendoTratamento(),
                model.getDescricaoTratamento(),
                model.getDoresCabecaFaceAtm(),
                model.getAlergiaMedicamentos(),
                model.getDescricaoAlergiaMedicamentos(),
                model.getReacaoAnestesiaLocal(),
                model.getSensibilidadeDentaria(),
                model.getBruxismoApertamento(),
                model.getSangramentoGengival(),
                model.getPossuiHabito(),
                model.getDescricaoHabito(),
                model.getHistoricoDiabetes(),
                model.getSangramentoExcessivo(),
                model.getProblemaCardiaco(),
                model.getDescricaoProblemaCardiaco(),
                model.getPressaoArterialNormal(),
                model.getDescricaoPressaoArterial(),
                model.getHistoricoDesmaioConvulsao(),
                model.getGestante()
        );
    }

    public static List<AnamneseResponse> toDto(List<Anamnese> entities) {
        return entities.stream()
                .map(AnamneseMapper::toDto)
                .toList();
    }
}

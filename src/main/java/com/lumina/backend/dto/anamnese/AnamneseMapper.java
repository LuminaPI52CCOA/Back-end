package com.lumina.backend.dto.anamnese;

import com.lumina.backend.dto.cliente.ClienteMapper;
import com.lumina.backend.dto.cliente.ClienteRequest;
import com.lumina.backend.dto.cliente.ClienteResponse;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.model.Cliente;

import java.util.List;

public class AnamneseMapper {

    public static Anamnese toEntity(AnamneseRequest dto) {
        if (dto == null) {
            return null;
        }

        Anamnese entidade = new Anamnese();
        entidade.setIdAnamnese(dto.getIdAnamnese());
        entidade.setDataAnamnese(dto.getDataAnamnese());
        entidade.setFazendoTratamento(dto.getFazendoTratamento());
        entidade.setDescricaoTratamento(dto.getDescricaoTratamento());
        entidade.setDoresCabecaFaceAtm(dto.getDoresCabecaFaceAtm());
        entidade.setAlergiaMedicamentos(dto.getAlergiaMedicamentos());
        entidade.setDescricaoAlergiaMedicamentos(dto.getDescricaoAlergiaMedicamentos());
        entidade.setReacaoAnestesiaLocal(dto.getReacaoAnestesiaLocal());
        entidade.setSensibilidadeDentaria(dto.getSensibilidadeDentaria());
        entidade.setBruxismoApertamento(dto.getBruxismoApertamento());
        entidade.setSangramentoGengival(dto.getSangramentoGengival());
        entidade.setPossuiHabito(dto.getPossuiHabito());
        entidade.setDescricaoHabito(dto.getDescricaoHabito());
        entidade.setHistoricoDiabetes(dto.getHistoricoDiabetes());
        entidade.setSangramentoExcessivo(dto.getSangramentoExcessivo());
        entidade.setProblemaCardiaco(dto.getProblemaCardiaco());
        entidade.setDescricaoProblemaCardiaco(dto.getDescricaoProblemaCardiaco());
        entidade.setPressaoArterialNormal(dto.getPressaoArterialNormal());
        entidade.setDescricaoPressaoArterial(dto.getDescricaoPressaoArterial());
        entidade.setHistoricoDesmaioConvulsao(dto.getHistoricoDesmaioConvulsao());
        entidade.setGestante(dto.getGestante());

        return entidade;
    }

    public static AnamneseResponse toDto(Anamnese model) {
        if (model == null) {
            return null;
        }

        Cliente cliente = model.getFkCliente();

        AnamneseResponse.AnamneseCliente anamneseCliente = new AnamneseResponse.AnamneseCliente();
        anamneseCliente.setIdCliente(cliente.getIdCliente());
        anamneseCliente.setNome(cliente.getNome());
        anamneseCliente.setCpf(cliente.getCpf());
        anamneseCliente.setRg(cliente.getRg());
        anamneseCliente.setDataNascimento(cliente.getDataNascimento());
        anamneseCliente.setNumeroCelular(cliente.getNumeroCelular());
        anamneseCliente.setEmail(cliente.getEmail());
        anamneseCliente.setSexo(cliente.getSexo());
        anamneseCliente.setNaturalidade(cliente.getNaturalidade());
        anamneseCliente.setNacionalidade(cliente.getNacionalidade());
        anamneseCliente.setFkEstadoCivil(cliente.getFkEstadoCivil());
        anamneseCliente.setEnderecoResidencial(cliente.getEnderecoResidencial());
        anamneseCliente.setCep(cliente.getCep());
        anamneseCliente.setFkClienteIndicacao(cliente.getFkClienteIndicacao());
        anamneseCliente.setFkResponsavel(cliente.getFkResponsavel());
        anamneseCliente.setGrauParentescoResponsavel(cliente.getGrauParentescoResponsavel());



        AnamneseResponse anamnese = new
                AnamneseResponse();

        AnamneseResponse dto = new AnamneseResponse(
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

        return dto;
    }

    public static List<AnamneseResponse> toDto(List<Anamnese> entities) {
        return entities.stream()
                .map(AnamneseMapper::toDto)
                .toList();
    }
}

package com.lumina.backend.dto.cliente;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.Perfil;

import java.util.List;

public class ClienteMapper {
    public static Cliente toEntity(ClienteRequest dto) {
        if (dto == null) {
            return null;
        }

        Cliente entidade = new Cliente();
        entidade.setIdCliente(dto.getIdCliente());
        entidade.setNome(dto.getNome());
        entidade.setCpf(dto.getCpf());
        entidade.setRg(dto.getRg());
        entidade.setDataNascimento(dto.getDataNascimento());
        entidade.setNumeroCelular(dto.getNumeroCelular());
        entidade.setEmail(dto.getEmail());
        entidade.setSexo(dto.getSexo());
        entidade.setNaturalidade(dto.getNaturalidade());
        entidade.setNacionalidade(dto.getNacionalidade());
        entidade.setFkEstadoCivil(dto.getFkEstadoCivil());
        entidade.setEnderecoResidencial(dto.getEnderecoResidencial());
        entidade.setCep(dto.getCep());
        entidade.setGrauParentescoResponsavel(dto.getGrauParentescoResponsavel());

        return entidade;
    }

    public static ClienteResponse toDto(Cliente model) {
        if (model == null) {
            return null;
        }

        Integer fkClienteIndicacao = model.getClienteIndicacao() != null && model.getClienteIndicacao().getIdCliente() != null
                ? model.getClienteIndicacao().getIdCliente().intValue()
                : null;

        Integer fkResponsavel = model.getResponsavel() != null && model.getResponsavel().getIdCliente() != null
                ? model.getResponsavel().getIdCliente().intValue()
                : null;

        ClienteResponse dto = new ClienteResponse(
                model.getIdCliente(),
                model.getNome(),
                model.getCpf(),
                model.getRg(),
                model.getDataNascimento(),
                model.getNumeroCelular(),
                model.getEmail(),
                model.getSexo(),
                model.getNaturalidade(),
                model.getNacionalidade(),
                model.getFkEstadoCivil(),
                model.getEnderecoResidencial(),
                model.getCep(),
                fkClienteIndicacao,
                fkResponsavel,
                model.getGrauParentescoResponsavel()
        );

        return dto;
    }

    public static List<ClienteResponse> toDto(List<Cliente> entities) {
        return entities.stream()
                .map(ClienteMapper::toDto)
                .toList();
    }
}

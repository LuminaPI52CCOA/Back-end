package com.lumina.backend.dto.cliente;
import com.lumina.backend.domain.cliente.Cliente;
import com.lumina.backend.domain.cliente.ClienteCommand;
import com.lumina.backend.domain.cliente.ClienteId;

import java.util.List;

public class ClienteMapper {
    public static ClienteCommand toCommand(ClienteRequest dto) {
        if (dto == null) {
            return null;
        }

        return new ClienteCommand(
            dto.getNome(),
            dto.getCpf(),
            dto.getRg(),
            dto.getDataNascimento(),
            dto.getNaturalidade(),
            dto.getNacionalidade(),
            dto.getSexo(),
            dto.getCep(),
            dto.getEnderecoResidencial(),
            dto.getEmail(),
            dto.getNumeroCelular(),
            dto.getFkEstadoCivil(),
            null, // clienteIndicacao - será tratado no service
            null, // responsavel - será tratado no service
            dto.getGrauParentescoResponsavel()
        );
    }

    public static ClienteResponse toDto(Cliente model) {
        if (model == null) {
            return null;
        }

        Integer fkClienteIndicacao = model.getClienteIndicacao() != null && model.getClienteIndicacao().getId() != null
                ? model.getClienteIndicacao().getId().getValue().intValue()
                : null;

        Integer fkResponsavel = model.getResponsavel() != null && model.getResponsavel().getId() != null
                ? model.getResponsavel().getId().getValue().intValue()
                : null;

        Long idCliente = model.getId() != null ? model.getId().getValue() : null;

        ClienteResponse dto = new ClienteResponse(
                idCliente,
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

    public static ClienteResponse toDto(com.lumina.backend.model.Cliente model) {
        if (model == null) {
            return null;
        }

        Integer fkClienteIndicacao = model.getClienteIndicacao() != null && model.getClienteIndicacao().getIdCliente() != null
                ? model.getClienteIndicacao().getIdCliente().intValue()
                : null;

        Integer fkResponsavel = model.getResponsavel() != null && model.getResponsavel().getIdCliente() != null
                ? model.getResponsavel().getIdCliente().intValue()
                : null;

        return new ClienteResponse(
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
    }

    public static List<ClienteResponse> toDto(List<Cliente> entities) {
        return entities.stream()
                .map(ClienteMapper::toDto)
                .toList();
    }

    public static com.lumina.backend.model.Cliente toEntity(ClienteRequest dto) {
        if (dto == null) {
            return null;
        }

        com.lumina.backend.model.Cliente entity = new com.lumina.backend.model.Cliente();
        entity.setIdCliente(dto.getIdCliente());
        entity.setNome(dto.getNome());
        entity.setCpf(dto.getCpf());
        entity.setRg(dto.getRg());
        entity.setDataNascimento(dto.getDataNascimento());
        entity.setNaturalidade(dto.getNaturalidade());
        entity.setNacionalidade(dto.getNacionalidade());
        entity.setSexo(dto.getSexo());
        entity.setCep(dto.getCep());
        entity.setEnderecoResidencial(dto.getEnderecoResidencial());
        entity.setEmail(dto.getEmail());
        entity.setNumeroCelular(dto.getNumeroCelular());
        entity.setFkEstadoCivil(dto.getFkEstadoCivil());
        entity.setGrauParentescoResponsavel(dto.getGrauParentescoResponsavel());

        return entity;
    }
}

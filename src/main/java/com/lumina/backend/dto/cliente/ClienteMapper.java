package com.lumina.backend.dto.cliente;

import com.lumina.backend.model.Cliente;

import java.util.List;

public class ClienteMapper {

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

    public static Cliente toEntity(ClienteRequest dto) {
        if (dto == null) {
            return null;
        }

        Cliente entity = new Cliente();
        entity.setIdCliente(dto.idCliente());
        aplicarDados(entity, dto);
        return entity;
    }

    public static void aplicarDados(Cliente entity, ClienteRequest dto) {
        if (entity == null || dto == null) {
            return;
        }
        entity.setNome(dto.nome());
        entity.setCpf(dto.cpf());
        entity.setRg(dto.rg());
        entity.setDataNascimento(dto.dataNascimento());
        entity.setNaturalidade(dto.naturalidade());
        entity.setNacionalidade(dto.nacionalidade());
        entity.setSexo(dto.sexo());
        entity.setCep(dto.cep());
        entity.setEnderecoResidencial(dto.enderecoResidencial());
        entity.setEmail(dto.email());
        entity.setNumeroCelular(dto.numeroCelular());
        entity.setFkEstadoCivil(dto.fkEstadoCivil());
        entity.setGrauParentescoResponsavel(dto.grauParentescoResponsavel());
    }
}

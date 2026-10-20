package com.lumina.backend.infrastructure.persistence.mapper;

import com.lumina.backend.domain.cliente.Cliente;
import com.lumina.backend.domain.cliente.ClienteCommand;
import com.lumina.backend.domain.cliente.ClienteId;
import com.lumina.backend.infrastructure.persistence.jpa.ClienteJpaEntity;

public class ClienteMapperJpa {

    public static ClienteJpaEntity toJpa(Cliente cliente) {
        if (cliente == null) {
            return null;
        }

        ClienteJpaEntity entity = new ClienteJpaEntity();
        if (cliente.getId() != null && cliente.getId().getValue() != null) {
            entity.setIdCliente(cliente.getId().getValue());
        }
        entity.setNome(cliente.getNome());
        entity.setCpf(cliente.getCpf());
        entity.setRg(cliente.getRg());
        entity.setDataNascimento(cliente.getDataNascimento());
        entity.setNaturalidade(cliente.getNaturalidade());
        entity.setNacionalidade(cliente.getNacionalidade());
        entity.setSexo(cliente.getSexo());
        entity.setCep(cliente.getCep());
        entity.setEnderecoResidencial(cliente.getEnderecoResidencial());
        entity.setEmail(cliente.getEmail());
        entity.setNumeroCelular(cliente.getNumeroCelular());
        entity.setFkEstadoCivil(cliente.getFkEstadoCivil());
        entity.setGrauParentescoResponsavel(cliente.getGrauParentescoResponsavel());

        if (cliente.getClienteIndicacao() != null) {
            entity.setClienteIndicacao(toJpa(cliente.getClienteIndicacao()));
        }
        if (cliente.getResponsavel() != null) {
            entity.setResponsavel(toJpa(cliente.getResponsavel()));
        }

        return entity;
    }

    public static Cliente toDomain(ClienteJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        ClienteId id = entity.getIdCliente() != null ? ClienteId.of(entity.getIdCliente()) : null;

        Cliente clienteIndicacao = null;
        if (entity.getClienteIndicacao() != null) {
            clienteIndicacao = toDomain(entity.getClienteIndicacao());
        }

        Cliente responsavel = null;
        if (entity.getResponsavel() != null) {
            responsavel = toDomain(entity.getResponsavel());
        }

        ClienteCommand command = new ClienteCommand(
            entity.getNome(),
            entity.getCpf(),
            entity.getRg(),
            entity.getDataNascimento(),
            entity.getNaturalidade(),
            entity.getNacionalidade(),
            entity.getSexo(),
            entity.getCep(),
            entity.getEnderecoResidencial(),
            entity.getEmail(),
            entity.getNumeroCelular(),
            entity.getFkEstadoCivil(),
            clienteIndicacao,
            responsavel,
            entity.getGrauParentescoResponsavel()
        );
        
        Cliente cliente;
        if (id != null) {
            cliente = Cliente.reconstruir(id, command);
        } else {
            cliente = Cliente.criar(command);
        }
        
        return cliente;
    }

    public static ClienteCommand toCommand(Cliente cliente) {
        if (cliente == null) {
            return null;
        }

        return new ClienteCommand(
            cliente.getNome(),
            cliente.getCpf(),
            cliente.getRg(),
            cliente.getDataNascimento(),
            cliente.getNaturalidade(),
            cliente.getNacionalidade(),
            cliente.getSexo(),
            cliente.getCep(),
            cliente.getEnderecoResidencial(),
            cliente.getEmail(),
            cliente.getNumeroCelular(),
            cliente.getFkEstadoCivil(),
            cliente.getClienteIndicacao(),
            cliente.getResponsavel(),
            cliente.getGrauParentescoResponsavel()
        );
    }
}
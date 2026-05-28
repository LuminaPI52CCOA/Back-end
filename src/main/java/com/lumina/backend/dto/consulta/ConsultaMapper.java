package com.lumina.backend.dto.consulta;

import com.lumina.backend.dto.cliente.ClienteMapper;
import com.lumina.backend.dto.usuario.UsuarioMapper;
import com.lumina.backend.model.Consulta;

import java.util.List;

public class ConsultaMapper {
    public static ConsultaResponse toResponse(Consulta consulta) {
        if(consulta == null) {
            return null;
        }

        return new ConsultaResponse(
                consulta.getIdConsulta(),
                ClienteMapper.toDto(consulta.getCliente()),
                UsuarioMapper.toDto(consulta.getUsuario()),
                consulta.getData(),
                consulta.getHorarioFim(),
                consulta.getHorarioFim()
        );
    }

    public static List<ConsultaResponse> toResponse(List<Consulta> consultas) {
        return consultas.stream()
                .map(ConsultaMapper::toResponse)
                .toList();

    }

    public static Consulta toEntity(ConsultaRequest consulta) {
        if(consulta == null) {
            return null;
        }

        Consulta consultaEntidade = new Consulta();
        consultaEntidade.setData(consulta.getData());
        consultaEntidade.setHorarioFim(consulta.getHorarioFim());
        consultaEntidade.setHorarioInicio(consulta.getHorarioInicio());

        return consultaEntidade;
    }
}

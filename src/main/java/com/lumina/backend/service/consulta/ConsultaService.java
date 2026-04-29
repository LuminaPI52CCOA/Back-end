package com.lumina.backend.service.consulta;

import com.lumina.backend.dto.consulta.ConsultaMapper;
import com.lumina.backend.dto.consulta.ConsultaRequest;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.ClienteRepository;
import com.lumina.backend.repository.ConsultaRepository;
import com.lumina.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultaService {
    private final ConsultaRepository consultaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;

    public ConsultaService(ConsultaRepository consultaRepository,
                           UsuarioRepository usuarioRepository,
                           ClienteRepository clienteRepository) {
        this.consultaRepository = consultaRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
    }

    public List<Consulta> listar() {
        return consultaRepository.findAll();
    }

    public Consulta cadastrar(ConsultaRequest consulta) {
        Cliente clienteEncontrado = clienteRepository.findById(consulta.getIdCliente())
                .orElseThrow(() -> new EntidadeNaoEncontrada("Cliente não encontrado"));

        Usuario usuarioEncontrado = usuarioRepository.findById(consulta.getIdUsuario())
                .orElseThrow(() -> new EntidadeNaoEncontrada("Usuário não encontrado"));

        Consulta consultaEntidade = ConsultaMapper.toEntity(consulta);
        consultaEntidade.setCliente(clienteEncontrado);
        consultaEntidade.setUsuario(usuarioEncontrado);

        return consultaRepository.save(consultaEntidade);
    }

    public Consulta buscarPorId(Long id) {

        return consultaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Consulta não encontrada"));
    }
}

package com.lumina.backend.repository;

import com.lumina.backend.dto.usuario.UsuarioRequest;
import com.lumina.backend.model.Usuario;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    @Modifying
    @Transactional
    @Query("""
UPDATE Usuario u SET 
u.nome = :#{#usuarios.nome},
u.cpf = :#{#usuarios.cpf},
u.email = :#{#usuarios.email},
u.senha = :#{#usuarios.senha},
u.fkPerfil = :#{#usuarios.fkPerfil},
u.cro = :#{#usuarios.cro},
u.ativo = :#{#usuarios.ativo}
WHERE u.idUsuario = :id
""")
    int atualizarPeloId(@Param("usuarios") UsuarioRequest usuarios,
                        @Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("""
UPDATE Usuario u SET 
u.ativo = :ativo
WHERE u.idUsuario = :id
""")
    int logicalDelete(@Param("ativo") Boolean ativo,
                        @Param("id") Integer id);
}

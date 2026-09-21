package com.lumina.backend.repository;

import com.lumina.backend.model.UsuarioAlexa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioAlexaRepository extends JpaRepository<UsuarioAlexa, Long> {

    Optional<UsuarioAlexa> findByAlexaUserIdAndAtivoTrue(String alexaUserId);

    Optional<UsuarioAlexa> findByCodigoPareamento(String codigoPareamento);

    Optional<UsuarioAlexa> findByUsuario_IdUsuarioAndAtivoTrue(Long idUsuario);

    List<UsuarioAlexa> findAllByAtivoTrue();
}

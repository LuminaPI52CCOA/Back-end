package com.lumina.backend.dto.usuario;

import com.lumina.backend.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UsuarioDetalhesDto implements UserDetails {

    private final String nome;

    private final String email;

    private final String senha;

    private final Integer fkPerfil;

    public UsuarioDetalhesDto(Usuario usuario) {
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.senha = usuario.getSenha();
        this.fkPerfil = usuario.getFkPerfil();
    }

    public String getNome() {
        return nome;
    }

    public Integer getFkPerfil() {
        return fkPerfil;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.fkPerfil == null) {
            return Collections.emptyList();
        }
        String roleName = switch (this.fkPerfil) {
            case 1 -> "ROLE_ADMIN";
            case 2 -> "ROLE_DENTISTA";
            case 3 -> "ROLE_RECEPCIONISTA";
            default -> "ROLE_USER";
        };
        return List.of(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

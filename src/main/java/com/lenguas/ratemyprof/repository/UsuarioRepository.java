package com.lenguas.ratemyprof.repository;

import com.lenguas.ratemyprof.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Por el id estable de Google (el "sub"): es la identidad de login. */
    Optional<Usuario> findByGoogleSub(String googleSub);
}

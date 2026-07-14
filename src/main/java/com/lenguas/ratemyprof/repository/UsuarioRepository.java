package com.lenguas.ratemyprof.repository;

import com.lenguas.ratemyprof.model.Rol;
import com.lenguas.ratemyprof.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByDni(String dni);
    boolean existsByDni(String dni);

    /** ¿Hay al menos un usuario con este rol? El seeder lo usa para crear el primer admin. */
    boolean existsByRol(Rol rol);
}

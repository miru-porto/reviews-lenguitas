package com.lenguas.ratemyprof.service;

import com.lenguas.ratemyprof.exception.ConflictException;
import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Spring Security usa este método para resolver un usuario por su "username".
     * En nuestro caso el username es el DNI. No hay contraseña: la autenticación
     * es solo por identidad (ver AuthApiController), así que el password del
     * UserDetails queda vacío y nunca se compara.
     */
    @Override
    public UserDetails loadUserByUsername(String dni) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByDni(dni)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + dni));

        return new User(usuario.getDni(), "",
                AuthorityUtils.createAuthorityList("ROLE_" + usuario.getRol().name()));
    }

    /** Alta de un usuario nuevo. 409 si el DNI ya está registrado. */
    public Usuario registrar(String dni, String nombre) {
        if (usuarioRepository.existsByDni(dni)) {
            throw new ConflictException("Ya existe un usuario con ese DNI");
        }

        Usuario usuario = new Usuario();
        usuario.setDni(dni);
        usuario.setNombre(nombre);

        return usuarioRepository.save(usuario);
    }

    /** Busca por DNI sin fallar: lo usa el login para decidir alta vs ingreso. */
    public Optional<Usuario> buscarPorDni(String dni) {
        return usuarioRepository.findByDni(dni);
    }

    /** Igual que buscarPorDni pero exige que exista (usuario ya autenticado). */
    public Usuario findByDni(String dni) {
        return usuarioRepository.findByDni(dni)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}

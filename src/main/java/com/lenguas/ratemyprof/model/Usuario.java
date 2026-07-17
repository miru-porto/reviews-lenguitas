package com.lenguas.ratemyprof.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El apodo público: es lo ÚNICO que se ve junto a una review. Se pide al
    // entrar por primera vez y NO sale de Google a propósito: Google devuelve el
    // nombre real, y firmar críticas a profesores con nombre y apellido de cada
    // estudiante es justo lo que no queremos. Nullable = perfil sin completar,
    // que es como el front sabe que tiene que pedirlo.
    @Column
    private String nombre;

    // La identidad: el "sub" de Google, su id estable de usuario. Es el principal
    // de Spring Security (auth.getName()). Se usa esto y no el email porque el
    // email puede cambiar de dueño; el sub no cambia nunca.
    @NotBlank
    @Column(name = "google_sub", nullable = false, unique = true)
    private String googleSub;

    // Lo devuelve Google con el scope 'email'. No se muestra en la app: sirve
    // para reconocer a la admin (ver ADMIN_EMAIL) y para poder contactar.
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    // Se guarda como texto ("USER"/"ADMIN"), no como número: EnumType.ORDINAL se
    // rompería si algún día se reordena el enum. El ColumnDefault hace que el
    // ALTER TABLE de Hibernate (ddl-auto=update) complete las filas existentes
    // con 'USER' en vez de fallar por el NOT NULL.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'USER'")
    private Rol rol = Rol.USER;
}

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

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    // El DNI es la identidad del usuario: con él se loguea (no hay contraseña).
    @NotBlank
    @Column(nullable = false, unique = true)
    private String dni;

    // Opcional: no se pide en el alta por DNI, pero queda la columna por si más
    // adelante se quiere contactar al usuario. Nullable; único si está presente.
    @Email
    @Column(unique = true)
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

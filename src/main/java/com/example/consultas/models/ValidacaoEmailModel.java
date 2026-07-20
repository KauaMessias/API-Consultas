package com.example.consultas.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TB_VerificarEmails")
public class ValidacaoEmailModel {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID token;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioModel usuario;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private StatusValidacao status;

    public ValidacaoEmailModel(UsuarioModel usuario, LocalDateTime expiresAt, StatusValidacao status) {
        this.usuario = usuario;
        this.expiresAt = expiresAt;
        this.status = status;
    }
}

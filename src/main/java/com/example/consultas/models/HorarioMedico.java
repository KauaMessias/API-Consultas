package com.example.consultas.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_HORARIOS")
public class HorarioMedico implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private DiaSemana diaSemana;
    @Column(nullable = false)
    private LocalTime horarioInicio;
    @Column(nullable = false)
    private LocalTime horarioFinal;
    @Column(nullable = false)
    private int duracao;
    @Column(nullable = false)
    private boolean ativo;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private MedicoModel medico;
}

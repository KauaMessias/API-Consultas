package com.example.consultas.dtos;

import com.example.consultas.models.ConsultaModel;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConsultaDto(UUID id, @NotNull @Future LocalDateTime dataConsulta, @NotBlank String tipoConsulta, @NotBlank String descricaoConsulta, @NotNull UUID medico_id, @NotNull UUID cliente_id) {

   public ConsultaDto(ConsultaModel consultaModel){
       this(consultaModel.getId(), consultaModel.getDataConsulta(), consultaModel.getTipoConsulta(), consultaModel.getDescricaoConsulta(), consultaModel.getMedico().getId(), consultaModel.getCliente().getId());
   }
}

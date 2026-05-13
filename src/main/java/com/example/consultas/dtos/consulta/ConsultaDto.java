package com.example.consultas.dtos.consulta;

import com.example.consultas.models.ConsultaModel;
import com.example.consultas.models.Status;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConsultaDto(@NotNull @Future LocalDateTime dataConsulta, @NotBlank String tipoConsulta, @NotBlank String descricaoConsulta, @NotNull UUID medico_id) {

   public ConsultaDto(ConsultaModel consultaModel){
       this(consultaModel.getDataConsulta(), consultaModel.getTipoConsulta(), consultaModel.getDescricaoConsulta(), consultaModel.getMedico().getId());
   }

   public ConsultaModel toEntity() {
       return new ConsultaModel(null, dataConsulta, tipoConsulta, descricaoConsulta, null, null, null);
   }

   public ConsultaModel updateEntity(ConsultaModel consulta) {
       consulta.setDataConsulta(dataConsulta);
       consulta.setTipoConsulta(tipoConsulta);
       consulta.setDescricaoConsulta(descricaoConsulta);
       return consulta;
   }
}

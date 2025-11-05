package ar.edu.tpi.seguimiento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String documento;
    private String direccion;
    private LocalDateTime createdAt;
}

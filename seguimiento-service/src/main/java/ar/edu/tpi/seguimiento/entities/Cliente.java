package ar.edu.tpi.seguimiento.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120, nullable = false)
    private String nombre;

    @Column(length = 160, nullable = false)
    private String email;

    @Column(length = 40)
    private String telefono;

    @Column(length = 50)
    private String documento;

    @Column(length = 240)
    private String direccion;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

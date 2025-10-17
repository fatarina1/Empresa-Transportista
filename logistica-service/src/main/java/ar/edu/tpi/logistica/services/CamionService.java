package ar.edu.tpi.logistica.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.tpi.logistica.entities.Camion;
import ar.edu.tpi.logistica.repositories.CamionRepository;

@Service
public class CamionService {

    @Autowired
    private CamionRepository camionRepository;

    public List<Camion> obtenerTodos() {
        return camionRepository.findAll();
    }

    public Camion obtenerPorDominio(String dominio) {
        return camionRepository.findByDominio(dominio);
    }

    public Camion guardar(Camion camion) {
        return camionRepository.save(camion);
    }

    public void eliminar(Long id) {
        camionRepository.deleteById(id);
    }

    public void eliminar(String dominio) {
        Camion camion = camionRepository.findByDominio(dominio);
        if (camion != null) {
            camionRepository.delete(camion);
        }
    }
    
    public Camion actualizar(String dominio, Camion datosActualizados) {
        Camion existente = camionRepository.findByDominio(dominio);
        if (existente != null) {
            datosActualizados.setId(existente.getId()); // mantener el ID original
            datosActualizados.setDominio(dominio); // mantener el dominio
            return camionRepository.save(datosActualizados);
        }
        return null;
    }    

    public List<Camion> obtenerDisponibles() {
        return camionRepository.findByDisponibleTrue();
    }
    
}

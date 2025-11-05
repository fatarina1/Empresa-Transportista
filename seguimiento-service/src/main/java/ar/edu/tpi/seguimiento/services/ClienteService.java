package ar.edu.tpi.seguimiento.services;

import ar.edu.tpi.seguimiento.dto.ClienteDTO;
import ar.edu.tpi.seguimiento.entities.Cliente;
import ar.edu.tpi.seguimiento.repositories.ClienteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<ClienteDTO> obtenerTodos() {
        return clienteRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public Optional<ClienteDTO> obtenerPorId(Long id) {
        return clienteRepository.findById(id)
                .map(this::convertirADTO);
    }

    public Optional<ClienteDTO> obtenerPorEmail(String email) {
        return clienteRepository.findByEmail(email)
                .map(this::convertirADTO);
    }

    @Transactional
    public ClienteDTO crear(ClienteDTO clienteDTO) {
        if (clienteRepository.existsByEmail(clienteDTO.getEmail())) {
            throw new IllegalArgumentException("Ya existe un cliente con ese email");
        }
        
        if (clienteDTO.getDocumento() != null && 
            clienteRepository.existsByDocumento(clienteDTO.getDocumento())) {
            throw new IllegalArgumentException("Ya existe un cliente con ese documento");
        }

        Cliente cliente = convertirAEntidad(clienteDTO);
        cliente.setCreatedAt(LocalDateTime.now());
        Cliente guardado = clienteRepository.save(cliente);
        log.info("Cliente creado: {}", guardado.getId());
        return convertirADTO(guardado);
    }

    @Transactional
    public ClienteDTO actualizar(Long id, ClienteDTO clienteDTO) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        cliente.setNombre(clienteDTO.getNombre());
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setTelefono(clienteDTO.getTelefono());
        cliente.setDocumento(clienteDTO.getDocumento());
        cliente.setDireccion(clienteDTO.getDireccion());

        Cliente actualizado = clienteRepository.save(cliente);
        log.info("Cliente actualizado: {}", id);
        return convertirADTO(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        clienteRepository.deleteById(id);
        log.info("Cliente eliminado: {}", id);
    }

    private ClienteDTO convertirADTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setEmail(cliente.getEmail());
        dto.setTelefono(cliente.getTelefono());
        dto.setDocumento(cliente.getDocumento());
        dto.setDireccion(cliente.getDireccion());
        dto.setCreatedAt(cliente.getCreatedAt());
        return dto;
    }

    private Cliente convertirAEntidad(ClienteDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setId(dto.getId());
        cliente.setNombre(dto.getNombre());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
        cliente.setDocumento(dto.getDocumento());
        cliente.setDireccion(dto.getDireccion());
        cliente.setCreatedAt(dto.getCreatedAt());
        return cliente;
    }
}

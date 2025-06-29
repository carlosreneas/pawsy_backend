package com.web.project.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.web.project.entity.Mascota;
import com.web.project.entity.TipoMascota;
import com.web.project.entity.Usuario;
import com.web.project.entity.dto.MascotaDTO;
import com.web.project.repository.MascotaRepository;
import com.web.project.repository.TipoMascotaRepository;
import com.web.project.repository.UsuarioRepository;

@Service
public class MascotaService {
	private final MascotaRepository mascotaRepository;
	private final UsuarioRepository usuarioRepository;
	private final TipoMascotaRepository tipoMascotaRepository;
	
	public MascotaService(MascotaRepository mascotaRepository, UsuarioRepository usuarioRepository, TipoMascotaRepository tipoMascotaRepository) {
		this.mascotaRepository = mascotaRepository;
		this.usuarioRepository = usuarioRepository;
		this.tipoMascotaRepository = tipoMascotaRepository;
	}
	
	public List<MascotaDTO> listAll(){
		return mascotaRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
	}

    public List<MascotaDTO> listarPorUsuario(Integer clienteId) {
        // Verifica si el usuario existe
        if (!usuarioRepository.existsById(clienteId)) {
            throw new NoSuchElementException("El cliente con ID " + clienteId + " no existe.");
        }

        return mascotaRepository.findByUsuarioId(clienteId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    public MascotaDTO create(MascotaDTO dto) {
        if (dto.getClienteId() == null) {
            throw new IllegalArgumentException("El campo clienteId no puede ser nulo.");
        }

        Usuario cliente = usuarioRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new NoSuchElementException("El cliente con ID " + dto.getClienteId() + " no existe."));

        TipoMascota tipo = null;
        if (dto.getTipoId() != null) {
            tipo = tipoMascotaRepository.findById(dto.getTipoId())
                    .orElseThrow(() -> new NoSuchElementException("El tipo de mascota con ID " + dto.getTipoId() + " no existe."));
        }

        Mascota mascota = new Mascota();
        mascota.setNombre(dto.getNombre());
        mascota.setEdad(dto.getEdad());
        mascota.setDescripcion(dto.getDescripcion());
        mascota.setUsuario(cliente);
        mascota.setTipoMascota(tipo);

        return toDTO(mascotaRepository.save(mascota));
    }

    public MascotaDTO update(Integer id, MascotaDTO dto) {
        Mascota mascota = mascotaRepository.findById(id).orElseThrow();

        mascota.setNombre(dto.getNombre());
        mascota.setEdad(dto.getEdad());
        mascota.setDescripcion(dto.getDescripcion());

        if (dto.getClienteId() != null) {
            Usuario cliente = usuarioRepository.findById(dto.getClienteId()).orElseThrow();
            mascota.setUsuario(cliente);
        }

        if (dto.getTipoId() != null) {
            TipoMascota tipo = tipoMascotaRepository.findById(dto.getTipoId()).orElse(null);
            mascota.setTipoMascota(tipo);
        }

        return toDTO(mascotaRepository.save(mascota));
    }
	
	public void delete(Integer id) {
        mascotaRepository.deleteById(id);
    }
	
	
	//Convertir la entidad Mascota en un objeto MascotaDTO
	private MascotaDTO toDTO(Mascota mascota) {
        return MascotaDTO.builder()
                .id(mascota.getId())
                .nombre(mascota.getNombre())
                .descripcion(mascota.getDescripcion())
                .edad(mascota.getEdad())
                .clienteId(mascota.getUsuario().getId())
                .tipoId(mascota.getTipoMascota() != null ? mascota.getTipoMascota().getId() : null)
                .build();
    }


    public MascotaDTO findById(Integer id) {
        Mascota mascota = mascotaRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("El mascota con ID " + id + " no existe."));
        return toDTO(mascota);
    }
}

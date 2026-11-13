package com.devgear.ms_recomendaciones.service;

import com.devgear.ms_recomendaciones.client.ProductoClient;
import com.devgear.ms_recomendaciones.dto.*;
import com.devgear.ms_recomendaciones.repository.EventoNavegacionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecomendacionService {

    private static final int MAX_RECOMENDACIONES = 6;
    private static final int MAX_CATEGORIAS = 3;

    private final EventoNavegacionRepository repository;
    private final ProductoClient productoClient;

    public RecomendacionService(EventoNavegacionRepository repository, ProductoClient productoClient) {
        this.repository = repository;
        this.productoClient = productoClient;
    }

    public RecomendacionesResponseDTO recomendar(String usuarioId, String bearerToken) {
        List<String> favoritas = repository.afinidadPorCategoria(usuarioId, PageRequest.of(0, MAX_CATEGORIAS))
                .stream().map(fila -> (String) fila[0]).toList();

        Map<Long, Long> popularidad = new HashMap<>();
        for (Object[] fila : repository.popularidadProductos(PageRequest.of(0, 500))) {
            popularidad.put((Long) fila[0], ((Number) fila[1]).longValue());
        }

        List<ProductoDTO> disponibles = productoClient.listar(bearerToken).stream()
                .filter(p -> !Boolean.FALSE.equals(p.activo()) && p.stock() != null && p.stock() > 0)
                .toList();

        List<RecomendacionDTO> resultado = new ArrayList<>();
        if (!favoritas.isEmpty()) {
            // Primero la categoría favorita, luego la segunda, etc.; dentro de cada una, los más populares.
            for (String categoria : favoritas) {
                disponibles.stream()
                        .filter(p -> categoria.equals(p.categoria()))
                        .sorted(porPopularidad(popularidad))
                        .forEach(p -> resultado.add(aRecomendacion(p, "Porque te interesa " + categoria)));
            }
        }
        if (resultado.size() < MAX_RECOMENDACIONES) {
            Set<Long> yaIncluidos = resultado.stream().map(RecomendacionDTO::productoId).collect(Collectors.toSet());
            disponibles.stream()
                    .filter(p -> !yaIncluidos.contains(p.id()))
                    .sorted(porPopularidad(popularidad))
                    .forEach(p -> resultado.add(aRecomendacion(p, "Popular entre los clientes")));
        }
        List<RecomendacionDTO> top = resultado.stream().limit(MAX_RECOMENDACIONES).toList();
        return new RecomendacionesResponseDTO(favoritas, top);
    }

    public MetricasDTO metricas() {
        Map<String, Long> porTipo = new LinkedHashMap<>();
        long total = 0;
        for (Object[] fila : repository.contarPorTipo()) {
            long n = ((Number) fila[1]).longValue();
            porTipo.put((String) fila[0], n);
            total += n;
        }
        List<MetricasDTO.Conteo> categorias = repository.contarPorCategoria(PageRequest.of(0, 5)).stream()
                .map(f -> new MetricasDTO.Conteo((String) f[0], ((Number) f[1]).longValue()))
                .toList();
        Map<Integer, Long> porParticion = new LinkedHashMap<>();
        for (Object[] fila : repository.contarPorParticion()) {
            porParticion.put(((Number) fila[0]).intValue(), ((Number) fila[1]).longValue());
        }
        return new MetricasDTO(
                total,
                porTipo,
                repository.countByProcesadoEnAfter(Instant.now().minusSeconds(60)),
                repository.contarUsuariosDistintos(),
                categorias,
                porParticion,
                repository.ultimoProcesado());
    }

    private Comparator<ProductoDTO> porPopularidad(Map<Long, Long> popularidad) {
        return Comparator.<ProductoDTO>comparingLong(p -> popularidad.getOrDefault(p.id(), 0L)).reversed();
    }

    private RecomendacionDTO aRecomendacion(ProductoDTO p, String motivo) {
        return new RecomendacionDTO(p.id(), p.nombre(), p.categoria(), p.precio(), p.imagenUrl(), motivo);
    }
}

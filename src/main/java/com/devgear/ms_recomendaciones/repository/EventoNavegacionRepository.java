package com.devgear.ms_recomendaciones.repository;

import com.devgear.ms_recomendaciones.model.EventoNavegacion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface EventoNavegacionRepository extends JpaRepository<EventoNavegacion, String> {

    // Afinidad del usuario por categoría: ver = 1 punto, agregar al carrito = 3 puntos.
    // Devuelve filas [categoria, puntaje] ordenadas de mayor a menor.
    @Query("""
            select e.categoria, sum(case e.tipo when 'PRODUCTO_VISTO' then 1 when 'ITEM_AGREGADO' then 3 else 0 end)
            from EventoNavegacion e
            where e.usuarioId = :usuarioId and e.categoria is not null
            group by e.categoria
            having sum(case e.tipo when 'PRODUCTO_VISTO' then 1 when 'ITEM_AGREGADO' then 3 else 0 end) > 0
            order by sum(case e.tipo when 'PRODUCTO_VISTO' then 1 when 'ITEM_AGREGADO' then 3 else 0 end) desc
            """)
    List<Object[]> afinidadPorCategoria(@Param("usuarioId") String usuarioId, Pageable pageable);

    // Popularidad global por producto: filas [productoId, puntaje].
    @Query("""
            select e.productoId, sum(case e.tipo when 'PRODUCTO_VISTO' then 1 when 'ITEM_AGREGADO' then 3 else 0 end)
            from EventoNavegacion e
            where e.productoId is not null
            group by e.productoId
            order by sum(case e.tipo when 'PRODUCTO_VISTO' then 1 when 'ITEM_AGREGADO' then 3 else 0 end) desc
            """)
    List<Object[]> popularidadProductos(Pageable pageable);

    // Métricas
    @Query("select e.tipo, count(e) from EventoNavegacion e group by e.tipo")
    List<Object[]> contarPorTipo();

    @Query("select e.categoria, count(e) from EventoNavegacion e where e.categoria is not null group by e.categoria order by count(e) desc")
    List<Object[]> contarPorCategoria(Pageable pageable);

    @Query("select e.particion, count(e) from EventoNavegacion e where e.particion is not null group by e.particion order by e.particion")
    List<Object[]> contarPorParticion();

    long countByProcesadoEnAfter(Instant desde);

    @Query("select count(distinct e.usuarioId) from EventoNavegacion e")
    long contarUsuariosDistintos();

    @Query("select max(e.procesadoEn) from EventoNavegacion e")
    Instant ultimoProcesado();
}

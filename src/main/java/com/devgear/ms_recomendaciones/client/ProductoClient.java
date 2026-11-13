package com.devgear.ms_recomendaciones.client;

import com.devgear.ms_recomendaciones.dto.ProductoDTO;
import com.devgear.ms_recomendaciones.dto.ProductosPageDTO;
import com.devgear.ms_recomendaciones.exception.ServicioProductosException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class ProductoClient {

    private final RestClient restClient;

    public ProductoClient(RestClient productosRestClient) {
        this.restClient = productosRestClient;
    }

    public List<ProductoDTO> listar(String bearerToken) {
        try {
            ProductosPageDTO pagina = restClient.get()
                    .uri("/api/v1/productos?size=200")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .body(ProductosPageDTO.class);
            return pagina == null || pagina.content() == null ? List.of() : pagina.content();
        } catch (RestClientException ex) {
            throw new ServicioProductosException("No se pudo consultar ms-productos: " + ex.getMessage());
        }
    }
}

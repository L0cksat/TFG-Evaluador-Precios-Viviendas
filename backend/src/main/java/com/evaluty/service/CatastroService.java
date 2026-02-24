package com.evaluty.service;

import com.evaluty.dto.CatastroResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.ByteArrayInputStream;

/**
 * Servicio de consulta a la API pública del Catastro español.
 * Endpoint de referencia: https://ovc.catastro.meh.es/OVCServWeb/OVCWcfCallejero/COVCCallejero.svc
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CatastroService {

    @Value("${evaluty.catastro.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    /**
     * Consulta los datos básicos de un inmueble por número de catastro (RC).
     * Nota: La valoración mínima oficial se obtiene del valor catastral.
     * El precio mínimo de transmisión es: Valor Catastral * coeficiente autonómico (aprox. 0.5 - 1.5)
     */
    public CatastroResponse consultarPorReferencia(String referenciaCatastral) {
        log.info("Consultando catastro para RC: {}", referenciaCatastral);

        // URL del servicio de consulta de datos de parcela por RC
        String url = "https://ovc.catastro.meh.es/OVCServWeb/OVCWcfCallejero/COVCCallejero.svc/rest/Consulta_DNPRC?RefCat={rc}";

        try {
            String xmlResponse = restTemplate.getForObject(url, String.class, referenciaCatastral);

            if (xmlResponse == null || xmlResponse.isBlank()) {
                throw new RuntimeException("Sin respuesta del Catastro");
            }

            return parseRespuestaCatastro(xmlResponse, referenciaCatastral);

        } catch (Exception e) {
            log.error("Error consultando catastro para {}: {}", referenciaCatastral, e.getMessage());
            throw new RuntimeException("No se pudo consultar el catastro: " + e.getMessage());
        }
    }

    private CatastroResponse parseRespuestaCatastro(String xml, String referenciaCatastral) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Seguridad: deshabilitar entidades externas
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
            doc.getDocumentElement().normalize();

            // Extraer campos del XML de catastro
            String direccion = getTagValue("ldt", doc);
            String municipio  = getTagValue("nm", doc);
            String uso        = getTagValue("cn", doc);
            String superficie = getTagValue("sfc", doc);

            // Valor catastral (base para calcular mínimo de transmisión)
            String valorCatastralStr = getTagValue("vcat", doc);
            double valorCatastral = 0;
            if (valorCatastralStr != null && !valorCatastralStr.isBlank()) {
                valorCatastral = Double.parseDouble(valorCatastralStr.trim());
            }

            // Valor mínimo de transmisión (según art. 57.1.b LGT: valor catastral * coeficiente)
            // Usamos un coeficiente conservador de 1.0 como aproximación
            // En producción deberías consultar el coeficiente de la comunidad autónoma
            double valoracionMinima = valorCatastral;

            String avisoFiscal = generarAvisoFiscal(valoracionMinima);

            return CatastroResponse.builder()
                    .numeroCatastro(referenciaCatastral)
                    .direccion(direccion != null ? direccion : "No disponible")
                    .municipio(municipio != null ? municipio : "No disponible")
                    .uso(uso != null ? uso : "No disponible")
                    .superficieConstruida(superficie != null ? Double.parseDouble(superficie.trim()) : null)
                    .valoracionMinima(valoracionMinima)
                    .avisoFiscal(avisoFiscal)
                    .build();

        } catch (Exception e) {
            log.error("Error parseando XML del Catastro", e);
            throw new RuntimeException("Error interpretando la respuesta del Catastro");
        }
    }

    private String getTagValue(String tag, Document doc) {
        NodeList nodes = doc.getElementsByTagName(tag);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    private String generarAvisoFiscal(double valoracionMinima) {
        if (valoracionMinima <= 0) return null;

        return String.format(
            "⚠️ AVISO FISCAL: El valor mínimo de transmisión para este inmueble " +
            "es de %.2f €. Vender por debajo de este precio puede implicar " +
            "una comprobación de valores por parte de Hacienda (art. 57 LGT). " +
            "Consulte con un asesor fiscal antes de formalizar la operación.",
            valoracionMinima
        );
    }
}

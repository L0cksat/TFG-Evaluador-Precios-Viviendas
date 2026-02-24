package com.evaluty.service;

import com.evaluty.model.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfService {

    private static final Color COLOR_PRIMARIO   = new Color(22, 78, 138);   // Azul corporativo
    private static final Color COLOR_SECUNDARIO = new Color(234, 246, 255); // Azul muy claro
    private static final Color COLOR_AVISO      = new Color(255, 243, 205); // Amarillo suave
    private static final Color COLOR_AVISO_BORDE = new Color(204, 147, 0);
    private static final NumberFormat FORMATO_EUROS = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

    public byte[] generarInforme(Valoracion valoracion) {
        log.info("Generando PDF para valoración {}", valoracion.getId());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 70, 70);
            PdfWriter writer = PdfWriter.getInstance(document, out);

            document.open();
            addMetadata(document, valoracion);
            addHeader(document, writer);
            addDatosVivienda(document, valoracion);
            addResultadoValoracion(document, valoracion);

            if (valoracion.getAvisoFiscal() != null) {
                addAvisoFiscal(document, valoracion.getAvisoFiscal());
            }

            if (valoracion.getComparables() != null && !valoracion.getComparables().isEmpty()) {
                addComparables(document, valoracion.getComparables());
            }

            addPieDePageFooter(document, writer);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            log.error("Error generando el PDF", e);
            throw new RuntimeException("No se pudo generar el informe PDF", e);
        }
    }

    private void addMetadata(Document doc, Valoracion v) {
        doc.addTitle("Informe de Valoración - Evaluty");
        doc.addAuthor("Evaluty");
        doc.addSubject("Valoración inmobiliaria: " + v.getDireccion());
        doc.addCreationDate();
    }

    private void addHeader(Document doc, PdfWriter writer) throws DocumentException {
        // Barra superior de color
        PdfContentByte canvas = writer.getDirectContent();
        canvas.setColorFill(COLOR_PRIMARIO);
        canvas.rectangle(36, doc.top() + 20, doc.right() - doc.left(), 8);
        canvas.fill();

        // Título
        Font fontTitulo = new Font(Font.HELVETICA, 22, Font.BOLD, COLOR_PRIMARIO);
        Paragraph titulo = new Paragraph("INFORME DE VALORACIÓN INMOBILIARIA", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(4);
        doc.add(titulo);

        Font fontSub = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
        Paragraph sub = new Paragraph("Generado por Evaluty · Estimación orientativa", fontSub);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(20);
        doc.add(sub);

        addLineSeparator(doc);
    }

    private void addDatosVivienda(Document doc, Valoracion v) throws DocumentException {
        Font fontSeccion = new Font(Font.HELVETICA, 13, Font.BOLD, COLOR_PRIMARIO);
        doc.add(new Paragraph("Datos de la vivienda consultada", fontSeccion));
        doc.add(Chunk.NEWLINE);

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{1, 2});

        addFilaTabla(tabla, "Dirección", v.getDireccion());
        addFilaTabla(tabla, "Metros cuadrados", v.getMetrosCuadrados() + " m²");
        addFilaTabla(tabla, "Habitaciones", String.valueOf(v.getHabitaciones()));
        if (v.getNumeroCatastro() != null) {
            addFilaTabla(tabla, "Ref. Catastral", v.getNumeroCatastro());
        }
        if (v.getFechaCreacion() != null) {
            addFilaTabla(tabla, "Fecha consulta",
                    v.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }

        doc.add(tabla);
        doc.add(Chunk.NEWLINE);
    }

    private void addResultadoValoracion(Document doc, Valoracion v) throws DocumentException {
        Font fontSeccion = new Font(Font.HELVETICA, 13, Font.BOLD, COLOR_PRIMARIO);
        doc.add(new Paragraph("Resultado de la valoración", fontSeccion));
        doc.add(Chunk.NEWLINE);

        // Cuadro destacado con el precio estimado
        PdfPTable cuadro = new PdfPTable(1);
        cuadro.setWidthPercentage(60);
        cuadro.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_SECUNDARIO);
        cell.setBorderColor(COLOR_PRIMARIO);
        cell.setBorderWidth(1.5f);
        cell.setPadding(16);

        Font fontPrecio = new Font(Font.HELVETICA, 26, Font.BOLD, COLOR_PRIMARIO);
        Paragraph precio = new Paragraph(FORMATO_EUROS.format(v.getPrecioEstimado()), fontPrecio);
        precio.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(precio);

        Font fontLabel = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.GRAY);
        Paragraph label = new Paragraph("PRECIO ESTIMADO", fontLabel);
        label.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(label);

        cuadro.addCell(cell);
        doc.add(cuadro);
        doc.add(Chunk.NEWLINE);

        // Métricas secundarias
        PdfPTable metricas = new PdfPTable(3);
        metricas.setWidthPercentage(100);

        addMetricaCell(metricas, FORMATO_EUROS.format(v.getPrecioPorMetroCuadrado()), "Precio / m²");
        addMetricaCell(metricas, v.getRangoConfianza() != null ? v.getRangoConfianza() : "-", "Rango de valores");
        addMetricaCell(metricas,
                v.getComparables() != null ? String.valueOf(v.getComparables().size()) : "0",
                "Comparables usados");

        doc.add(metricas);
        doc.add(Chunk.NEWLINE);

        // Valoración mínima catastro (si existe)
        if (v.getValoracionMinimaOficial() != null) {
            PdfPTable catastro = new PdfPTable(2);
            catastro.setWidthPercentage(100);
            catastro.setWidths(new float[]{1, 2});
            addFilaTabla(catastro, "Valor mínimo catastro",
                    FORMATO_EUROS.format(v.getValoracionMinimaOficial()));
            doc.add(catastro);
            doc.add(Chunk.NEWLINE);
        }
    }

    private void addAvisoFiscal(Document doc, String aviso) throws DocumentException {
        Font fontSeccion = new Font(Font.HELVETICA, 13, Font.BOLD, new Color(153, 100, 0));
        doc.add(new Paragraph("Aviso fiscal", fontSeccion));
        doc.add(Chunk.NEWLINE);

        PdfPTable tabla = new PdfPTable(1);
        tabla.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_AVISO);
        cell.setBorderColor(COLOR_AVISO_BORDE);
        cell.setBorderWidth(1.5f);
        cell.setPadding(12);

        Font fontAviso = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(100, 60, 0));
        cell.addElement(new Paragraph(aviso, fontAviso));
        tabla.addCell(cell);
        doc.add(tabla);
        doc.add(Chunk.NEWLINE);
    }

    private void addComparables(Document doc, List<PropiedadComparable> comparables) throws DocumentException {
        addLineSeparator(doc);
        Font fontSeccion = new Font(Font.HELVETICA, 13, Font.BOLD, COLOR_PRIMARIO);
        doc.add(new Paragraph("Propiedades comparables analizadas", fontSeccion));
        doc.add(Chunk.NEWLINE);

        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{3, 2, 1, 1, 2});

        // Cabecera
        String[] cabeceras = {"Título", "Ubicación", "m²", "Hab.", "Precio"};
        for (String cab : cabeceras) {
            PdfPCell header = new PdfPCell(new Phrase(cab,
                    new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE)));
            header.setBackgroundColor(COLOR_PRIMARIO);
            header.setPadding(6);
            tabla.addCell(header);
        }

        // Filas
        boolean par = false;
        for (PropiedadComparable p : comparables) {
            Color fondo = par ? COLOR_SECUNDARIO : Color.WHITE;
            addFilaComparable(tabla, p, fondo);
            par = !par;
        }

        doc.add(tabla);
    }

    private void addPieDePageFooter(Document doc, PdfWriter writer) {
        PdfContentByte canvas = writer.getDirectContentUnder();
        Font fontFooter = new Font(Font.HELVETICA, 7, Font.ITALIC, Color.GRAY);
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase("Este informe es una estimación orientativa generada automáticamente. " +
                        "No constituye tasación oficial ni asesoramiento fiscal. " +
                        "Consulte con un profesional antes de tomar decisiones.", fontFooter),
                doc.getPageSize().getWidth() / 2, 30, 0);
    }

    // ---- Helpers ----

    private void addFilaTabla(PdfPTable tabla, String label, String valor) {
        Font fontLabel = new Font(Font.HELVETICA, 9, Font.BOLD, COLOR_PRIMARIO);
        Font fontValor = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);

        PdfPCell cLabel = new PdfPCell(new Phrase(label, fontLabel));
        cLabel.setBackgroundColor(COLOR_SECUNDARIO);
        cLabel.setPadding(6);
        cLabel.setBorderColor(Color.LIGHT_GRAY);

        PdfPCell cValor = new PdfPCell(new Phrase(valor, fontValor));
        cValor.setPadding(6);
        cValor.setBorderColor(Color.LIGHT_GRAY);

        tabla.addCell(cLabel);
        tabla.addCell(cValor);
    }

    private void addMetricaCell(PdfPTable tabla, String valor, String label) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_SECUNDARIO);
        cell.setBorderColor(Color.LIGHT_GRAY);
        cell.setPadding(10);

        Font fontV = new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_PRIMARIO);
        Font fontL = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY);
        Paragraph p = new Paragraph(valor, fontV);
        p.setAlignment(Element.ALIGN_CENTER);
        Paragraph l = new Paragraph(label, fontL);
        l.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(p);
        cell.addElement(l);
        tabla.addCell(cell);
    }

    private void addFilaComparable(PdfPTable tabla, PropiedadComparable p, Color fondo) {
        Font font = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);
        String[] valores = {
            p.getTitulo() != null ? p.getTitulo() : "-",
            p.getUbicacion() != null ? p.getUbicacion() : "-",
            p.getMetrosCuadrados() != null ? p.getMetrosCuadrados() + " m²" : "-",
            p.getHabitaciones() != null ? String.valueOf(p.getHabitaciones()) : "-",
            p.getPrecio() != null ? FORMATO_EUROS.format(p.getPrecio()) : "-"
        };
        for (String v : valores) {
            PdfPCell c = new PdfPCell(new Phrase(v, font));
            c.setBackgroundColor(fondo);
            c.setPadding(5);
            c.setBorderColor(Color.LIGHT_GRAY);
            tabla.addCell(c);
        }
    }

    private void addLineSeparator(Document doc) throws DocumentException {
        LineSeparator line = new LineSeparator();
        line.setLineColor(COLOR_PRIMARIO);
        line.setLineWidth(0.5f);
        doc.add(new Chunk(line));
        doc.add(Chunk.NEWLINE);
    }
}

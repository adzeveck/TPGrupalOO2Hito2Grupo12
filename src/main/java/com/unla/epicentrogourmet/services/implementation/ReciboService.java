package com.unla.epicentrogourmet.services.implementation;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.unla.epicentrogourmet.dtos.LiquidacionHaberesDTO;
import com.unla.epicentrogourmet.services.IReciboService;

// No calcula nada: el recibo imprime el LiquidacionHaberesDTO que ya trae todos los conceptos y montos
@Service("reciboService")
public class ReciboService implements IReciboService {

    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.GRAY);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 11);
    private static final Font NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font PIE = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public byte[] generarPdf(LiquidacionHaberesDTO l) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            Paragraph titulo = new Paragraph("Recibo de sueldo", TITULO);
            titulo.setSpacingAfter(4);
            documento.add(titulo);
            Paragraph subtitulo = new Paragraph(l.getFestival() + " · " + l.getUnidad(), SUBTITULO);
            subtitulo.setSpacingAfter(16);
            documento.add(subtitulo);

            // Datos del empleado
            PdfPTable datos = new PdfPTable(2);
            datos.setWidthPercentage(100);
            datos.setWidths(new float[] { 30, 70 });
            datos.setSpacingAfter(16);
            fila(datos, "Empleado", l.getEmpleado());
            fila(datos, "DNI", l.getDni());
            fila(datos, "Puesto", l.getPuesto());
            fila(datos, "Período", l.getFechaInicio().format(FECHA) + " al " + l.getFechaFin().format(FECHA)
                    + " (" + l.getDias() + " días)");
            documento.add(datos);

            // Conceptos y montos
            PdfPTable conceptos = new PdfPTable(2);
            conceptos.setWidthPercentage(100);
            conceptos.setWidths(new float[] { 70, 30 });
            encabezado(conceptos, "Concepto", "Monto");
            concepto(conceptos, "Sueldo base", moneda(l.getSueldoBase()), NORMAL);
            concepto(conceptos, l.getConceptoAdicional(), moneda(l.getAdicional()), NORMAL);
            concepto(conceptos, "Subtotal", moneda(l.getSubtotal()), NEGRITA);
            concepto(conceptos, "Liquidación por " + l.getDias() + " días: " + l.getPorcentaje()
                    + "% del subtotal", "", NORMAL);
            concepto(conceptos, "Total a cobrar", moneda(l.getTotal()), NEGRITA);
            documento.add(conceptos);

            Paragraph pie = new Paragraph("Emitido el " + LocalDate.now().format(FECHA), PIE);
            pie.setSpacingBefore(20);
            documento.add(pie);

            documento.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el recibo de sueldo", e);
        }
        return salida.toByteArray();
    }

    private void fila(PdfPTable tabla, String etiqueta, String valor) {
        tabla.addCell(celda(etiqueta, NEGRITA, Element.ALIGN_LEFT, null));
        tabla.addCell(celda(valor, NORMAL, Element.ALIGN_LEFT, null));
    }

    private void encabezado(PdfPTable tabla, String izquierda, String derecha) {
        tabla.addCell(celda(izquierda, NEGRITA, Element.ALIGN_LEFT, Color.LIGHT_GRAY));
        tabla.addCell(celda(derecha, NEGRITA, Element.ALIGN_RIGHT, Color.LIGHT_GRAY));
    }

    private void concepto(PdfPTable tabla, String texto, String monto, Font fuente) {
        tabla.addCell(celda(texto, fuente, Element.ALIGN_LEFT, null));
        tabla.addCell(celda(monto, fuente, Element.ALIGN_RIGHT, null));
    }

    private PdfPCell celda(String texto, Font fuente, int alineacion, Color fondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(6);
        if (fondo != null) {
            celda.setBackgroundColor(fondo);
        }
        return celda;
    }

    // Formato argentino: $ 230.000,00
    private String moneda(BigDecimal monto) {
        DecimalFormat formato = new DecimalFormat("#,##0.00",
                DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-AR")));
        return "$ " + formato.format(monto);
    }
}

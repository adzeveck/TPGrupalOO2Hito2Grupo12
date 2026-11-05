package com.unla.epicentrogourmet.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Histórico: lo que se le liquidó a un empleado por un tramo de días. Se llega desde la unidad de venta
// (no desde el festival). No guarda quién la calculó. Como Pedido, el hijo es dueño de las FK
@Entity
@Getter @Setter @NoArgsConstructor
public class LiquidacionHaberes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Personal empleado;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidad_id", nullable = false)
    private UnidadDeVenta unidad;
    // Día en que se cerró (se confirmó) la liquidación
    @Column(nullable = false)
    private LocalDate fecha;
    // Tramo liquidado, ambos días incluidos
    @Column(nullable = false)
    private LocalDate desde;
    @Column(nullable = false)
    private LocalDate hasta;
    @Column(nullable = false)
    private Integer dias;
    // Categoría o rol al momento de liquidar
    @Column(nullable = false, length = 50)
    private String puesto;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sueldoBase;
    // El plus (categoría o antigüedad) al momento de liquidar
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal adicional;
    // Cómo se llamaba el adicional al liquidar: "Plus por categoría" o "Antigüedad (3 años)". Es nullable
    // para que la columna se pueda agregar aunque ya haya filas
    @Column(length = 60)
    private String conceptoAdicional;

    // Para el recibo: con cuántos días acumulados se aplicó la escala, lo que le correspondía en total y lo ya
    // pagado en cierres anteriores. "porcentaje" es el de esos días acumulados y "total", lo pagado en este cierre.
    // Nullable: las liquidaciones viejas no los tienen
    @Column
    private Integer diasAcumulados;
    @Column(precision = 12, scale = 2)
    private BigDecimal corresponde;
    @Column(precision = 12, scale = 2)
    private BigDecimal yaPagado;

    @Column(nullable = false)
    private Integer porcentaje;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    // Cuenta del histórico: lo que se liquidó antes de aplicar el porcentaje
    public BigDecimal calcularSubtotal() {
        return sueldoBase.add(adicional);
    }

}
package com.rota.entity;

/**
 * HU-23: causas válidas de una baja. Exigir una causa evita que la pérdida
 * se sume a la "merma desconocida".
 */
public enum CausaMerma {
    VENCIMIENTO("Vencimiento"),
    DANIO("Daño"),
    ROTURA("Rotura");

    private final String etiqueta;

    CausaMerma(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}

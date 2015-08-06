package io.corbel.resources.rem.format;

public enum ImageFormat {
    JPG, JPEG, TIFF, PNG, GIF, BMP, ARW, SRF, SR2, BAY, CRW, CR2, CAP, TIF, IIQ, EIP, DCS, DCR, DRF, K25, KDC, DNG, ERF, FFF, MEF, MOS, MRW, NEF, NRW, ORF, PTX, PEF, PXN, R3D, RAF, RAW, RW2, RWL, RWZ, X3F;

    public static ImageFormat safeValueOf(String value) {
        return value != null ? valueOf(value.toUpperCase()) : null;
    }
}

package Geymsla.unusedClasses;

public enum NoteNames {
    C(0),  C_SHARP(1), D_FLAT(1), D(2), D_SHARP(3), E_FLAT(3),
    E(4),  F_FLAT(4), E_SHARP(5), F(5), F_SHARP(6), G_FLAT(6),
    G(7), G_SHARP(8), A_FLAT(8), A(9), A_SHARP(10), B_FLAT(10),
    B(11), C_FLAT(11), B_SHARP(12);

    private final int keyIndex;

    NoteNames(int keyIndex) {
        this.keyIndex = keyIndex;
    }

    public int getKeyIndex() {
        return keyIndex;
    }
}


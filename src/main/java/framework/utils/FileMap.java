package framework.utils;

public class FileMap {
 
    private String nomFichier;
    private String extension;
    private byte[] bin;


    public void afficher(){
        System.out.println("FICHIER : " + this.nomFichier);
        System.out.println("EXTENSION : " + this.extension);
        System.out.println("BIN : " + this.bin.length);

    }
    public String getNomFichier() {
        return nomFichier;
    }
    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }
    public String getExtension() {
        return extension;
    }
    public void setExtension(String extension) {
        this.extension = extension;
    }
    public byte[] getBin() {
        return bin;
    }
    public void setBin(byte[] bin) {
        this.bin = bin;
    }
    
    
}

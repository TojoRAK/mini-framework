package framework.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    public static Path sauvegarderFichier(FileMap fichier, String path) throws IOException {

        Path dossier = Paths
                .get(path)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(dossier);

        String nom = Paths.get(fichier.getNomFichier())
                .getFileName()
                .toString();

        if (nom.isBlank()) {
            throw new IOException("Nom de fichier invalide");
        }

        Path destination = dossier.resolve(nom).normalize();

        if (!destination.startsWith(dossier)) {
            throw new IOException("Chemin invalide");
        }

        Files.write(destination, fichier.getBin());

        return destination;
    }
}

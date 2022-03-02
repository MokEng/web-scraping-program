import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Allows you to load and save Sitemap-objects to file
 */
public class SitemapHandler {

    /**
     * Fetches all sitemap objects within a given directory
     * @param dirName is the name of the directory to fetch sitemaps from
     * @param failedFiles will be filled with file paths that was invalid or the file was broken
     * @return a list of sitemap-objects
     */
    public static List<Sitemap> loadSitemaps(String dirName,List<Path> failedFiles){
        List<Sitemap> sitemapList = new ArrayList<>();
        try {
            Stream<Path> paths = Files.walk(Paths.get(dirName)); // Find paths in directory
            paths
                    .filter(Files::isRegularFile) // for only regular files
                    .forEach(filePath->{
                        ObjectInputStream ois;
                        // recreate sitemap from file
                        try {
                            ois = new ObjectInputStream(new FileInputStream(filePath.toFile()));
                            Sitemap s = (Sitemap) ois.readObject();
                            sitemapList.add(s);
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                            failedFiles.add(filePath);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sitemapList;
    }

    /**
     * Saves sitemap-objects on separate files given a directory
     * @param dirname is the name of the directory that the files are saved in
     * @param sitemaps is the actual objects to save, each in their own file
     * @return true if all saves where successful
     */
    public static boolean saveSitemaps(String dirname,List<Sitemap> sitemaps){
        AtomicBoolean success= new AtomicBoolean(true);
        sitemaps.forEach(sitemap -> {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dirname+sitemap.getName()));
                oos.writeObject(sitemap);
                oos.flush();
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
                success.set(false);
            }
        });
        return success.get();
    }

}

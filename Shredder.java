import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Shredder {

    private static final int BUFFER_SIZE = 8192; // 8 KB chunk buffer

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Error: Please provide a filename or directory path.");
            System.out.println("Usage: java Shredder <path-to-file-or-directory>");
            System.exit(1);
        }

        Path targetPath = Paths.get(args[0]);

        if (!Files.exists(targetPath)) {
            System.err.println("Error: Path '" + targetPath + "' does not exist.");
            System.exit(1);
        }

        shredPath(targetPath.toFile());
        System.out.println("Shredding operation completed successfully.");
    }

    /**
     * Recursively processes directories or passes individual files to the shredder.
     */
    private static void shredPath(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    shredPath(child);
                }
            }
            // Delete directory after all contents inside are shredded
            if (file.delete()) {
                System.out.println("Deleted Directory: " + file.getAbsolutePath());
            } else {
                System.err.println("Failed to delete directory: " + file.getAbsolutePath());
            }
        } else if (file.isFile()) {
            shredFile(file);
        }
    }

    /**
     * Overwrites file content with zeroes in chunks and then deletes the file.
     */
    private static void shredFile(File file) {
        if (!file.canWrite()) {
            System.err.println("Skipping (read-only or permission denied): " + file.getAbsolutePath());
            return;
        }

        long fileLength = file.length();
        byte[] zeroBuffer = new byte[BUFFER_SIZE];
        Arrays.fill(zeroBuffer, (byte) 0);

        try (RandomAccessFile raf = new RandomAccessFile(file, "rws")) {
            long bytesWritten = 0;

            while (bytesWritten < fileLength) {
                int bytesToWrite = (int) Math.min(BUFFER_SIZE, fileLength - bytesWritten);
                raf.write(zeroBuffer, 0, bytesToWrite);
                bytesWritten += bytesToWrite;
            }

            // Ensure physical disk flush before closing stream
            raf.getFD().sync();
            System.out.println("Shredded: " + file.getAbsolutePath() + " (" + fileLength + " bytes zeroed)");

        } catch (Exception e) {
            System.err.println("Error shredding " + file.getAbsolutePath() + ": " + e.getMessage());
            return;
        }

        // Delete the zeroed-out file
        if (!file.delete()) {
            System.err.println("Warning: Overwritten, but failed to delete file: " + file.getAbsolutePath());
        }
    }
}

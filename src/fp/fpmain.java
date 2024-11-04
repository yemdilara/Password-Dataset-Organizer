package fp;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;

public class fpmain {

    public static List<String> loadPasswords(String filePath) {
        List<String> passwords = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String password = line.trim();
                if (!password.isEmpty()) {
                    passwords.add(password);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return passwords;
    }

    public static String generateHash(String inputStr, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] encodedHash = digest.digest(inputStr.getBytes(StandardCharsets.UTF_8));
            return convertBytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error hashing string: " + e.getMessage());
            return null;
        }
    }

    private static String convertBytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void savePasswords(List<String> passwords, String indexDirectory, String sourceFileName) {
        for (String password : passwords) {
            char firstChar = Character.toLowerCase(password.charAt(0));
            if (!Character.isLetterOrDigit(firstChar)) {
                firstChar = 'a'; 
            }

            String folderPath = indexDirectory + File.separator + firstChar;
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String filePath = folderPath + File.separator + firstChar + "_passwords.txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                String md5Hash = generateHash(password, "MD5");
                String sha1Hash = generateHash(password, "SHA-1");
                String sha256Hash = generateHash(password, "SHA-256");
                writer.write(password + "|" + md5Hash + "|" + sha1Hash + "|" + sha256Hash + "|" + sourceFileName + "\n");
            } catch (IOException e) {
                System.err.println("Error writing file: " + e.getMessage());
            }
        }
    }

    public static void relocateProcessedFile(String source, String destination) {
        File sourceFile = new File(source);
        File destFile = new File(destination);
        try {
            Files.move(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error moving file: " + e.getMessage());
        }
    }

    public static List<String[]> searchPassword(String query, String indexDirectory) {
        List<String[]> passwords = new ArrayList<>();
        char firstChar = Character.toLowerCase(query.charAt(0));
        if (!Character.isLetterOrDigit(firstChar)) {
            firstChar = 'a';
        }

        String folderPath = indexDirectory + File.separator + firstChar;
        File folder = new File(folderPath);
        if (folder.exists()) {
            File[] filesInFolder = folder.listFiles();
            if (filesInFolder != null) {
                for (File file : filesInFolder) {
                    if (file.isFile()) {
                        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                String[] passwordData = line.split("\\|");
                                if (passwordData.length >= 5 && passwordData[0].equals(query)) {
                                    passwords.add(passwordData);
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Error reading file: " + e.getMessage());
                        }
                    }
                }
            }
        }

        if (passwords.isEmpty() && folder.exists()) {
            File[] filesInFolder = folder.listFiles();
            if (filesInFolder != null && filesInFolder.length > 0) {
                File lastFile = filesInFolder[filesInFolder.length - 1];
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(lastFile, true))) {
                    String md5Hash = generateHash(query, "MD5");
                    String sha1Hash = generateHash(query, "SHA-1");
                    String sha256Hash = generateHash(query, "SHA-256");
                    writer.write(query + "|" + md5Hash + "|" + sha1Hash + "|" + sha256Hash + "|search\n");
                } catch (IOException e) {
                    System.err.println("Error writing file: " + e.getMessage());
                }
            }
        }
        return passwords;
    }

    public static void handlePasswordFiles(String unprocessedDirectory, String processedDirectory, String indexDirectory) {
        long startTime = System.currentTimeMillis();
        File unprocessedDir = new File(unprocessedDirectory);
        if (unprocessedDir.exists() && unprocessedDir.isDirectory()) {
            File[] files = unprocessedDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        List<String> passwords = loadPasswords(file.getAbsolutePath());
                        savePasswords(passwords, indexDirectory, file.getName());
                        String destination = processedDirectory + File.separator + file.getName();
                        relocateProcessedFile(file.getAbsolutePath(), destination);
                    }
                }
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken to process files: " + (endTime - startTime) + " milliseconds");
    }

    public static void divideLargeFiles(String indexDirectory) {
        long startTime = System.currentTimeMillis();
        File indexDir = new File(indexDirectory);
        if (indexDir.exists() && indexDir.isDirectory()) {
            File[] subFolders = indexDir.listFiles();
            if (subFolders != null) {
                for (File subFolder : subFolders) {
                    if (subFolder.isDirectory()) {
                        File[] files = subFolder.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if (file.isFile()) {
                                    try {
                                        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                                        int totalLines = lines.size();
                                        int numFiles = (totalLines + 9999) / 10000;
                                        if (numFiles > 1) {
                                            for (int i = 0; i < numFiles; i++) {
                                                int startIndex = i * 10000;
                                                int endIndex = Math.min((i + 1) * 10000, totalLines);
                                                List<String> sublist = lines.subList(startIndex, endIndex);
                                                String newFilePath = file.getAbsolutePath().replace(".txt", "_" + (i + 1) + ".txt");
                                                Files.write(Paths.get(newFilePath), sublist, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                                            }
                                            Files.deleteIfExists(file.toPath());
                                        }
                                    } catch (IOException e) {
                                        System.err.println("Error splitting file: " + e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken to split large files: " + (endTime - startTime) + " milliseconds");
    }

    public static double evaluateSearchPerformance(String indexDirectory) {
        String[] samplePasswords = {"pass1", "pass2", "pass3", "pass4", "pass5", "pass6", "pass7", "pass8", "pass9", "pass10"};
        long totalSearchTime = 0;
        for (String password : samplePasswords) {
            long startTime = System.currentTimeMillis();
            searchPassword(password, indexDirectory);
            long endTime = System.currentTimeMillis();
            totalSearchTime += endTime - startTime;
        }
        return totalSearchTime / (double) samplePasswords.length;
    }

    public static void main(String[] args) {
        String projectDirectory = "C:/Users/asd/Desktop/lazım";
        String unprocessedDirectory = projectDirectory + "/raw-passwords";
        String processedDirectory = projectDirectory + "/processed-passwords";
        String indexDirectory = projectDirectory + "/indexed-passwords";

        for (String directory : new String[]{unprocessedDirectory, processedDirectory, indexDirectory}) {
            File dir = new File(directory);
            if (!dir.exists() && !dir.mkdirs()) {
                System.err.println("Failed to create directory: " + directory);
                return;
            }
        }

        handlePasswordFiles(unprocessedDirectory, processedDirectory, indexDirectory);
        divideLargeFiles(indexDirectory);
        double avgSearchTime = evaluateSearchPerformance(indexDirectory);

        System.out.println("Average search time for 10 random passwords: " + avgSearchTime + " milliseconds");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a password for searching (Example: 15042003): ");
        String queryPassword = scanner.nextLine().trim();
        long startTime = System.currentTimeMillis();
        List<String[]> searchResult = searchPassword(queryPassword, indexDirectory);
        long endTime = System.currentTimeMillis();
        long searchTime = endTime - startTime;

        if (!searchResult.isEmpty()) {
            System.out.println("Password found:");
            for (String[] passwordData : searchResult) {
                System.out.println(Arrays.toString(passwordData));
            }
        } else {
            System.out.println("Password not found.");
        }

        System.out.println("Time taken to search the entered password: " + searchTime + " milliseconds");
    }
}

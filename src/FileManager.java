import java.io.*;
import java.util.ArrayList;

class FileManager {
    static boolean write(String filePath, String string) {
        if (!new File(filePath).exists()) {
            if (!createFile(filePath)) return false;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(string);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    static ArrayList<String> read(String filePath) {
        ArrayList<String> strings = new ArrayList<>();

        if (!new File(filePath).exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String tmp;

            while ((tmp = br.readLine()) != null) {
                strings.add(tmp);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return strings;
    }

    static boolean erase(String filePath) {
        if (new File(filePath).exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {
                bw.write("");
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    static boolean exist(String filePath) {
        return new File(filePath).exists();
    }

    static boolean rename(String filePath, String newFilePath) {
        File original = new File(filePath);
        File newFile = new File(newFilePath);

        return original.renameTo(newFile);
    }

    private static boolean createFile(String filePath) {
        String[] tmp = filePath.split("\\\\");
        String fileName = tmp[tmp.length - 1];
        String dirName = filePath.substring(0, filePath.length() - fileName.length() - 1);

        new File(dirName).mkdirs();
        try {
            if (!new File(filePath).createNewFile()) return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

package cloud;

import app.AppConfig;

import java.io.*;

public class FileUtil {

    public static CloudFile createFile(String filePath, AccessType accessType) {
        File file = new File(filePath);
        StringBuilder fileData;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            fileData = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                fileData.append(line);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            AppConfig.timestampedErrorPrint(e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            AppConfig.timestampedErrorPrint(e.getMessage());
            throw new RuntimeException(e);
        }

        return new CloudFile(filePath, fileData.toString().getBytes(), accessType);
    }
}

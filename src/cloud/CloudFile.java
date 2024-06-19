package cloud;

import java.io.Serializable;
import java.util.Arrays;

public class CloudFile implements Serializable {

    private static final long serialVersionUID = -5625143284318034900L;
    private String filePath;
    private byte[] fileData;
    private AccessType accessType;

    public CloudFile(String filePath, byte[] fileData, AccessType accessType) {
        this.filePath = filePath;
        this.fileData = fileData;
        this.accessType = accessType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public AccessType getFileType() {
        return accessType;
    }

    public void setFileType(AccessType accessType) {
        this.accessType = accessType;
    }

    public String getFileInfo(){
        int fileSizeInBytes = this.fileData.length;
        double fileSizeInKb = (double) fileSizeInBytes / 1024;  // Convert to Kb (using double for precision)
        return "Path: " + this.getFilePath() + " Size: " + String.format("%.2f Kb", fileSizeInKb) + " Access: " + this.accessType + "\n";
    }
}

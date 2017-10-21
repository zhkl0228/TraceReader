package cn.banny.trace.ui;

class FilenameUtils {

    static String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return "";
        }
        return fileName.substring(index + 1);
    }

    static String getBaseName(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return fileName;
        }
        return fileName.substring(0, index);
    }

}

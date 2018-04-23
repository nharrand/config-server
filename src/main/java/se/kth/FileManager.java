package se.kth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class FileManager {
    File src;
    File dest;
    File errors;

    Map<String, STATUS> configs;

    public enum STATUS {
        DONE,IN_PROGRESS,TO_DO,ERROR
    }


    public FileManager(File src, File dest, File errors) {
        this.src = src;
        this.dest = dest;
        this.errors = errors;
        this.configs = new HashMap<>();
        reload();
    }

    public synchronized void reload() {
        for(String path: listJSONFile(src,src)) {
            File srcFile = new File(src,path);
            if(srcFile.exists() && srcFile.isFile()) {
                File destFile = new File(dest, path);
                if(destFile.exists() && destFile.isFile()) {
                    configs.put(path, STATUS.DONE);
                } else {
                    configs.put(path, STATUS.TO_DO);
                }
            }
        }
    }

    public File getConfig() {
        File res = configs.entrySet().stream()
                .filter(e -> e.getValue() == STATUS.TO_DO)
                .findAny()
                .map(stringSTATUSEntry -> new File(src, stringSTATUSEntry.getKey()))
                .orElse(null);
        if(res != null) {
            configs.put(getPath(res, src), STATUS.IN_PROGRESS);
        }
        return res;
    }

    public void postResult(String config, String res) {
        if(configs.containsKey(config) && configs.get(config) == STATUS.IN_PROGRESS) {
            File f = new File(dest,config);
            createPath(f);
            writeFile(res,f);
            configs.put(config,STATUS.DONE);
        } else {
            File f = new File(errors,config);
            createPath(f);
            writeFile(res,f);
            configs.put(config,STATUS.ERROR);
        }
    }

    public static Set<String> listJSONFile(File dir, File origin) {
        HashSet<String> res = new HashSet<>();
        for(File f : dir.listFiles()) {
            if(f.isDirectory()) {
                res.addAll(listJSONFile(f, origin));
            } else {
                if(f.getName().endsWith(".json")) {
                    res.add(getPath(f,origin));
                }
            }
        }
        return res;
    }

    public static String getPath(File f, File origin) {
        return f.getAbsolutePath().substring(origin.getAbsolutePath().length()+1);
    }

    public static void createPath(File f) {
        File parent = f.getParentFile();
        if (!parent.exists()) parent.mkdirs();
    }

    public static void writeFile(String str, File f) {
        try {
            PrintWriter w = new PrintWriter(f);
            w.print(str);
            w.close();
        } catch (Exception ex) {
            System.err.println("Problem writing " + f.getPath());
            ex.printStackTrace();
        }
    }

    public static String getFileContent(File f) {
        String result = null;
        try {
            InputStream input = new FileInputStream(f);
            result = org.apache.commons.io.IOUtils.toString(input, java.nio.charset.Charset.forName("UTF-8"));
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}

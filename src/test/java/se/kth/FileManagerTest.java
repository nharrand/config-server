package se.kth;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class FileManagerTest {

    File src;
    File dest;
    File errors;

    @Before
    public void init() throws Exception {
        src = new File("src/test/resources/src");
        dest = new File("src/test/resources/dest");
        errors = new File("src/test/resources/errors");
        if(!src.exists()) src.mkdirs();
        if(!dest.exists()) dest.mkdirs();
        if(!errors.exists()) errors.mkdirs();
    }

    @Test
    public void testInit() throws Exception {
        assertTrue(src.exists() && src.isDirectory());
        assertTrue(dest.exists() && dest.isDirectory());
        assertTrue(errors.exists() && errors.isDirectory());

        FileManager fm = new FileManager(src,dest,errors);

        assertTrue(fm.getConfig("0.0.0.0") == null);

    }

    @Test
    public void reload() throws Exception {

        FileManager fm = new FileManager(src,dest,errors);

        File projetA = new File(src, "projetA");
        projetA.mkdir();
        File conf1 = new File(src, "projetA/conf1.json");
        conf1.createNewFile();

        fm.reload();

        assertTrue(fm.getConfig("0.0.0.0").getPath().equals(conf1.getPath()));

        conf1.delete();
        projetA.delete();
    }

    @Test
    public void getConfig() throws Exception {
        File projetB = new File(src, "projetB");
        projetB.mkdir();
        File conf2 = new File(src, "projetB/conf2.json");
        conf2.createNewFile();


        FileManager fm = new FileManager(src,dest,errors);
        assertTrue(fm.getConfig("0.0.0.0").getPath().equals(conf2.getPath()));
        assertTrue(fm.configs.get("projetB/conf2.json").equals(FileManager.STATUS.IN_PROGRESS));


        conf2.delete();

        File projetC = new File(src, "projetC");
        projetC.mkdir();
        File sub = new File(src, "projetC/sub");
        sub.mkdir();
        File conf3 = new File(src, "projetC/sub/conf3.json");
        conf3.createNewFile();
        fm.reload();

        assertTrue(fm.getConfig("0.0.0.0").getPath().equals(conf3.getPath()));
        assertTrue(fm.configs.get("projetC/sub/conf3.json").equals(FileManager.STATUS.IN_PROGRESS));

        conf3.delete();

        File conf4 = new File(src, "projetC/sub/conf4.json");
        conf4.createNewFile();

        File conf5 = new File(src, "projetC/sub/conf5.json");
        conf5.createNewFile();

        File conf6 = new File(src, "projetC/sub/conf6.json");
        conf6.createNewFile();
        fm.reload();
        assertTrue(FileManager.getPath(fm.getConfig("0.0.0.0"),src).startsWith("projetC/sub/"));
        assertTrue(FileManager.getPath(fm.getConfig("0.0.0.0"),src).startsWith("projetC/sub/"));
        assertTrue(FileManager.getPath(fm.getConfig("0.0.0.0"),src).startsWith("projetC/sub/"));
        assertTrue(fm.getConfig("0.0.0.0") == null);

        conf4.delete();
        conf5.delete();
        conf6.delete();
        projetB.delete();
        sub.delete();
        projetC.delete();
    }

    @Test
    public void postResult() throws Exception {
        File projetD = new File(src, "projetD");
        projetD.mkdir();
        File conf7 = new File(src, "projetD/conf7.json");
        conf7.createNewFile();
        File projetE = new File(src, "projetE");
        projetE.mkdir();
        File conf8 = new File(src, "projetE/conf8.json");
        conf8.createNewFile();
        File sub = new File(src, "projetE/sub");
        sub.mkdir();
        File conf9 = new File(src, "projetE/sub/conf9.json");
        conf9.createNewFile();


        FileManager fm = new FileManager(src,dest,errors);

        assertTrue(fm.configs.get("projetD/conf7.json").equals(FileManager.STATUS.TO_DO));
        assertTrue(fm.configs.get("projetE/conf8.json").equals(FileManager.STATUS.TO_DO));
        assertTrue(fm.configs.get("projetE/sub/conf9.json").equals(FileManager.STATUS.TO_DO));

        fm.getConfig("0.0.0.0");
        fm.getConfig("0.0.0.0");
        fm.getConfig("0.0.0.0");

        assertTrue(fm.configs.get("projetD/conf7.json").equals(FileManager.STATUS.IN_PROGRESS));
        assertTrue(fm.configs.get("projetE/conf8.json").equals(FileManager.STATUS.IN_PROGRESS));
        assertTrue(fm.configs.get("projetE/sub/conf9.json").equals(FileManager.STATUS.IN_PROGRESS));

        fm.postResult("projetD/conf7.json","res", "0.0.0.0");
        fm.postResult("projetE/conf8.json","res", "0.0.0.0");
        fm.postResult("projetE/sub/conf9.json","res", "0.0.0.0");

        assertTrue(fm.configs.get("projetD/conf7.json").equals(FileManager.STATUS.DONE));
        assertTrue(fm.configs.get("projetE/conf8.json").equals(FileManager.STATUS.DONE));
        assertTrue(fm.configs.get("projetE/sub/conf9.json").equals(FileManager.STATUS.DONE));

        assertTrue(new File(dest, "projetD/conf7.json").exists());
        assertTrue(new File(dest, "projetE/conf8.json").exists());
        assertTrue(new File(dest, "projetE/sub/conf9.json").exists());

        fm.reload();

        assertTrue(fm.configs.get("projetD/conf7.json").equals(FileManager.STATUS.DONE));
        assertTrue(fm.configs.get("projetE/conf8.json").equals(FileManager.STATUS.DONE));
        assertTrue(fm.configs.get("projetE/sub/conf9.json").equals(FileManager.STATUS.DONE));


        conf7.delete();
        conf8.delete();
        conf9.delete();
        projetD.delete();
        sub.delete();
        projetE.delete();

        new File(dest, "projetD/conf7.json").delete();
        new File(dest, "projetD").delete();
        new File(dest, "projetE/conf8.json").delete();
        new File(dest, "projetE/sub/conf9.json").delete();
        new File(dest, "projetE/sub").delete();
        new File(dest, "projetE").delete();
    }

    /*public String getProperties(String path) {
        String[] tmp = path.split("/");
        if (tmp.length != 3) return null;
        String exp = tmp[0];
        String project = tmp[1];
    }*/

}
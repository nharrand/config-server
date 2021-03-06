package se.kth;

import static spark.Spark.*;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import spark.Request;
import spark.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

public class App {

    @Parameter(names = {"--src", "-s"}, description = "Directory containing source configurations")
    String src;
    @Parameter(names = {"--dest", "-d"}, description = "Directory to put results")
    String dest;
    @Parameter(names = {"--errors", "-e"}, description = "Directory to put errors")
    String errors;
    @Parameter(names = {"--port", "-p"}, description = "Directory to put errors")
    Integer port = 8080;

    public static void printUsage(JCommander jc) {
        jc.usage();
    }

    public static void main( String[] args )
    {
        App app = new App();
        JCommander jcom = new JCommander(app, args);
        if(app.src == null || app.dest == null || app.errors == null) {
            printUsage(jcom);
        } else {
            File srcDir = new File(app.src);
            File destDir = new File(app.dest);
            File errorDir = new File(app.errors);
            FileManager fm = new FileManager(srcDir,destDir,errorDir);

            System.out.println("Server is Starting on port " + app.port);
            port(app.port);


            get("/getConfig", (Request req, Response res) -> {
                res.type("text/json");
                File jsonFile = fm.getConfig(req.ip());
                if(jsonFile != null) {
                    File properties = new File(jsonFile.getParentFile(), "properties.properties");
                    res.status(200);
                    Properties p = new Properties();
                    if(properties.exists() && properties.isFile()) {
                        p.load(new FileInputStream(properties));
                        p.setProperty("transformation.directory", "transformations/" + FileManager.getPath(jsonFile,fm.src));
                        p.setProperty("result", "output/" + FileManager.getPath(jsonFile,fm.src));
                        res.type("text/json");
                        Enumeration<String> enums = (Enumeration<String>) p.propertyNames();
                        while (enums.hasMoreElements()) {
                            String key = enums.nextElement();
                            String value = p.getProperty(key);
                            res.header(key,value);
                        }
                    }

                    return FileManager.getFileContent(jsonFile);
                } else {
                    res.status(204);
                    return "No more configuration to process.";
                }
            });

            post("/postResult", (Request req, Response res) -> {
                String config = req.headers("transformation.directory");
                if(config != null) {
                    res.status(200);
                    fm.postResult(config, req.body(), req.ip());
                } else  {
                    res.status(400);
                }
                return "";
            });

            post("/postError", (Request req, Response res) -> {
                String config = req.headers("transformation.directory");
                if(config != null) {
                    res.status(200);
                    fm.postError(config, req.body());
                } else  {
                    res.status(400);
                }
                return "";
            });

            get("/reload", (Request req, Response res) -> {
                fm.reload();
                res.status(200);
                return "OK";
            });

            get("/overview", (Request req, Response res) -> {
                res.type("text/html");
                return ReportGenerator.generateReport(fm);
            });

            get("/style.css", (Request req, Response res) -> {
                res.type("text/css");
                return ReportGenerator.getTemplateByID("templates/style.css");
            });

            get("/script.js", (Request req, Response res) -> {
                res.type("text/javascript");
                return ReportGenerator.getTemplateByID("templates/script.js");
            });

            get("/config/*", (Request req, Response res) -> {
                String str = String.join("/", req.splat());

                File f = new File(srcDir, str);
                if(f.exists() && f.isFile()) {
                    res.type("text/json");
                    return FileManager.getFileContent(f);
                } else {
                    res.status(404);
                    return "file not found";
                }
            });

            get("/result/*", (Request req, Response res) -> {
                String str = String.join("/", req.splat());

                File f = new File(destDir, str);
                if(f.exists() && f.isFile()) {
                    res.type("text/json");
                    return FileManager.getFileContent(f);
                } else {
                    res.status(404);
                    return "file not found";
                }
            });

            get("/errors/*", (Request req, Response res) -> {
                String str = String.join("/", req.splat());

                File f = new File(errorDir, str);
                if(f.exists() && f.isFile()) {
                    res.type("text/json");
                    return FileManager.getFileContent(f);
                } else {
                    res.status(404);
                    return "file not found";
                }
            });

            System.out.println("Server is Running");
        }
    }
}

package se.kth;

import java.io.File;
import java.io.InputStream;

public class ReportGenerator {
    public static String generateReport(FileManager fm) {
        String html = getTemplateByID("templates/overview.html");
        html = html.replace("<!--header-->", generateTableHeader());
        for(File f : fm.src.listFiles()) {
            html = html.replace("<!--sections-->", generateDirReport(fm, f));
        }
        return html;
    }

    public static String generateTableHeader() {
        return  "                    <th>Name</th>\n" +
                "                    <th>Type</th>\n" +
                "                    <th>Status</th>\n" +
                "                    <th>Assigned to</th>\n" +
                "                    <th>Last Modification</th>\n";
    }

    public static String generateDirReport(FileManager fm, File f) {
        int nbColomns = 5;
        String res = "";
        if(f.isDirectory()) {
            res = "            <tbody class=\"labels\">\n" +
                    "                    <tr>\n" +
                    "                        <td colspan=\"" + nbColomns + "\">\n" +
                    "                            <label for=\"" + FileManager.getPath(f, fm.src) + "\">" + FileManager.getPath(f, fm.src) + "</label>\n" +
                    "                            <input type=\"checkbox\" name=\"" + FileManager.getPath(f, fm.src) + "\" id=\"" + FileManager.getPath(f, fm.src) + "\" data-toggle=\"toggle\">\n" +
                    "                        </td>\n" +
                    "                    </tr>\n" +
                    "                </tbody>\n" +
                    "                <tbody class=\"hide\">\n";
            for(File c : f.listFiles()) {
                res += generateDirReport(fm, c);
            }
            res +=  "                </tbody>\n";
        } else {
            String path = FileManager.getPath(f, fm.src);
            if(fm.configs.containsKey(path)) {
                String cla = "error";
                switch (fm.configs.get(path)) {
                    case IN_PROGRESS:
                        cla = "inprogress";
                        break;
                    case TO_DO:
                        cla = "todo";
                        break;
                    case DONE:
                        cla = "done";
                        break;
                    case ERROR:
                        cla = "error";
                }

                res =   "                    <tr class=\"" + cla + "\">\n" +
                        "                        <td class=\"" + cla + "\"><a href=\"/config/" + FileManager.getPath(f, fm.src) + "\">" + f.getName() + "</a></td>\n" +
                        "                        <td class=\"" + cla + "\">.</td>\n" +
                        "                        <td class=\"" + cla + "\">.</td>\n" +
                        "                        <td class=\"" + cla + "\">.</td>\n" +
                        "                        <td class=\"" + cla + "\">.</td>\n" +
                        "                    </tr>\n";
            }
        }
        return res;
    }

    public static String getTemplateByID(String template_id) {
        InputStream input = ReportGenerator.class.getClassLoader().getResourceAsStream(template_id);
        String result = null;
        try {
            if (input != null) {
                result = org.apache.commons.io.IOUtils.toString(input, java.nio.charset.Charset.forName("UTF-8"));
                input.close();
            } else {
                System.out.println("[Error] Template not found: " + template_id);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            return null; // the template was not found
        }
        return result;
    }
}

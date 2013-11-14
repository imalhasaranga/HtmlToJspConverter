package htmltojsp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author imal
 */
public class ConversionLogic {

    private String sourcepath;
    public static int TOTAL_FILES;
    public static int CURRENT_READ_FILE;
    public static String matchingCriteria = "html|htm|HTML";
    public static String replacingExtension = "jsp";
    private ArrayList<File> htmlfiles = new ArrayList<File>();

    public ConversionLogic(String sourcepath) {
        this.sourcepath = sourcepath;
        
    }

    public String fetchFiles() {
        
        getHtmlfiles(sourcepath);
        String files = "";
        for (File f : htmlfiles) {
            files += f.getAbsolutePath() + "\n";
        }
        TOTAL_FILES = htmlfiles.size();
        return files;
    }

    public void getHtmlfiles(String path) {

        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) {
            return;
        }

        for (File f : list) {
            if (!f.isDirectory()) {

                if (isHtml(f)) {
                    htmlfiles.add(f);
                }
            } else {
                getHtmlfiles(f.getAbsolutePath());
            }

        }
    }

    public boolean isHtml(File f) {

        String filename = f.getName();
        String extension = filename.substring(filename.lastIndexOf("."), filename.length());
        extension = extension.toLowerCase();
        if (extension.equals(".html") || extension.equals(".htm")) {
            return true;
        }
        return false;
    }

    public void convertALL() {


        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                for (File f : htmlfiles) {
                    convertSingleFile(f);
                }
            }
        });

        t.start();
    }

    private void convertSingleFile(File f) {
        StringBuffer bf = new StringBuffer();
        ++CURRENT_READ_FILE;
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {


                Pattern p = Pattern.compile("^*\\s*href\\s*=\\s*('|\")\\s*(.?)*\\.(" + matchingCriteria + ")\\s*('|\")\\s*");
                Matcher m = p.matcher(line);
                if (m.find()) {
                    String matc = m.group().trim();
                    String corrctm = matc.replaceAll("^*\\s*\\.(" + matchingCriteria + ")\\s*", "." + replacingExtension);
                    String changedline = m.replaceFirst(" " + corrctm + " ");
                    System.out.println("LINE" + line + "      REPLACELINE = " + changedline);
                    line = changedline;
                }
                bf.append(line + "\n");
            }
            br.close();
        } catch (Exception e) {
        }
        System.out.println(bf.toString());
       writeToFile(f, bf);
    }

    private void writeToFile(File f, StringBuffer buf) {
        try {
            String s = f.getAbsolutePath();
            s  = s.substring(0,s.lastIndexOf("."))+"."+replacingExtension;
            File f1 = new File(s);
            BufferedWriter out = new BufferedWriter(new FileWriter(f1));
            String outText = buf.toString();
            out.write(outText);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        f.delete();
    }
}

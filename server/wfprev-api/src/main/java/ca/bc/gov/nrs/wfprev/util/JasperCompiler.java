package ca.bc.gov.nrs.wfprev.util;

import net.sf.jasperreports.engine.JasperCompileManager;

import java.io.File;

public class JasperCompiler {

    public static void main(String[] args) throws Exception {
        String inputDir = "src/main/resources/jasper-template";
        String outputDir = "target/classes/jasper";

        File dir = new File(inputDir);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".jrxml"));

        if (files == null) {
            throw new RuntimeException("No JRXML files found in " + inputDir);
        }

        new File(outputDir).mkdirs();

        for (File file : files) {
            String outPath = outputDir + "/" + file.getName().replace(".jrxml", ".jasper");
            JasperCompileManager.compileReportToFile(file.getAbsolutePath(), outPath);
            System.out.println("Compiled: " + file.getName() + " -> " + outPath);
        }
    }
}

package cn.labzen.spring;

import cn.labzen.spring.env.Crypto;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class EncryptConfigFileUtil {

  public static void main(String[] args) throws Exception {
    String inputLocation = "/Users/dean/Working/labzen/configs/spring";
    String outputLocation = "/Users/dean/Working/labzen/configs/spring/crypto";

    File inputDirectory = new File(inputLocation);
    File outputDirectory = new File(outputLocation);
    if (!outputDirectory.exists()) {
      outputDirectory.mkdir();
    }

    File[] subFiles = inputDirectory.listFiles(p -> p.isFile() && p.getName().endsWith(".yml"));
    if (subFiles == null) {
      System.out.println("目录下没有yml文件");
      return;
    }

    // !!! 对称加密 Key
    String password = "q68HAItfjWbJIBPk";
    Crypto crypto = new Crypto(password);

    for (File subFile : subFiles) {
      String name = subFile.getName();
      int dotIndex = name.lastIndexOf('.');
      String cleanName = name.substring(0, dotIndex);

      String content = Files.readString(subFile.toPath());
      byte[] encryptedContent = crypto.encrypt(content);

      File output = new File(outputDirectory, cleanName + ".cfg");
      Path outputPath = output.toPath();
      if (output.exists()) {
        Files.delete(outputPath);
      }
      Files.write(outputPath, encryptedContent, StandardOpenOption.CREATE_NEW);
    }
  }
}

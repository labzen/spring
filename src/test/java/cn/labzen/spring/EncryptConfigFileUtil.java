package cn.labzen.spring;

import cn.labzen.cells.algorithm.crypto.Ciphers;
import cn.labzen.cells.algorithm.crypto.cipher.SymmetricalCipher;
import cn.labzen.cells.core.utils.Strings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class EncryptConfigFileUtil {

  public static void main(String[] args) throws Exception {
    String inputLocation = "C:\\Dean\\working\\labzen\\configs\\spring";
    String outputLocation = "C:\\Dean\\working\\labzen\\configs\\spring\\crypto";

    File inputFile = new File(inputLocation);
    File outputFile = new File(outputLocation);
    if (!outputFile.exists()) {
      outputFile.mkdir();
    }

    File[] subFiles = inputFile.listFiles(p -> p.isFile() && p.getName().endsWith(".yml"));
    if (subFiles == null) {
      System.out.println("目录下没有yml文件");
      return;
    }

    // !!! 对称加密 Key
    String cryptoKey = "q68HAItfjWbJIBPk";
    byte[] iv = new byte[16];
    Arrays.fill(iv, (byte) 0);
    SymmetricalCipher sc = Ciphers.symmetrical().withKey(cryptoKey).withIVParameter(iv);

    for (File subFile : subFiles) {
      String name = subFile.getName();
      String cleanName = Strings.trim(name, ".yml");

      byte[] contentBytes = Files.readAllBytes(subFile.toPath());
      byte[] encryptedContent = sc.encrypt(contentBytes);

      File output = new File(outputFile, cleanName + ".cfg");
      Path outputPath = output.toPath();
      if (output.exists()) {
        Files.delete(outputPath);
      }
      Files.write(outputPath, encryptedContent, StandardOpenOption.CREATE_NEW);
    }
  }

}

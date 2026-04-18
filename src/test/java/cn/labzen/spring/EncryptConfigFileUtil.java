package cn.labzen.spring;

import cn.labzen.spring.env.Crypto;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 配置文件加密工具类
 * 用于将YAML配置文件加密为二进制格式
 *
 * 使用方式：
 * java -cp your-app.jar cn.labzen.spring.EncryptConfigFileUtil <输入目录> <输出目录> <加密密码>
 */
public class EncryptConfigFileUtil {

  public static void main(String[] args) throws Exception {
    // 从命令行参数读取配置
    if (args.length < 3) {
      System.out.println("用法: EncryptConfigFileUtil <输入目录> <输出目录> <加密密码>");
      System.out.println("示例: EncryptConfigFileUtil /path/to/input /path/to/output your-password");
      return;
    }

    String inputLocation = args[0];
    String outputLocation = args[1];
    String password = args[2];

    File inputDirectory = new File(inputLocation);
    if (!inputDirectory.exists() || !inputDirectory.isDirectory()) {
      System.out.println("输入目录不存在或不是有效目录: " + inputLocation);
      return;
    }

    File outputDirectory = new File(outputLocation);
    if (!outputDirectory.exists()) {
      if (!outputDirectory.mkdirs()) {
        System.out.println("无法创建输出目录: " + outputLocation);
        return;
      }
    }

    File[] subFiles = inputDirectory.listFiles(p -> p.isFile() && p.getName().endsWith(".yml"));
    if (subFiles == null || subFiles.length == 0) {
      System.out.println("目录下没有yml文件: " + inputLocation);
      return;
    }

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

    System.out.println("加密完成，共处理 " + subFiles.length + " 个文件");
  }
}

package de.scaramangado.lily.commandline.util;

import java.io.InputStream;
import java.util.Scanner;

public class LilyScanner {

  private final Scanner scanner;

  public LilyScanner(InputStream inputStream) {

    scanner = new Scanner(inputStream);
  }

  public String nextLine() {

    return scanner.nextLine();
  }
}

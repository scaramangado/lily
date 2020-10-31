package de.scaramangado.lily.commandline.util;

import de.scaramangado.lily.testutils.InputStreamMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LilyScannerTest {

  private LilyScanner     scanner;
  private InputStreamMock inputStreamMock;

  @BeforeEach
  void setup() {

    inputStreamMock = InputStreamMock.getInputStreamMock();

    scanner = new LilyScanner(inputStreamMock.getMock());
  }

  @Test
  void proxiesNextLine() {

    String testLine = UUID.randomUUID().toString();

    inputStreamMock.provideLine(testLine + "\r\n");

    String actual = scanner.nextLine();

    assertThat(actual).isEqualTo(testLine);
  }
}

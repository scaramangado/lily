package de.scaramanga.lily.testutils;

import org.mockito.invocation.InvocationOnMock;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InputStreamMock {

  private InputStream inputStreamMock = mock(InputStream.class);

  private InputStreamMock() { }

  public static InputStreamMock getInputStreamMock() {

    return new InputStreamMock();
  }

  public InputStream getMock() {

    return inputStreamMock;
  }

  public void provideLine(String line) {

    try {
      when(inputStreamMock.available()).thenReturn(line.getBytes().length);
      //noinspection ResultOfMethodCallIgnored
      doAnswer(invocation -> provideInputStream(line, invocation)).when(inputStreamMock)
                                                                  .read(any(byte[].class), any(int.class),
                                                                        any(int.class));
    } catch (IOException e) {
      // Mock
    }
  }

  private Object provideInputStream(String message, InvocationOnMock invocation) {

    byte[] answerMessage = (message).getBytes();

    Object[] args          = invocation.getArguments();
    byte[]   bytes         = (byte[]) args[0];
    int      startPosition = (int) args[1];

    System.arraycopy(answerMessage, 0, bytes, startPosition, answerMessage.length);

    try {
      resetInputStream();
    } catch (IOException e) {
      // Mock
    }

    return answerMessage.length;
  }

  private void resetInputStream() throws IOException {

    when(inputStreamMock.available()).thenReturn(0);
    when(inputStreamMock.read()).thenReturn(0);
  }
}

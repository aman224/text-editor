package org.texteditor;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

import java.io.IOException;
import java.util.Arrays;


public class Main {
    private static LibC.Termios defaultAttributes;

    public static void main(String[] args) throws IOException {
        System.out.print("TEXT EDITOR");

        enableRawMode();

        while (true) {
            int key = System.in.read();

            if (key == 'q') {
                resetAttributes();
                System.exit(0);
            }

            System.out.print((char) key + "(" + key + ")\r\n");
        }
    }

    private static void enableRawMode() {
        LibC.Termios termios = new LibC.Termios();

        int rc = LibC.INSTANCE.tcgetattr(LibC.SYSTEM_OUT_FD, termios);
        if (rc != 0) {
            System.out.println("Error calling tcgetattr");
            System.exit(rc);
        }
        defaultAttributes = LibC.Termios.of(termios);

        termios.c_lflag &= ~(LibC.ECHO | LibC.ICANON | LibC.IEXTEN | LibC.ISIG);
        termios.c_iflag &= ~(LibC.IXON | LibC.ICRNL);
        termios.c_oflag &= ~(LibC.OPOST);

        termios.c_cc[LibC.VMIN] = 0;
        termios.c_cc[LibC.VTIME] = 1;

        LibC.INSTANCE.tcsetattr(LibC.SYSTEM_OUT_FD, LibC.TCSA_FLUSH, termios);
    }

    private static void resetAttributes() {
        LibC.INSTANCE.tcsetattr(LibC.SYSTEM_OUT_FD, LibC.TCSA_FLUSH, defaultAttributes);
    }
}

interface LibC extends Library {
    int SYSTEM_OUT_FD = 0;

    int ISIG = 1, ICANON = 1, ECHO = 10, TCSA_FLUSH = 2, IXON = 2000,
            ICRNL = 400, IEXTEN = 100000, OPOST = 1, VMIN = 6, VTIME = 5, TI0CGWINSZ = 0x5413;

    LibC INSTANCE = Native.load("c", LibC.class);

    @Structure.FieldOrder(value = {"c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_cc"})
    class Termios extends Structure {
        public int c_iflag, c_oflag, c_cflag, c_lflag;
        public byte[] c_cc = new byte[19];  /* special characters */

        public static Termios of(Termios t) {
            Termios copy = new Termios();
            copy.c_iflag = t.c_iflag;
            copy.c_oflag = t.c_oflag;
            copy.c_cflag = t.c_cflag;
            copy.c_lflag = t.c_lflag;
            copy.c_cc = t.c_cc.clone();
            return copy;
        }

        @Override
        public String toString() {
            return "Termios {" +
                    ", c_iflag=" + c_lflag +
                    ", c_oflag=" + c_oflag +
                    ", c_cflag=" + c_cflag +
                    ", c_lflag=" + c_lflag +
                    ", c_cc=" + Arrays.toString(c_cc);
        }
    }

    int tcgetattr(int fd, Termios termios);

    int tcsetattr(int fd, int optional_actions, Termios termios);
}
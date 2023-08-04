package ru.themikhailz.qr;

import ru.themikhailz.util.QRCodeGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileQrCodeGenerator {

    public static File generate(String data, int size) throws IOException {
        byte[] bytes = QRCodeGenerator.generateQRCodeImage(data, size);
        File qr = File.createTempFile("qr-code", ".jpg");
        try(FileOutputStream fos = new FileOutputStream(qr);){
            fos.write(bytes);
        }
        return qr;
    }
}

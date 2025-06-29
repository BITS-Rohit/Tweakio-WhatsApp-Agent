package org.Tweakio.WhatsappWeb;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class SessionQR {
    void Qr(File path ) throws IOException, NotFoundException, WriterException {
        BufferedImage original = ImageIO.read(path);
        LuminanceSource src = new BufferedImageLuminanceSource(original);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(src));
        Result result = new MultiFormatReader().decode(bitmap);
        String payload = result.getText();
        System.out.println("Decoded payload: " + payload);

        // 2) Re‑encode exactly the same payload into a new QR
        int size = 300; // pixels
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(payload, BarcodeFormat.QR_CODE, size, size);

        Path outPath = Path.of("wrapped_qr.png");
        MatrixToImageWriter.writeToPath(matrix, "PNG", outPath);
        System.out.println("New QR written to " + outPath.toAbsolutePath());
    }
}



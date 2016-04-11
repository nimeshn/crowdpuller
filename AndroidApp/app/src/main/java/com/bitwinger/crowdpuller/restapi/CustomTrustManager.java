package com.bitwinger.crowdpuller.restapi;

import android.content.Context;

import com.bitwinger.crowdpuller.R;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by Nimesh on 27-01-2016.
 */
public class CustomTrustManager {

    private static SSLContext context = null;
    private static SSLContext getSSLContext(Context current){
        try {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt
            InputStream caInput = new BufferedInputStream(current.getResources().openRawResource(R.raw.bitwinger));
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext cntxt = SSLContext.getInstance("TLS");
            cntxt.init(null, tmf.getTrustManagers(), null);

            return cntxt;
        }
        catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public static SSLSocketFactory getSSLSocketFactory(Context current){
        if (context == null){
            context = getSSLContext(current);
        }
        return context.getSocketFactory();
    }
}

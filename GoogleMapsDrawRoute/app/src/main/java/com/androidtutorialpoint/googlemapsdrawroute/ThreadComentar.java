package com.androidtutorialpoint.googlemapsdrawroute;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;


public class ThreadComentar extends Thread implements Runnable
{

    private String mensaje;
    private Hospital actual;

    public ThreadComentar(String pMensaje, Hospital pHospital)
    {
        mensaje = pMensaje;
        actual = pHospital;
    }


    @Override
    public void run() {
        long id = actual.getId();
        System.out.println("El mensaje recibido en el Thread es: " + mensaje);
        String urlHospitales = null;
        URL url = null;

        try
        {
            String desc = URLEncoder.encode(mensaje,"UTF-8");
            String query = "hospital="+id+"&descripcion="+desc;
            urlHospitales = "https://galileounbosque.herokuapp.com/comentario";
            url = new URL(urlHospitales);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setUseCaches(false);

            Writer wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(query);
            wr.flush();
            wr.close();

            //Obtenemos respuesta
            InputStream is = con.getInputStream();
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            String linea = buf.readLine();
            System.out.println(linea);

            buf.close();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

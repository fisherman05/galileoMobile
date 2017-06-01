package com.androidtutorialpoint.googlemapsdrawroute;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

public class ThreadCalificar extends Thread implements Runnable {

    private double calificacion;
    private Hospital actual;
    public ThreadCalificar(double pCalificacion, Hospital pHospital)
    {
        calificacion = pCalificacion;
        actual = pHospital;
    }
    @Override
    public void run() {
        double id = actual.getId();
        System.out.println("El mensaje recibido en el Thread es: " + calificacion);
        URL url = null;
        String urlHospitales = null;
        try
        {
            String query = "id="+id+"&calificacion="+calificacion;
            urlHospitales = "https://galileounbosque.herokuapp.com/hospital";
            url = new URL(urlHospitales);
            System.out.println(url);
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

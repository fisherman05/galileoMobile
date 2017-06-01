package com.androidtutorialpoint.googlemapsdrawroute;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ThreadVerComentarios extends Thread implements Runnable {

    private Hospital actual;
    HttpHandler sh = null;
    private String comentarios;
    public ThreadVerComentarios(Hospital pHospital) {
        actual = pHospital;
        comentarios = "";
    }

    @Override
    public void run() {
        sh = new HttpHandler();
        long id = actual.getId();
        String urlHospitales = "https://galileounbosque.herokuapp.com/comentario/?hospital=" + id;
        System.out.println("Url Resultante: " + urlHospitales);

        String jsonStr = sh.makeServiceCall(urlHospitales);

        System.out.println("Json Resultante: " + jsonStr);


        try
        {
            JSONArray arreglo = new JSONArray(jsonStr);

            for(int i = 0; i< arreglo.length(); i++)
            {
                JSONObject obj = (JSONObject) arreglo.get(i);
                String descripcion = (String)obj.get("descripcion");
                comentarios += descripcion + "\n";
            }
            System.out.println("Comentarios thread: " + comentarios);

            MapsActivity.capturarComentarios(comentarios);
        }

        catch (JSONException e)
        {
            e.printStackTrace();
        }

    }
}
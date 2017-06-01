package com.androidtutorialpoint.googlemapsdrawroute;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    static boolean salio;
    static ArrayList<Hospital> hospitales = new ArrayList<Hospital>();
    static Hospital actual = null;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    static String menu2[] = {""};
    HttpHandler sh = null;
    List<Polyline> polylines = new ArrayList<Polyline>();
    String valoracion []={"Muy lleno", "Lleno", "Normal", "Vacio"};
    static LatLng hospitalPos = null;
    AlertDialog.Builder builder;
    static public String comentarios = "";

    public static void capturarComentarios(String cs){
        comentarios = cs;

    }

    public static void actualizarHospitalActual(int i)
    {
        if(i < hospitales.size())
        {
            actual = hospitales.get(i);
            System.out.println(actual.getNombre());
        }
    }

    public static void agregarHospital(Hospital nuevo)
    {
        System.out.println("Está agregando hospital");
        if(hospitales.contains(nuevo))
        {
            hospitales.remove(nuevo);
        }
        hospitales.add(nuevo);
        System.out.println(hospitales.size());
    }

    public void comentar(String pMensaje)
    {
        //Si no hay hospital seleccionado, el usuario debe seleccionar el hospital

        System.out.println("Entró a comentar");


        String latH = actual.getLatitud()+ "";
        String lonH = actual.getLongitud() + "";

        String latGeo =  mLastLocation.getLatitude()+"";
        String lngGeo = mLastLocation.getLongitude()+"";

        latH = latH.substring(0,5);
        lonH = lonH.substring(0,6);

        latGeo = latGeo.substring(0,5);
        lngGeo = lngGeo.substring(0,6);

        //if(!latH.equals(latGeo) || !lonH.equals(lngGeo))
        //{
            //Se muestra dialogo indicando que el usuario debe estar parado en esa posición
          //  Toast.makeText(getApplicationContext(),
            //        "Debes estar ubicado en el Hospital para comentar.", Toast.LENGTH_SHORT).show();
        //}

        //else
        //{
            ThreadComentar nuevoThread = new ThreadComentar(pMensaje, actual);
            nuevoThread.start();
        //}

    }
    public void verComentarios() {

        ThreadVerComentarios nuevoThread = new ThreadVerComentarios(actual);
        nuevoThread.start();

        while(nuevoThread.isAlive())
        {
            //Esperamos a que termine el thread
        }

        if(comentarios.equals("")){
            comentarios = "No hay comentarios";
        }
        builder = new AlertDialog.Builder(this);
        builder.setTitle(actual.getNombre());
        builder.setMessage(comentarios);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();

        System.out.println("Comentarios capturados: " + comentarios);
    }

    public void comentarH() {
        if (actual == null) {
            Toast.makeText(getApplicationContext(),
                    "Seleccione un hospital", Toast.LENGTH_SHORT).show();
        } else {
            builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            builder.setTitle(actual.nombre);
            builder.setView(inflater.inflate(R.layout.activity_comentar, null));
            View regisText = inflater.inflate(R.layout.activity_comentar, null);
            final EditText et1 = (EditText) regisText.findViewById(R.id.textoComentar);

            builder.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Dialog f = (Dialog) dialogInterface;
                    EditText et = (EditText) f.findViewById(R.id.textoComentar);
                    String txt = et.getText().toString();
                    if(txt.equals("")){
                        Toast.makeText(getApplicationContext(), "Campo vacio", Toast.LENGTH_SHORT).show();
                    }else{
                        comentar(txt);
                        Toast.makeText(getApplicationContext(), "Comentario enviado", Toast.LENGTH_SHORT).show();
                    }

                }
            });
            builder.setNeutralButton("+ Comentarios", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                verComentarios();
                }
            });

            builder.setNegativeButton(android.R.string.cancel, null);
            builder.create().show();
        }
    }
    public void valorar(double calificacionEnviada) {
        System.out.println("Entró a valorar");


        String latH = actual.getLatitud() + "";
        String lonH = actual.getLongitud() + "";

        String latGeo = mLastLocation.getLatitude() + "";
        String lngGeo = mLastLocation.getLongitude() + "";

        latH = latH.substring(0, 5);
        lonH = lonH.substring(0, 6);

        latGeo = latGeo.substring(0, 5);
        lngGeo = lngGeo.substring(0, 6);

        //if (!latH.equals(latGeo) || !lonH.equals(lngGeo)) {
            //Se muestra dialogo indicando que el usuario debe estar parado en esa posición
          //  Toast.makeText(getApplicationContext(),
            //        "Debes estar ubicado en el Hospital para comentar.", Toast.LENGTH_SHORT).show();
        //}
        //else {
            ThreadCalificar nuevoThread = new ThreadCalificar(calificacionEnviada, actual);
            nuevoThread.start();
        //}
    }
    public void valoracionH() {
        if (actual == null) {
            Toast.makeText(getApplicationContext(),
                    "Seleccione un hospital", Toast.LENGTH_SHORT).show();
        } else {
            builder = new AlertDialog.Builder(this);
            builder.setTitle(actual.nombre);
            builder.setSingleChoiceItems(valoracion, 0, null);
            builder.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Dialog f = (Dialog) dialogInterface;
                    double posicion = (double)((AlertDialog)f).getListView().getCheckedItemPosition()+1;
                    System.out.println("La posicion es: "+ posicion);
                    valorar(posicion);
                    Toast.makeText(getApplicationContext(),
                            "Valoracion enviada", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            Dialog dialog = builder.create();
            dialog.show();
        }
    }
    public void informar(){


        if(actual == null){
            Toast.makeText(getApplicationContext(),
                    "Seleccione un hospital", Toast.LENGTH_SHORT).show();
        }else {
            String indice="";
            if(actual.getIndice()==0) {
                indice="No hay calificacion";
            }
            else if (actual.getIndice()>0 && actual.getIndice()<=1.5){
                indice = valoracion[0];
            }else if(actual.getIndice()>1.5 && actual.getIndice()<=2.5){
                indice=valoracion[1];
            }else if(actual.getIndice()>2.5 && actual.getIndice()<=3.5){
                indice= valoracion[2];
            }else if(actual.getIndice()>3.5){
                indice = valoracion[3];
            }
            String informacion = "Direccion: " + actual.getDireccion() + " " + "\n" + "Telefono: " + actual.getTelefono()
                    +"\n"+"Estado: " + indice;
            builder = new AlertDialog.Builder(this);
            builder.setTitle(actual.nombre);
            builder.setMessage(informacion);

            builder.setNegativeButton("Volver", null);
            Dialog dialog = builder.create();
            dialog.show();
        }
    }
    public void instrucciones(){
        builder = new AlertDialog.Builder(this);
        builder.setTitle("¡Bienvenido!");
        builder.setMessage("Toca la pantalla para conocer los hospitales que tienes cerca");
        builder.setPositiveButton("Continuar", null);
        builder.create().show();
    }
    public static ArrayList<Hospital> darHospitales()
    {
        return hospitales;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        builder = new AlertDialog.Builder(this);
        builder.setTitle("¡Advertencia!");
        builder.setMessage("Galileo es un apoyo para tu toma de decisiones.\n" +
                "La informacion es provista por la ciudadania y para la ciudadania.\n" +
                "La aplicacion no se hace responsable de los contextos en los que se encuentren las salas de urgencias escogidas");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                instrucciones();
            }
        });
        builder.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setResult(RESULT_OK);
                finish();
            }
        });
        builder.create().show();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        hospitales = new ArrayList<Hospital>();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.dibujarRuta:
                pintarRuta();
                return true;
            case R.id.valorarH:
                valoracionH();
                return true;
            case R.id.comentarioH:
                comentarH();
                return true;
            case R.id.informacion:
                informar();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }



        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {

                System.out.println("Entró a actualizar");
                sh = new HttpHandler();
                MarkerOptions options = new MarkerOptions();

                mMap= googleMap;
                mMap.clear();
                salio = false;

                //1) Obtenemos nuestra posición (lat, lon)
                double lat = -74.710495;
                double lon = 4.711415;
                LatLng origin = new LatLng(lat, lon);
                hospitales = new ArrayList<Hospital>();

                System.out.println("Llegamos antes de iniciar el Thread");

                Thread nuevoThread = new Thread(new Runnable()
                {
                    public void run()
                    {
                        double lat = -74.710495;
                        double lon = 4.711415;
                        if(mLastLocation != null)
                        {
                            lat = mLastLocation.getLatitude();
                            lon =  mLastLocation.getLongitude();
                        }
                        String urlHospitales = "https://galileounbosque.herokuapp.com/hospital/?lat="+lat+"&lon="+lon;
                        System.out.println("Url Resultante: "+urlHospitales);

                        String jsonStr = sh.makeServiceCall(urlHospitales);

                        System.out.println("Json Resultante: "+jsonStr);
                        if(jsonStr != null)
                        {
                            try
                            {
                                JSONArray hosps = new JSONArray(jsonStr);
                                System.out.println("Hospitales JSON: " + hosps.length());
                                salio = false;
                                for(int i = 0; i< hosps.length(); i++)
                                {
                                        JSONObject h = hosps.getJSONObject(i);
                                        System.out.println("Entró al arreglo");
                                        String latH = h.getString("latitud");
                                        String lonH = h.getString("longitud");
                                        String idH = h.getString("id");
                                        String nombre = h.getString("nombre");
                                        String telefono = h.getString("telefono");
                                        String indice = h.getString("indice");
                                        String descripcion = h.getString("descripcion");
                                        String direccion = h.getString("direccion");
                                        String calificadores = h.getString("calificadores");
                                        double latitud = Double.parseDouble(latH);
                                        double longitud = Double.parseDouble(lonH);
                                        double calificacion = Double.parseDouble(indice);
                                        int elID = Integer.parseInt(idH);
                                        int numCalificadores = Integer.parseInt(calificadores);

                                        Hospital nuevo = new  Hospital();
                                        nuevo.setDescripcion(descripcion);
                                        nuevo.setId(elID);
                                        nuevo.setLatitud(latitud);
                                        nuevo.setLongitud(longitud);
                                        nuevo.setTelefono(telefono);
                                        nuevo.setDireccion(direccion);
                                        nuevo.setNombre(nombre);
                                        nuevo.setIndice(calificacion);
                                        nuevo.setCalificadores(numCalificadores);

                                        System.out.println("Latitudes y longitudes "+latH + " " + lonH + " "+idH);
                                        agregarHospital(nuevo);
                                }


                                System.out.println("Salió del for de agregar hospitals: " + salio);
                                salio = true;
                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                            }

                            catch (Throwable throwable)
                            {
                                throwable.printStackTrace();
                            }
                        }
                    }
                });


                nuevoThread.start();


                if(mLastLocation != null && false)
                {
                    lat = mLastLocation.getLatitude();
                    lon =  mLastLocation.getLongitude();
                }

                while(salio == false)
                {
                    System.out.println("Se quedó aquí");
                    //System.out.println("Acá se queda");
                    //Lo dejo quieto hasta que se termine el Thread
                }

                nuevoThread.interrupt();

                hospitales = darHospitales();
                System.out.println("Ya salimos del thread " + hospitales.size());
                menu2 = new String[hospitales.size()];

                Hospital actual = null;

                for(int i = 0; i<hospitales.size(); i++)
                {
                    System.out.println("Hospitales al momento: "+hospitales.size());
                    actual = hospitales.get(i);
                    int id = actual.getId();
                    String nombre = actual.getNombre();
                    String valor = id + ": " + nombre;
                    menu2[i] = valor;
                    int pos = i + 1;
                    LatLng nuevoLatLon = new LatLng(actual.getLatitud(), actual.getLongitud());
                    if(actual.getIndice()==0){
                        System.out.println("Entro al blanco");

                        mMap.addMarker(new MarkerOptions().position(nuevoLatLon)
                                .title(pos+": " +actual.getNombre()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.hospital)));
                    }

                    if (actual.getIndice()>0 && actual.getIndice()<=1.5){
                        System.out.println("Entro al rojo");
                        mMap.addMarker(new MarkerOptions().position(nuevoLatLon)
                                .title(pos+": " +actual.getNombre()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.red)));
                    }

                    if(actual.getIndice()>1.5 && actual.getIndice()<=2.5){
                        System.out.println("Entro al naranja");
                        mMap.addMarker(new MarkerOptions().position(nuevoLatLon)
                                .title(pos+": " +actual.getNombre()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.orange)));
                    }if(actual.getIndice()>2.5 && actual.getIndice()<=3.5){
                    System.out.println("Entro al amarillo");
                        mMap.addMarker(new MarkerOptions().position(nuevoLatLon)
                                .title(pos+": " +actual.getNombre()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.yellow)));
                    }
                    if(actual.getIndice()>3.5){
                        System.out.println("Entro al verde");
                        mMap.addMarker(new MarkerOptions().position(nuevoLatLon)
                                .title(pos+": " +actual.getNombre()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.green)));
                    }


                    System.out.println("Entró al for SEBAS");
                }


                System.out.println("El menú 2 es: " + menu2.length);


                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                    @Override
                    public boolean onMarkerClick(Marker marker)
                    {
                        String titulo = marker.getTitle();

                        String[] valores = titulo.split(": ");
                        int pos = Integer.parseInt(valores[0]);
                        int i = pos-1;
                        actualizarHospitalActual(i);
                        return false;
                    }
                });


            }

        });
    }

    private void pintarRuta() {
        if (actual != null) {

            //limpiamos las rutas
            for (int i = 0; i < polylines.size(); i++) {
                Polyline p = polylines.get(i);
                p.remove();
            }

            polylines.clear();

            //pintamos ruta
            LatLng origin = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            LatLng dest = new LatLng(actual.getLatitud(), actual.getLongitud());
            String url = getUrl(origin, dest);
            Log.d("onMapClick", url.toString());
            FetchUrl FetchUrl = new FetchUrl();

            FetchUrl.execute(url);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        } else {

            Toast.makeText(getApplicationContext(),
                    "Seleccione un hospital", Toast.LENGTH_SHORT).show();
        }
    }
    private String getUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }


    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }



    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(4);
                lineOptions.color(Color.MAGENTA);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            if(lineOptions != null) {
                Polyline p = mMap.addPolyline(lineOptions);
                polylines.add(p);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }

        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {


                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }
}
package com.lifeapp.helper;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;

public class Local {


    public static float calcularDistancia(LatLng pontoInicial, LatLng pontoFinal){

        Location localInicial = new Location("Local Inicial");
        localInicial.setLatitude(pontoInicial.latitude);
        localInicial.setLongitude(pontoInicial.longitude);

        Location localFinal= new Location("Local Final");
        localFinal.setLatitude(pontoFinal.latitude);
        localFinal.setLongitude(pontoFinal.longitude);

        float distancia = localInicial.distanceTo(localFinal);

        return distancia/1000;
    }

    public static String formatarDistancia(float distancia){

     String distanciaformatada;

     if(distancia< 1){

         distancia  = distancia *1000;
         distanciaformatada = Math.round(distancia) + " M ";
     }else{
         DecimalFormat decimal = new DecimalFormat("0.0");
         distanciaformatada = decimal.format(distancia) + " KM";
     }

        return distanciaformatada;
    }
}

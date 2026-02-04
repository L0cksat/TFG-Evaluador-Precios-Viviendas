import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class InmobilarioBuscarService {
  
  constructor(private http: HttpClient) {}

  botInmobilario(direccion: string, metros: number, habitaciones: number) {
    // TODO: Crear endpoint para llamar el metodo de Python
    //return this.http.get(`http://localhost:8000/run-search?address=${direccion}?metros=~${metros}?habitaciones=${habitaciones}`);
    console.log('Values from python bot component: direccion ' + direccion + ' metros: '+ metros + ' habitaciones: ' + habitaciones);
  }
}

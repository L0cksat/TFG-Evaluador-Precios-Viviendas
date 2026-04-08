import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface ValuationRequest {
  direccion: string;
  metrosCuadrados: number;
  habitaciones: number;
  referenciaCatastral?: string;
}

@Injectable({
  providedIn: 'root',
})
export class InmobilarioBuscarService {

  private apiUrl = 'http://localhost:8080/api/valoraciones';

  constructor(private http: HttpClient) {}

  botInmobilario(request: ValuationRequest): Observable<any> {
    
    const token = localStorage.getItem('token');

    let headers = new HttpHeaders()

    if (token && token !== 'null'){
      headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    }

    return this.http.post<any>(this.apiUrl, request, { headers });
  }
}
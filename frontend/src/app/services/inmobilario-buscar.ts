import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

// Interface matching the backend's ValuationRequest.java
export interface ValuationRequest {
  direccion: string;
  metrosCuadrados: number; // Must be 'metrosCuadrados', not 'metros'
  habitaciones: number;
  referenciaCatastral?: string;
}

@Injectable({
  providedIn: 'root',
})
export class InmobilarioBuscarService {
  // Match the port of your Spring Boot backend
  private apiUrl = 'http://localhost:8080/api/valoraciones';

  constructor(private http: HttpClient) {}

  botInmobilario(request: ValuationRequest): Observable<any> {
    
    // Retrieve the token saved during login
    const token = localStorage.getItem('token');

    // Added the base empty headers
    let headers = new HttpHeaders()

    // Added if to check to see if the token truely exists
    if (token && token !== 'null'){
      headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    }

    // Call the POST /api/valoraciones endpoint
    return this.http.post<any>(this.apiUrl, request, { headers });
  }
}
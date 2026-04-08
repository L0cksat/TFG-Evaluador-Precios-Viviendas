// src/app/services/valuation.service.ts
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface ValuationResponse {
  id: number;
  direccion: string;
  precioEstimado: number;
  fecha: string;
}

@Injectable({
  providedIn: 'root',
})
export class ValuationService {
  private apiUrl = 'http://localhost:8080/api/valoraciones';

  constructor(private http: HttpClient) {}

  getUserValuations(): Observable<ValuationResponse[]> {
    const token = localStorage.getItem('token');

    let headers = new HttpHeaders(); 

    
    if (token && token !== 'null'){
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    
    return this.http.get<ValuationResponse[]>(this.apiUrl, { headers });
  }
}
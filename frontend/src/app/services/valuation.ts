// src/app/services/valuation.service.ts
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface ValuationResponse {
  id: number;
  direccion: string;
  precioEstimado: number;
  fecha: string;
  // Add other fields from your backend ValuationResponse.java
}

@Injectable({
  providedIn: 'root',
})
export class ValuationService {
  private apiUrl = 'http://localhost:8080/api/valuations';

  constructor(private http: HttpClient) {}

  getUserValuations(): Observable<ValuationResponse[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.get<ValuationResponse[]>(`${this.apiUrl}/user`, { headers });
  }
}
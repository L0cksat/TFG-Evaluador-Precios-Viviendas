import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class GeocodingService {

  constructor(private http: HttpClient) {}

  reverseGeocode(lat: number, lng: number): Observable<string> {
    const headers = { 'User-Agent': 'Evaluty-App' }

    const url = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}`;
    return this.http.get<any>(url, { headers }).pipe(map(
      response => response.display_name
    ))
  }
}

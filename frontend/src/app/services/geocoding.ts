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
      response => {
        const addr = response.address
        const road = addr?.road || ""
        const house = addr?.house_number || ""
        const postcode =addr.postcode || ""
        const city = addr?.city || addr?.town || addr?.village || ""

        return `${road} ${house}, ${postcode} ${city}`.trim()
      }
    ))
  }
}

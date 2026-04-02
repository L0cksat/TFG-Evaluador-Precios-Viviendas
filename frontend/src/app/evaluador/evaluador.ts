import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { InmobilarioBuscarService } from '../services/inmobilario-buscar';
import { HomeDataService } from '../services/home-data';
import { GeocodingService } from '../services/geocoding';
import * as L from 'leaflet'
import 'leaflet/dist/leaflet.css'

@Component({
  selector: 'app-evaluador',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './evaluador.html',
  styleUrl: './evaluador.css'
})
export class EvaluadorComponent implements OnInit, AfterViewInit {
  formEvaluacion!: FormGroup;
  isLoading: boolean = false;
  resultadoValoracion: any = null; // Variable to hold the backend response

  constructor(
    private fb: FormBuilder, 
    private ibs: InmobilarioBuscarService, 
    private hds: HomeDataService,
    private geo: GeocodingService
  ) {}

  ngOnInit(): void {
    const direccionDeHome = this.hds.direccion;

    this.formEvaluacion = this.fb.group({
      direccion: [direccionDeHome, Validators.required],
      metros: ['', [Validators.required, Validators.min(1)]],
      habitaciones: ['', [Validators.required, Validators.min(0)]]
    });
  }

  onSubmit() {
    if (this.formEvaluacion.valid) {
      const dir = this.formEvaluacion.value.direccion;
      const metros = this.formEvaluacion.value.metros;
      const habitaciones = this.formEvaluacion.value.habitaciones;
      
      this.isLoading = true;

      this.ibs.botInmobilario(dir, metros, habitaciones).subscribe({
        next: (response) => {
          console.log('Valoración procesada exitosamente:', response);
          this.resultadoValoracion = response;
          this.isLoading = false;
          
          // Optionally: Show a success message or display the data in your HTML
        },
        error: (err) => {
          console.error('Error al conectar con el backend:', err);
          this.isLoading = false;
        }
      });
    }
  }

  @ViewChild('map') inputElement !: ElementRef
  ngAfterViewInit() {
    const map = L.map(this.inputElement.nativeElement).setView([40.40807688713441, -3.6971967296675787], 13)
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; <a href="http://openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map)
    let popup = L.popup()
    map.on('click', (event: any) =>{
      const lat = event.latlng.lat
      const lng = event.latlng.lng

      popup.setLatLng(event.latlng)
      .setContent(`Lat: ${lat}, Lng: ${lng}`)
      .openOn(map)

    this.geo.reverseGeocode(lat, lng).subscribe({
      next: (address) => {
        this.formEvaluacion.patchValue({ direccion: address})
      },
      error: (err) =>{
        console.error("Geocoding error:", err)
      }
    })
    })
  }
}